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
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

public class TestSudachiBaseFormFilter extends BaseTokenStreamTestCase {
    InMemoryDictionary dic = new InMemoryDictionary();
    SudachiBaseFormFilterFactory factory = new SudachiBaseFormFilterFactory(Collections.emptyMap());

    @Test
    public void testBaseForm() throws IOException {
        TokenStream tokenStream = dic.tokenizer("東京都に行った。");
        tokenStream = factory.create(tokenStream);
        assertTokenStreamContents(tokenStream, new String[] { "東京都", "に", "行く", "た" });
    }

    @Test
    public void testBaseFormWithUnnormalizedWord() throws IOException {
        TokenStream tokenStream = dic.tokenizer("東京都にいった。");
        tokenStream = factory.create(tokenStream);
        assertTokenStreamContents(tokenStream, new String[] { "東京都", "に", "いく", "た" });
    }
}
