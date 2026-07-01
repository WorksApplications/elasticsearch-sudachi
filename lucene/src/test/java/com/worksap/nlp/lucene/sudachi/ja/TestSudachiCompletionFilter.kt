/*
 * Copyright (c) 2022-2026 Works Applications Co., Ltd.
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

import com.worksap.nlp.test.InMemoryDictionary
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.tests.analysis.BaseTokenStreamTestCase
import org.junit.Test

class TestSudachiCompletionFilter : BaseTokenStreamTestCase() {
  private val dic = InMemoryDictionary()

  @Test
  fun testCompletionIndex1() {
    val tokenStream = setUpTokenStream("index", "東京都に行った。")
    assertTokenStreamContents(
        tokenStream,
        arrayOf("東京都", "toukyouto", "に", "ni", "行っ", "iltu", "た", "ta"),
        intArrayOf(0, 0, 3, 3, 4, 4, 6, 6),
        intArrayOf(3, 3, 4, 4, 6, 6, 7, 7),
        intArrayOf(1, 0, 1, 0, 1, 0, 1, 0),
    )
  }

  @Test
  fun testCompletionIndex2() {
    val tokenStream = setUpTokenStream("index", "東京都")
    assertTokenStreamContents(
        tokenStream,
        arrayOf("東京都", "toukyouto"),
        intArrayOf(0, 0),
        intArrayOf(3, 3),
        intArrayOf(1, 0),
    )
  }

  @Test
  fun testCompletionIndex3() {
    val tokenStream = setUpTokenStream("index", "あ")
    assertTokenStreamContents(
        tokenStream,
        arrayOf("あ", "a"),
        intArrayOf(0, 0),
        intArrayOf(1, 1),
        intArrayOf(1, 0),
    )
  }

  @Test
  fun testCompletionIndex4() {
    val tokenStream = setUpTokenStream("index", "東京都h")
    assertTokenStreamContents(
        tokenStream,
        arrayOf("東京都", "toukyouto", "h"),
        intArrayOf(
            0,
            0,
            3,
        ),
        intArrayOf(3, 3, 4),
        intArrayOf(1, 0, 1),
    )
  }

  @Test
  fun testCompletionIndex5() {
    val tokenStream = setUpTokenStream("index", "ち。知")
    assertTokenStreamContents(
        tokenStream,
        arrayOf("ち", "ti", "知"),
        intArrayOf(
            0,
            0,
            2,
        ),
        intArrayOf(1, 1, 3),
        intArrayOf(1, 0, 1),
    )
  }

  @Test
  fun testCompletionQuery1() {
    val tokenStream = setUpTokenStream("query", "東京都に行った。")
    assertTokenStreamContents(
        tokenStream,
        arrayOf("東京都", "toukyouto", "に", "ni", "行っ", "iltu", "た", "ta"),
        intArrayOf(0, 0, 3, 3, 4, 4, 6, 6),
        intArrayOf(3, 3, 4, 4, 6, 6, 7, 7),
        intArrayOf(1, 0, 1, 0, 1, 0, 1, 0),
    )
  }

  @Test
  fun testCompletionQuery2() {
    val tokenStream = setUpTokenStream("query", "東京都")
    assertTokenStreamContents(
        tokenStream,
        arrayOf("東京都", "toukyouto"),
        intArrayOf(0, 0),
        intArrayOf(3, 3),
        intArrayOf(1, 0),
    )
  }

  @Test
  fun testCompletionQuery3() {
    val tokenStream = setUpTokenStream("query", "どらどら")
    assertTokenStreamContents(
        tokenStream,
        arrayOf("どらどら", "doradora"),
        intArrayOf(0, 0),
        intArrayOf(4, 4),
        intArrayOf(1, 0),
    )
  }

  @Test
  fun testCompletionQuery4() {
    val tokenStream = setUpTokenStream("query", "さんこうｓ")
    assertTokenStreamContents(
        tokenStream,
        arrayOf("さんこうｓ", "sankous"),
        intArrayOf(0, 0),
        intArrayOf(5, 5),
        intArrayOf(1, 0),
    )
  }

  @Test
  fun testCompletionQueryKanaConcat() {
    val tokenStream = setUpTokenStream("query", "アイアイウ")
    assertTokenStreamContents(
        tokenStream,
        arrayOf("アイアイウ", "aiaiu"),
        intArrayOf(0, 0),
        intArrayOf(5, 5),
        intArrayOf(1, 0),
    )
  }

  @Test
  fun testCompletionIndexKanjiNoReading() {
    val tokenStream = setUpTokenStream("index", "知")
    assertTokenStreamContents(
        tokenStream,
        arrayOf("知"),
        intArrayOf(0),
        intArrayOf(1),
        intArrayOf(1),
    )
  }

  fun setUpTokenStream(mode: String, input: String): TokenStream {
    val factory =
        SudachiCompletionFilterFactory(
            mutableMapOf("mode" to mode),
        )
    val stream = dic.tokenizer(input)
    return factory.create(stream)
  }
}
