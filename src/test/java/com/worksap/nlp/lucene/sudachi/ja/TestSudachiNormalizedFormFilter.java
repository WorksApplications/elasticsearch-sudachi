/*
 *  Copyright (c) 2018 Works Applications Co., Ltd.
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

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;

public class TestSudachiNormalizedFormFilter extends BaseTokenStreamTestCase {
    TokenStream tokenStream;
    SudachiNormalizedFormFilterFactory factory;

    @Rule
    public TemporaryFolder tempFolderForDictionary = new TemporaryFolder();

    public void setUp() throws Exception {
        super.setUp();
        tempFolderForDictionary.create();
        File tempFileForDictionary = tempFolderForDictionary
                .newFolder("sudachiDictionary");
        ResourceUtil.copy(tempFileForDictionary);

        String settings;
        try (InputStream is = this.getClass().getResourceAsStream("sudachi.json")) {
            settings = ResourceUtil.getSudachiSetting(is);
        }

        tokenStream = new SudachiTokenizer(true, SudachiTokenizer.Mode.SEARCH, tempFileForDictionary.getPath(), settings);
        factory = new SudachiNormalizedFormFilterFactory(Collections.emptyMap());
    }

    public void testNormalizedForm() throws IOException {
        ((Tokenizer)tokenStream).setReader(new StringReader("東京都に行った。"));
        tokenStream = factory.create(tokenStream);
        assertTokenStreamContents(tokenStream, new String[] {"東京都", "東京", "都", "に", "行く", "た"});
    }

    public void testNormalizedFormWithUnnormalizedWord() throws IOException {
        ((Tokenizer)tokenStream).setReader(new StringReader("東京都にいった。"));
        tokenStream = factory.create(tokenStream);
        assertTokenStreamContents(tokenStream, new String[] {"東京都", "東京", "都", "に", "行く", "た"});
    }

}
