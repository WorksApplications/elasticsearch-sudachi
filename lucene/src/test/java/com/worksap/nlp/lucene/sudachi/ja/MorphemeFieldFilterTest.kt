/*
 * Copyright (c) 2024 Works Applications Co., Ltd.
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
import com.worksap.nlp.sudachi.Morpheme
import com.worksap.nlp.test.InMemoryDictionary
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.miscellaneous.KeywordMarkerFilterFactory
import org.junit.Test

class TestMorphemeFieldFilter : BaseTokenStreamTestCase() {
  private val dic = InMemoryDictionary()

  @Test
  fun defaultValueFun() {
    var tokenStream: TokenStream = dic.tokenizer("東京都に行った。")
    tokenStream = SurfaceFilter(tokenStream)
    assertTokenStreamContents(tokenStream, arrayOf("東京都", "に", "行っ", "た"))
  }

  @Test
  fun nullValueFun() {
    var tokenStream: TokenStream = dic.tokenizer("東京都に行った。")
    tokenStream = NullFilter(tokenStream)
    assertTokenStreamContents(tokenStream, arrayOf("東京都", "に", "行っ", "た"))
  }

  @Test
  fun withKeyword() {
    val kwFactory = KeywordMarkerFilterFactory(mutableMapOf("pattern" to "東京都"))
    var tokenStream: TokenStream = dic.tokenizer("東京都に行った。")
    tokenStream = SurfaceFilter(kwFactory.create(tokenStream))
    assertTokenStreamContents(tokenStream, arrayOf("東京都", "に", "行っ", "た"))
  }
}

class SurfaceFilter(input: TokenStream) : MorphemeFieldFilter(input) {}

class NullFilter(input: TokenStream) : MorphemeFieldFilter(input) {
  override fun value(m: Morpheme): String? = null
}
