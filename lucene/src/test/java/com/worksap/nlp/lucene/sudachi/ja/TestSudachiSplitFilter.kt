/*
 * Copyright (c) 2022-2024 Works Applications Co., Ltd.
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
import java.io.IOException
import org.apache.lucene.analysis.TokenStream
import org.junit.Test

class TestSudachiSplitFilter : BaseTokenStreamTestCase() {
  private val dic = InMemoryDictionary()

  @Test
  fun testSearchMode() {
    val tokenStream = setUpTokenStream("search", "東京都に行った。")
    assertTokenStreamContents(
        tokenStream,
        arrayOf("東京都", "東京", "都", "に", "行っ", "た"),
        intArrayOf(0, 0, 2, 3, 4, 6),
        intArrayOf(3, 2, 3, 4, 6, 7),
        intArrayOf(1, 0, 1, 1, 1, 1),
        intArrayOf(2, 1, 1, 1, 1, 1),
        8,
    )
  }

  @Test
  fun testExtendedMode() {
    val tokenStream = setUpTokenStream("extended", "東京都に行った。")
    assertTokenStreamContents(
        tokenStream,
        arrayOf("東京都", "東京", "都", "に", "行っ", "た"),
        intArrayOf(0, 0, 2, 3, 4, 6),
        intArrayOf(3, 2, 3, 4, 6, 7),
        intArrayOf(1, 0, 1, 1, 1, 1),
        intArrayOf(2, 1, 1, 1, 1, 1),
        8,
    )
  }

  @Test
  fun testWithPunctuationsBySearchMode() {
    val tokenStream = setUpTokenStream("search", "東京都に行った。東京都に行った。")
    assertTokenStreamContents(
        tokenStream,
        arrayOf("東京都", "東京", "都", "に", "行っ", "た", "東京都", "東京", "都", "に", "行っ", "た"),
        intArrayOf(0, 0, 2, 3, 4, 6, 8, 8, 10, 11, 12, 14),
        intArrayOf(3, 2, 3, 4, 6, 7, 11, 10, 11, 12, 14, 15),
        intArrayOf(1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1),
        intArrayOf(2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1),
        16,
    )
  }

  @Test
  fun testWithPunctuationsByExtendedMode() {
    val tokenStream = setUpTokenStream("extended", "東京都に行った。東京都に行った。")
    assertTokenStreamContents(
        tokenStream,
        arrayOf("東京都", "東京", "都", "に", "行っ", "た", "東京都", "東京", "都", "に", "行っ", "た"),
        intArrayOf(0, 0, 2, 3, 4, 6, 8, 8, 10, 11, 12, 14),
        intArrayOf(3, 2, 3, 4, 6, 7, 11, 10, 11, 12, 14, 15),
        intArrayOf(1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1),
        intArrayOf(2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1),
        16,
    )
  }

  @Test
  fun testWithOOVBySearchMode() {
    val tokenStream = setUpTokenStream("search", "アマゾンに行った。")
    assertTokenStreamContents(
        tokenStream,
        arrayOf("アマゾン", "に", "行っ", "た"),
        intArrayOf(0, 4, 5, 7),
        intArrayOf(4, 5, 7, 8),
        intArrayOf(1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1),
        9,
    )
  }

  @Test
  fun testWithOOVByExtendedMode() {
    val tokenStream = setUpTokenStream("extended", "アマゾンに行った。")
    assertTokenStreamContents(
        tokenStream,
        arrayOf("アマゾン", "ア", "マ", "ゾ", "ン", "に", "行っ", "た"),
        intArrayOf(0, 0, 1, 2, 3, 4, 5, 7),
        intArrayOf(4, 1, 2, 3, 4, 5, 7, 8),
        intArrayOf(1, 0, 1, 1, 1, 1, 1, 1),
        intArrayOf(4, 1, 1, 1, 1, 1, 1, 1),
        9,
    )
  }

  @Test
  fun testWithSingleCharOOVBySearchMode() {
    val tokenStream = setUpTokenStream("search", "あ")
    assertTokenStreamContents(
        tokenStream,
        arrayOf("あ"),
        intArrayOf(0),
        intArrayOf(1),
        intArrayOf(1),
        intArrayOf(1),
        1,
    )
  }

  @Test
  fun testWithSingleCharOOVByExtendedMode() {
    val tokenStream = setUpTokenStream("extended", "あ")
    assertTokenStreamContents(
        tokenStream,
        arrayOf("あ"),
        intArrayOf(0),
        intArrayOf(1),
        intArrayOf(1),
        intArrayOf(1),
        1,
    )
  }

  @Test
  @Throws(IOException::class)
  fun testWithOOVSequenceBySearchMode() {
    val tokenStream = setUpTokenStream("search", "アマゾンにワニ")
    assertTokenStreamContents(
        tokenStream,
        arrayOf("アマゾン", "に", "ワニ"),
        intArrayOf(0, 4, 5),
        intArrayOf(4, 5, 7),
        intArrayOf(1, 1, 1),
        intArrayOf(1, 1, 1),
        7,
    )
  }

  @Test
  @Throws(IOException::class)
  fun testWithOOVSequenceByExtendedMode() {
    val tokenStream = setUpTokenStream("extended", "アマゾンにワニ")
    assertTokenStreamContents(
        tokenStream,
        arrayOf("アマゾン", "ア", "マ", "ゾ", "ン", "に", "ワニ", "ワ", "ニ"),
        intArrayOf(0, 0, 1, 2, 3, 4, 5, 5, 6),
        intArrayOf(4, 1, 2, 3, 4, 5, 7, 6, 7),
        intArrayOf(1, 0, 1, 1, 1, 1, 1, 0, 1),
        intArrayOf(4, 1, 1, 1, 1, 1, 2, 1, 1),
        7,
    )
  }

  @Test
  @Throws(IOException::class)
  fun testWithSurrogateCharOOVByExtendedMode() {
    val tokenStream = setUpTokenStream("extended", "𠮟")
    assertTokenStreamContents(
        tokenStream,
        arrayOf("𠮟"),
        intArrayOf(0),
        intArrayOf(2),
        intArrayOf(1),
        intArrayOf(1),
        2,
    )
  }

  @Test
  fun testWithSurrogateStringOOVByExtendedMode() {
    val tokenStream = setUpTokenStream("extended", "𠮟る")
    assertTokenStreamContents(
        tokenStream,
        arrayOf("𠮟る", "𠮟", "る"),
        intArrayOf(0, 0, 2),
        intArrayOf(3, 2, 3),
        intArrayOf(1, 0, 1),
        intArrayOf(2, 1, 1),
        3,
    )
  }

  @Test
  fun testWithCharNormalizationBySearchMode() {
    val tokenStream = setUpTokenStream("search", "六三四㍿に行くカ゛カ゛カ゛")
    assertTokenStreamContents(
        tokenStream,
        arrayOf("六三四", "㍿", "㍿", "㍿", "に", "行く", "カ゛カ゛カ゛", "カ゛カ゛", "カ゛"),
        intArrayOf(0, 3, 3, 3, 4, 5, 7, 7, 11),
        intArrayOf(3, 4, 4, 4, 5, 7, 13, 11, 13),
        intArrayOf(1, 1, 0, 1, 1, 1, 1, 0, 1),
        intArrayOf(1, 2, 1, 1, 1, 1, 2, 1, 1),
        13,
    )
  }

  @Test
  fun testWithCharNormalizationInNormalizedFormBySearchMode() {
    var tokenStream = setUpTokenStream("search", "六三四㍿に行くカ゛カ゛カ゛")
    val normFactory = SudachiNormalizedFormFilterFactory(mutableMapOf())
    tokenStream = normFactory.create(tokenStream)

    assertTokenStreamContents(
        tokenStream,
        arrayOf("六三四", "株式会社", "株式", "会社", "に", "行く", "ガガガ", "ガガ", "ガ"),
        intArrayOf(0, 3, 3, 3, 4, 5, 7, 7, 11),
        intArrayOf(3, 4, 4, 4, 5, 7, 13, 11, 13),
        intArrayOf(1, 1, 0, 1, 1, 1, 1, 0, 1),
        intArrayOf(1, 2, 1, 1, 1, 1, 2, 1, 1),
        13,
    )
  }

  @Test
  fun testWithCharNormalizationByExtendedMode() {
    // extending normalized form seems more natural, but we cannot calculate their offsets.
    val tokenStream = setUpTokenStream("extended", "10㌢㍍いったソ゛")
    assertTokenStreamContents(
        tokenStream,
        arrayOf("1", "0", "㌢㍍", "㌢", "㍍", "いっ", "た", "ソ゛", "ソ", "゛"),
        intArrayOf(0, 1, 2, 2, 3, 4, 6, 7, 7, 8),
        intArrayOf(1, 2, 4, 3, 4, 6, 7, 9, 8, 9),
        intArrayOf(1, 1, 1, 0, 1, 1, 1, 1, 0, 1),
        intArrayOf(1, 1, 2, 1, 1, 1, 1, 2, 1, 1),
        9,
    )
  }

  @Test
  fun testWithCharNormalizationInNormalizedFormByExtendedMode() {
    // extending normalized form seems more natural, but we cannot calculate their offsets.
    var tokenStream = setUpTokenStream("extended", "10㌢㍍いったソ゛")
    val normFactory = SudachiNormalizedFormFilterFactory(mutableMapOf())
    tokenStream = normFactory.create(tokenStream)

    assertTokenStreamContents(
        tokenStream,
        arrayOf("1", "0", "センチメートル", "㌢", "㍍", "行く", "た", "ゾ", "ソ", "゛"),
        intArrayOf(0, 1, 2, 2, 3, 4, 6, 7, 7, 8),
        intArrayOf(1, 2, 4, 3, 4, 6, 7, 9, 8, 9),
        intArrayOf(1, 1, 1, 0, 1, 1, 1, 1, 0, 1),
        intArrayOf(1, 1, 2, 1, 1, 1, 1, 2, 1, 1),
        9)
  }

  fun setUpTokenStream(mode: String, input: String): TokenStream {
    val factory =
        SudachiSplitFilterFactory(
            mutableMapOf("mode" to mode),
        )
    val stream = dic.tokenizer(input)
    return factory.create(stream)
  }
}
