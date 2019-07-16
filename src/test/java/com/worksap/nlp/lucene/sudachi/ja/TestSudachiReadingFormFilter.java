/*
 *  Copyright (c) 2019 Works Applications Co., Ltd.
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
import java.util.HashMap;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;

public class TestSudachiReadingFormFilter extends BaseTokenStreamTestCase {
    TokenStream tokenStream;

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
    }

    public void testReadingForm() throws IOException {
        SudachiReadingFormFilterFactory factory = new SudachiReadingFormFilterFactory(Collections.emptyMap());
        ((Tokenizer)tokenStream).setReader(new StringReader("東京都に行った。"));
        tokenStream = factory.create(tokenStream);
        assertTokenStreamContents(tokenStream, new String[] {"トウキョウト", "トウキョウ", "ト", "ニ", "イッ", "タ"});
    }

    public void testRomanizedReadingForm() throws IOException {
        SudachiReadingFormFilterFactory factory = new SudachiReadingFormFilterFactory(new HashMap<String, String>() {{ put("useRomaji", "true"); }});
        ((Tokenizer)tokenStream).setReader(new StringReader("東京都に行った。"));
        tokenStream = factory.create(tokenStream);
        assertTokenStreamContents(tokenStream, new String[] {"toukyouto", "toukyou", "to", "ni", "iltu", "ta"});
    }
}
