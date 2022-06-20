/*
 * Copyright (c) 2018-2022 Works Applications Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.worksap.nlp.lucene.sudachi.ja;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Collections;

import com.worksap.nlp.lucene.sudachi.aliases.BaseTokenStreamTestCase;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;

import com.worksap.nlp.sudachi.Tokenizer.SplitMode;

public class TestSudachiBaseFormFilter extends BaseTokenStreamTestCase {
    TokenStream tokenStream;
    SudachiBaseFormFilterFactory factory;

    @Rule
    public TemporaryFolder tempFolderForDictionary = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        super.setUp();
        tempFolderForDictionary.create();
        File tempFileForDictionary = tempFolderForDictionary.newFolder("sudachiDictionary");
        ResourceUtil.copy(tempFileForDictionary);

        String settings;
        try (InputStream is = this.getClass().getResourceAsStream("sudachi.json")) {
            settings = ResourceUtil.getSudachiSetting(is);
        }

        tokenStream = new SudachiTokenizer(true, SplitMode.C, tempFileForDictionary.getPath(), settings, false);
        factory = new SudachiBaseFormFilterFactory(Collections.emptyMap());
    }

    @Test
    public void testBaseForm() throws IOException {
        ((Tokenizer) tokenStream).setReader(new StringReader("東京都に行った。"));
        tokenStream = factory.create(tokenStream);
        assertTokenStreamContents(tokenStream, new String[] { "東京都", "に", "行く", "た" });
    }

    @Test
    public void testBaseFormWithUnnormalizedWord() throws IOException {
        ((Tokenizer) tokenStream).setReader(new StringReader("東京都にいった。"));
        tokenStream = factory.create(tokenStream);
        assertTokenStreamContents(tokenStream, new String[] { "東京都", "に", "いく", "た" });
    }

}
