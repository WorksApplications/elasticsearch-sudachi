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

package com.worksap.nlp.lucene.sudachi.ja

import com.worksap.nlp.lucene.sudachi.aliases.BaseTokenStreamTestCase
import com.worksap.nlp.test.InMemoryDictionary
import org.apache.lucene.analysis.Tokenizer
import org.junit.Test

class TestSudachiPartOfSpeechStopFilterFactory : BaseTokenStreamTestCase() {
  private val dictionary = InMemoryDictionary()
  @Test
  fun testBasics() {
    val tags = "動詞,非自立可能\n"
    val tokenizer: Tokenizer = dictionary.tokenizer("東京都に行った。")
    val factory = SudachiPartOfSpeechStopFilterFactory(mutableMapOf("tags" to "stoptags.txt"))
    factory.inform(StringResourceLoader(tags))
    val ts = factory.create(tokenizer)
    assertTokenStreamContents(ts, arrayOf("東京都", "に", "た"))
  }

  @Test
  fun testBogusArguments() {
    val expected =
        expectThrows(IllegalArgumentException::class.java) {
          SudachiPartOfSpeechStopFilterFactory(mutableMapOf("foo" to "bar"))
        }
    assertTrue(expected.message!!.contains("Unknown parameters"))
  }
}
