/*
 * Copyright (c) 2018-2023 Works Applications Co., Ltd.
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

import com.worksap.nlp.lucene.sudachi.aliases.BaseTokenStreamTestCase;
import com.worksap.nlp.test.InMemoryDictionary;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

public class TestSudachiNormalizedFormFilter extends BaseTokenStreamTestCase {
    SudachiNormalizedFormFilterFactory factory = new SudachiNormalizedFormFilterFactory(Collections.emptyMap());

    private final InMemoryDictionary dic = new InMemoryDictionary();

    @Test
    public void testNormalizedForm() throws IOException {
        Tokenizer tokenizer = dic.tokenizer("東京都に行った。");
        TokenStream stream = factory.create(tokenizer);
        assertTokenStreamContents(stream, new String[] { "東京都", "に", "行く", "た" });
    }

    @Test
    public void testNormalizedFormWithUnnormalizedWord() throws IOException {
        Tokenizer tokenizer = dic.tokenizer("東京都にいった。");
        TokenStream stream = factory.create(tokenizer);
        assertTokenStreamContents(stream, new String[] { "東京都", "に", "行く", "た" });
    }

}
