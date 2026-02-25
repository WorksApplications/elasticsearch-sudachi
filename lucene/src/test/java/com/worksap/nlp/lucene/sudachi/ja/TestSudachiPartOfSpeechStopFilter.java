/*
 * Copyright (c) 2018-2026 Works Applications Co., Ltd.
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

import com.worksap.nlp.sudachi.Tokenizer.SplitMode;
import com.worksap.nlp.test.InMemoryDictionary;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.tests.analysis.BaseTokenStreamTestCase;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TestSudachiPartOfSpeechStopFilter extends BaseTokenStreamTestCase {
    TokenStream tokenStream;
    SudachiPartOfSpeechStopFilterFactory factory;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        InMemoryDictionary imd = new InMemoryDictionary();

        tokenStream = imd.tokenizer("東京都に行った。", true, SplitMode.A);
        Map<String, String> args = new HashMap<>();
        args.put("tags", "stoptags.txt");
        factory = new SudachiPartOfSpeechStopFilterFactory(args);
    }

    void assertFilteredContents(String tags, String[] expected) throws IOException {
        factory.inform(new StringResourceLoader(tags));
        tokenStream = factory.create(tokenStream);
        assertTokenStreamContents(tokenStream, expected);
    }

    @Test
    public void testAllPOS() throws IOException {
        assertFilteredContents("動詞,非自立可能\n名詞,固有名詞,地名,一般\n", new String[] { "都", "に", "た" });
    }

    @Test
    public void testPrefix() throws IOException {
        assertFilteredContents("動詞\n名詞,固有名詞\n", new String[] { "都", "に", "た" });
    }

    @Test
    public void testConjugationType() throws IOException {
        assertFilteredContents("*,*,*,*,五段-カ行\n", new String[] { "東京", "都", "に", "た" });
    }

    @Test
    public void testConjugationTypeAndForm() throws IOException {
        assertFilteredContents("*,*,*,*,五段-カ行,終止形-一般\n", new String[] { "東京", "都", "に", "行っ", "た" });
    }

    @Test
    public void testConjugationForm() throws IOException {
        assertFilteredContents("*,*,*,*,*,終止形-一般\n", new String[] { "東京", "都", "に", "行っ" });
    }

    @Test
    public void testPrefixWithUnmatchedSubcategory() throws IOException {
        assertFilteredContents("助詞,格助詞\n助詞,格助詞,引用\n", new String[] { "東京", "都", "行っ", "た" });
    }

    @Test
    public void testTooLongCategory() throws IOException {
        assertFilteredContents("名詞,固有名詞,地名,一般,一般\n", new String[] { "東京", "都", "に", "行っ", "た" });
    }

}
