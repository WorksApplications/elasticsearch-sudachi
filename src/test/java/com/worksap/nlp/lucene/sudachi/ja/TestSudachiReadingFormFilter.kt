/*
 * Copyright (c) 2019-2023 Works Applications Co., Ltd.
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

package com.worksap.nlp.lucene.sudachi.ja

import com.worksap.nlp.lucene.sudachi.aliases.BaseTokenStreamTestCase
import com.worksap.nlp.test.InMemoryDictionary
import org.apache.lucene.analysis.Tokenizer
import org.junit.Test

class TestSudachiReadingFormFilter : BaseTokenStreamTestCase() {
  private val dic = InMemoryDictionary()

  @Test
  fun testReadingForm() {
    val factory = SudachiReadingFormFilterFactory(mutableMapOf())
    val tokenizer: Tokenizer = dic.tokenizer("東京都に行った。")
    val tokenStream = factory.create(tokenizer)
    assertTokenStreamContents(tokenStream, arrayOf("トウキョウト", "ニ", "イッ", "タ"))
  }

  @Test
  fun testRomanizedReadingForm() {
    val factory = SudachiReadingFormFilterFactory(mutableMapOf("useRomaji" to "true"))
    val tokenizer: Tokenizer = dic.tokenizer("東京都に行った。")
    val tokenStream = factory.create(tokenizer)
    assertTokenStreamContents(tokenStream, arrayOf("toukyouto", "ni", "iltu", "ta"))
  }
}
