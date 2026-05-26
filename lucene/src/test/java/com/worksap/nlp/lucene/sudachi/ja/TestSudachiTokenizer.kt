/*
 * Copyright (c) 2017-2026 Works Applications Co., Ltd.
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

import com.worksap.nlp.lucene.sudachi.ja.input.CopyingInputExtractor
import com.worksap.nlp.lucene.sudachi.ja.input.NoopInputExtractor
import com.worksap.nlp.lucene.sudachi.ja.plugin.AnalysisCache
import com.worksap.nlp.lucene.sudachi.ja.plugin.ReloadableDictionary
import com.worksap.nlp.sudachi.Config
import com.worksap.nlp.sudachi.PathAnchor
import com.worksap.nlp.sudachi.Tokenizer.SplitMode
import com.worksap.nlp.test.InnerCacheBuilderImpl
import com.worksap.nlp.test.TestDictionary
import java.io.StringReader
import org.apache.lucene.analysis.charfilter.MappingCharFilter
import org.apache.lucene.analysis.charfilter.NormalizeCharMap
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute
import org.apache.lucene.tests.analysis.BaseTokenStreamTestCase
import org.apache.lucene.util.AttributeFactory
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// Test of character segmentation using incrementToken(tokenizer)
open class TestSudachiTokenizer : BaseTokenStreamTestCase() {
  @JvmField @Rule var testDic = TestDictionary("system")

  private lateinit var config: Config

  fun makeTokenizer(
      mode: SplitMode,
      noPunctuation: Boolean = true,
      allowEmptyMorpheme: Boolean = false,
      capacity: Int = 0,
  ): SudachiTokenizer {
    val dict = ReloadableDictionary(config.allowEmptyMorpheme(allowEmptyMorpheme))
    val extractor =
        if (capacity == 0) {
          NoopInputExtractor.INSTANCE
        } else {
          CopyingInputExtractor(Short.MAX_VALUE.toInt())
        }
    val tok =
        CachingTokenizer(
            dict.newTokenizer(),
            mode,
            AnalysisCache(InnerCacheBuilderImpl().build(capacity), extractor),
        )
    return SudachiTokenizer(tok, noPunctuation, AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY)
  }

  @Before
  fun setup() {
    val configDir = testDic.root.toPath().resolve("config/sudachi")
    config = Config.fromFile(configDir.resolve("sudachi.json"))
  }

  @Test
  fun incrementTokenWithShiftJis() {
    val sjis = charset("Shift_JIS")
    val str = String("東京都に行った。".toByteArray(sjis), sjis)
    val tokenizer = makeTokenizer(SplitMode.C)
    tokenizer.setReader(StringReader(str))
    assertTokenStreamContents(
        tokenizer,
        arrayOf("東京都", "に", "行っ", "た"),
        intArrayOf(0, 3, 4, 6),
        intArrayOf(3, 4, 6, 7),
        intArrayOf(1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1),
        8,
    )
  }

  @Test
  fun incrementTokenByDefaultMode() {
    val tokenizer = makeTokenizer(SplitMode.C)
    tokenizer.setReader(StringReader("東京都に行った。"))
    assertTokenStreamContents(
        tokenizer,
        arrayOf("東京都", "に", "行っ", "た"),
        intArrayOf(0, 3, 4, 6),
        intArrayOf(3, 4, 6, 7),
        intArrayOf(1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1),
        8,
    )
  }

  @Test
  fun incrementTokenByDefaultModeCached() {
    val tokenizer = makeTokenizer(SplitMode.C, capacity = 10)
    tokenizer.setReader(StringReader("東京都に行った。"))
    assertTokenStreamContents(
        tokenizer,
        arrayOf("東京都", "に", "行っ", "た"),
        intArrayOf(0, 3, 4, 6),
        intArrayOf(3, 4, 6, 7),
        intArrayOf(1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1),
        8,
    )
  }

  @Test
  fun incrementTokenByPunctuationMode() {
    val tokenizer = makeTokenizer(SplitMode.C, noPunctuation = false)
    tokenizer.setReader(StringReader("東京都に行った。"))
    assertTokenStreamContents(
        tokenizer,
        arrayOf("東京都", "に", "行っ", "た", "。"),
        intArrayOf(0, 3, 4, 6, 7),
        intArrayOf(3, 4, 6, 7, 8),
        intArrayOf(1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1),
        8,
    )
  }

  @Test
  fun incrementTokenWithPunctuationsByDefaultMode() {
    val tokenizer = makeTokenizer(SplitMode.C, noPunctuation = true)
    tokenizer.setReader(StringReader("東京都に行った。東京都に行った。"))
    assertTokenStreamContents(
        tokenizer,
        arrayOf("東京都", "に", "行っ", "た", "東京都", "に", "行っ", "た"),
        intArrayOf(0, 3, 4, 6, 8, 11, 12, 14),
        intArrayOf(3, 4, 6, 7, 11, 12, 14, 15),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
        16,
    )
  }

  @Test
  fun incrementTokenWithPunctuationsByPunctuationMode() {
    val tokenizer = makeTokenizer(SplitMode.C, noPunctuation = false)
    tokenizer.setReader(StringReader("東京都に行った。東京都に行った。"))
    assertTokenStreamContents(
        tokenizer,
        arrayOf("東京都", "に", "行っ", "た", "。", "東京都", "に", "行っ", "た", "。"),
        intArrayOf(0, 3, 4, 6, 7, 8, 11, 12, 14, 15),
        intArrayOf(3, 4, 6, 7, 8, 11, 12, 14, 15, 16),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        16,
    )
  }

  @Test
  fun incrementTokenWithPunctuationsByPunctuationModeCached() {
    val tokenizer = makeTokenizer(SplitMode.C, noPunctuation = false, capacity = 10)
    tokenizer.setReader(StringReader("東京都に行った。東京都に行った。"))
    assertTokenStreamContents(
        tokenizer,
        arrayOf("東京都", "に", "行っ", "た", "。", "東京都", "に", "行っ", "た", "。"),
        intArrayOf(0, 3, 4, 6, 7, 8, 11, 12, 14, 15),
        intArrayOf(3, 4, 6, 7, 8, 11, 12, 14, 15, 16),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        16,
    )
  }

  @Test
  fun incrementTokenWithOOVByDefaultMode() {
    val tokenizer = makeTokenizer(SplitMode.C)
    tokenizer.setReader(StringReader("アマゾンに行った。"))
    assertTokenStreamContents(
        tokenizer,
        arrayOf("アマゾン", "に", "行っ", "た"),
        intArrayOf(0, 4, 5, 7),
        intArrayOf(4, 5, 7, 8),
        intArrayOf(1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1),
        9,
    )
  }

  @Test
  fun incrementTokenWithOOVByPunctuationMode() {
    val tokenizerPunctuation = makeTokenizer(SplitMode.C, noPunctuation = false)
    tokenizerPunctuation.setReader(StringReader("アマゾンに行った。"))
    assertTokenStreamContents(
        tokenizerPunctuation,
        arrayOf("アマゾン", "に", "行っ", "た", "。"),
        intArrayOf(0, 4, 5, 7, 8),
        intArrayOf(4, 5, 7, 8, 9),
        intArrayOf(1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1),
        9,
    )
  }

  @Test
  fun incrementTokenByAMode() {
    val tokenizerA = makeTokenizer(SplitMode.A)
    tokenizerA.setReader(StringReader("東京都に行った。"))
    assertTokenStreamContents(
        tokenizerA,
        arrayOf("東京", "都", "に", "行っ", "た"),
        intArrayOf(0, 2, 3, 4, 6),
        intArrayOf(2, 3, 4, 6, 7),
        intArrayOf(1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1),
        8,
    )
  }

  @Test
  fun incrementTokenByBMode() {
    val tokenizerB = makeTokenizer(SplitMode.B)
    tokenizerB.setReader(StringReader("東京都に行った。"))
    assertTokenStreamContents(
        tokenizerB,
        arrayOf("東京都", "に", "行っ", "た"),
        intArrayOf(0, 3, 4, 6),
        intArrayOf(3, 4, 6, 7),
        intArrayOf(1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1),
        8,
    )
  }

  @Test
  fun incrementTokenWithCorrectOffset() {
    val builder = NormalizeCharMap.Builder()
    builder.add("東京都", "京都")
    val filter = MappingCharFilter(builder.build(), StringReader("東京都に行った。"))
    val tokenizer = makeTokenizer(SplitMode.C)
    tokenizer.setReader(filter)
    assertTokenStreamContents(
        tokenizer,
        arrayOf("京都", "に", "行っ", "た"),
        intArrayOf(0, 3, 4, 6),
        intArrayOf(3, 4, 6, 7),
        intArrayOf(1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1),
        8,
    )
  }

  @Test
  fun incrementTokenWithCorrectSplitOffset() {
    val builder = NormalizeCharMap.Builder()
    builder.add("（株）", "株式会社")
    val filter = MappingCharFilter(builder.build(), StringReader("（株）に行った。"))
    val tokenizer = makeTokenizer(SplitMode.A)
    tokenizer.setReader(filter)
    assertTokenStreamContents(
        tokenizer,
        arrayOf("株式", "会社", "に", "行っ", "た"),
        intArrayOf(0, 2, 3, 4, 6),
        intArrayOf(2, 3, 4, 6, 7),
        intArrayOf(1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1),
        8,
    )
  }

  @Test
  fun incrementTokenWithDisallowEmptyMorpheme() {
    val tokenizer = makeTokenizer(SplitMode.A, allowEmptyMorpheme = false)
    tokenizer.setReader(StringReader("㍿に行った。"))
    assertTokenStreamContents(
        tokenizer,
        arrayOf("㍿", "㍿", "に", "行っ", "た"),
        intArrayOf(0, 0, 1, 2, 4),
        intArrayOf(1, 1, 2, 4, 5),
        intArrayOf(1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1),
        6,
    )
  }

  @Test
  fun incrementTokenWithAllowEmptyMorpheme() {
    val tokenizer = makeTokenizer(SplitMode.A, allowEmptyMorpheme = true)
    tokenizer.setReader(StringReader("㍿に行った。"))
    assertTokenStreamContents(
        tokenizer,
        arrayOf("㍿", "", "に", "行っ", "た"),
        intArrayOf(0, 1, 1, 2, 4),
        intArrayOf(1, 1, 2, 4, 5),
        intArrayOf(1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1),
        6,
    )
  }

  @Test
  fun additionalSettings() {
    val tokenizer = makeTokenizer(SplitMode.C)
    tokenizer.setReader(StringReader("自然言語"))
    assertTokenStreamContents(
        tokenizer,
        arrayOf("自然言語"),
        intArrayOf(0),
        intArrayOf(4),
        intArrayOf(1),
        intArrayOf(1),
        4,
    )

    var anchor = PathAnchor.filesystem(testDic.root.toPath().resolve("config/sudachi"))
    anchor = anchor.andThen(PathAnchor.classpath())
    config =
        Config.fromClasspath(ResourceUtil::class.java.getResource("additional.json"), anchor)
            .withFallback(config)
    val tokenizer2 = makeTokenizer(SplitMode.C)
    tokenizer2.setReader(StringReader("自然言語"))
    assertTokenStreamContents(
        tokenizer2,
        arrayOf("自然", "言語"),
        intArrayOf(0, 2),
        intArrayOf(2, 4),
        intArrayOf(1, 1),
        intArrayOf(1, 1),
        4,
    )
  }

  @Test
  fun equalsHashCodeCoverage() {
    val tokenizerA = makeTokenizer(SplitMode.A)
    val tokenizerB = makeTokenizer(SplitMode.B)
    assertNotEquals(tokenizerA, tokenizerB)
    assertNotEquals(tokenizerA.hashCode().toLong(), tokenizerB.hashCode().toLong())
  }

  @Test
  fun hugeCharactersByDefaultMode() {
    val tokenizer = makeTokenizer(SplitMode.C)

    val charLength = 10 * 1024 * 1024
    tokenizer.setReader(StringReader("あ".repeat(charLength)))

    val charTermAttribute = tokenizer.getAttribute(CharTermAttribute::class.java)
    val offsetAttribute: OffsetAttribute = tokenizer.getAttribute(OffsetAttribute::class.java)

    tokenizer.reset()

    var totalLength = 0
    var prevEndOffset = 0
    while (tokenizer.incrementToken()) {
      Assert.assertEquals(prevEndOffset, offsetAttribute.startOffset())

      prevEndOffset = offsetAttribute.endOffset()
      totalLength += charTermAttribute.length
    }

    Assert.assertEquals(charLength, totalLength)
    Assert.assertEquals(charLength, prevEndOffset)
  }
}
