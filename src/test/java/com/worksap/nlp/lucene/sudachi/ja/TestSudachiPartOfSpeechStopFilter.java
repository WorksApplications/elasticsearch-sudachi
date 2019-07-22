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
import java.util.HashMap;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;

public class TestSudachiPartOfSpeechStopFilter extends BaseTokenStreamTestCase {
    TokenStream tokenStream;
    SudachiPartOfSpeechStopFilterFactory factory;

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
        ((Tokenizer)tokenStream).setReader(new StringReader("東京都に行った。"));
        factory = new SudachiPartOfSpeechStopFilterFactory(new HashMap<String, String>() {{ put("tags", "stoptags.txt"); }});
    }

    public void testAllPOS() throws IOException {
        String tags = "動詞,非自立可能\n名詞,固有名詞,地名,一般\n";
        factory.inform(new StringResourceLoader(tags));
        tokenStream = factory.create(tokenStream);
        assertTokenStreamContents(tokenStream, new String[] {"都", "に", "た"});
    }

    public void testPrefix() throws IOException {
        String tags = "動詞\n名詞,固有名詞\n";
        factory.inform(new StringResourceLoader(tags));
        tokenStream = factory.create(tokenStream);
        assertTokenStreamContents(tokenStream, new String[] {"都", "に", "た"});
    }

    public void testConjugationType() throws IOException {
        String tags = "五段-カ行\n";
        factory.inform(new StringResourceLoader(tags));
        tokenStream = factory.create(tokenStream);
        assertTokenStreamContents(tokenStream,
                                  new String[] { "東京都", "東京", "都", "に", "た"});
    }

    public void testConjugationTypeAndForm() throws IOException {
        String tags = "五段-カ行,終止形-一般\n";
        factory.inform(new StringResourceLoader(tags));
        tokenStream = factory.create(tokenStream);
        assertTokenStreamContents(tokenStream,
                                  new String[] { "東京都", "東京", "都", "に", "行っ", "た"});
    }

    public void testConjugationForm() throws IOException {
        String tags = "終止形-一般\n";
        factory.inform(new StringResourceLoader(tags));
        tokenStream = factory.create(tokenStream);
        assertTokenStreamContents(tokenStream,
                                  new String[] {"東京都", "東京", "都", "に", "行っ"});
    }

    public void testPrefixWithUnmatchedSubcategory() throws IOException {
        String tags = "助詞,格助詞\n助詞,格助詞,引用\n";
        factory.inform(new StringResourceLoader(tags));
        tokenStream = factory.create(tokenStream);
        assertTokenStreamContents(tokenStream,
                                  new String[] {"東京都", "東京", "都", "行っ", "た"});
    }

    public void testTooLongCategory() throws IOException {
        String tags = "名詞,固有名詞,地名,一般,一般\n";
        factory.inform(new StringResourceLoader(tags));
        tokenStream = factory.create(tokenStream);
        assertTokenStreamContents(tokenStream,
                                  new String[] {"東京都", "東京", "都", "に", "行っ", "た"});
    }

}
