/*
 * Copyright (c) 2022 Works Applications Co., Ltd.
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

package com.worksap.nlp.lucene.sudachi.ja.util

import com.worksap.nlp.lucene.sudachi.ja.CachingTokenizer
import com.worksap.nlp.lucene.sudachi.ja.MorphemeIterator
import com.worksap.nlp.lucene.sudachi.ja.NonCachedAnalysis
import com.worksap.nlp.lucene.sudachi.ja.input.CopyingInputExtractor
import com.worksap.nlp.sudachi.Tokenizer.SplitMode
import com.worksap.nlp.test.InMemoryDictionary
import java.io.StringReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AnalysisCacheTest {
  private val dic = InMemoryDictionary()

  inner class TestAnalyzer(mode: SplitMode = SplitMode.C, capacity: Int = 4) {
    private val cache =
        CachingTokenizer(
            tokenizer = dic.dic.newTokenizer(),
            splitMode = mode,
            cache = AnalysisCache(capacity, CopyingInputExtractor(128)))
    fun analyze(data: String): MorphemeIterator {
      val reader = StringReader(data)
      return cache.tokenize(reader)
    }

    fun cacheSize(): Int = cache.cacheMainSize
  }

  @Test
  fun cached() {
    val ana = TestAnalyzer()
    ana.analyze("東京都")
    ana.analyze("東京都")
    assertEquals(1, ana.cacheSize())
  }

  @Test
  fun swapCache() {
    val ana = TestAnalyzer()
    ana.analyze("東京都1")
    ana.analyze("東京都2")
    ana.analyze("東京都3")
    ana.analyze("東京都4")
    ana.analyze("東京都1")
    assertEquals(1, ana.cacheSize())
  }

  @Test
  fun largeMorpheme() {
    val ana = TestAnalyzer()
    val morphs = ana.analyze("0123456789".repeat(30))
    assertIs<NonCachedAnalysis>(morphs)
    val m = morphs.next()!!
    assertEquals("0123456789".repeat(30), m.surface())
  }
}
