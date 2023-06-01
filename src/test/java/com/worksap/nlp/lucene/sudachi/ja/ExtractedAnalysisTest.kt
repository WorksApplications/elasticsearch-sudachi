/*
 * Copyright (c) 2023 Works Applications Co., Ltd.
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

import com.worksap.nlp.elasticsearch.sudachi.plugin.AnalysisCache
import com.worksap.nlp.lucene.sudachi.ja.input.CopyingInputExtractor
import com.worksap.nlp.lucene.sudachi.ja.input.NoopInputExtractor
import com.worksap.nlp.sudachi.Morpheme
import com.worksap.nlp.sudachi.Tokenizer
import com.worksap.nlp.test.InMemoryDictionary
import java.io.Reader
import java.io.StringReader
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BadReader(private val underlying: Reader, private val limit: Int) : Reader() {
  override fun read(cbuf: CharArray, off: Int, len: Int): Int {
    return underlying.read(cbuf, off, min(len, limit))
  }

  override fun close() {
    underlying.close()
  }
}

abstract class ExtractedAnalysisTest {
  interface TestAna {
    fun analyze(input: CharSequence): MorphemeIterator
  }

  abstract fun makeAnalyzer(): TestAna

  @Test
  fun analyze20ch() {
    val a = makeAnalyzer()
    val result = a.analyze("東京都に行く六三四。東京都に行く六三四。")
    result.assertTokens("東京都", "に", "行く", "六三四", "。")
    result.assertTokens("東京都", "に", "行く", "六三四", "。")
    assertNull(result.next())
  }

  @Test
  fun analyze150ch() {
    val a = makeAnalyzer()
    val input = "東京都に行く六三四。".repeat(15)
    val result = a.analyze(input)
    repeat(15) { result.assertTokens("東京都", "に", "行く", "六三四", "。") }
    assertNull(result.next())
  }
}

fun MorphemeIterator.assertTokens(vararg tokens: String) {
  var token: Morpheme? = null
  var cnt = 0
  while (cnt < tokens.size && next().also { token = it } != null) {
    val t = assertNotNull(token)
    assertEquals(tokens[cnt], t.surface(), "expected token [$cnt] $t == ${tokens[cnt]}")
    cnt += 1
  }
}

class Len95CachedAnalysisTest : ExtractedAnalysisTest() {
  private val dic = InMemoryDictionary()
  override fun makeAnalyzer(): TestAna {
    return object : TestAna {
      private val cache =
          CachingTokenizer(
              tokenizer = dic.dic.newTokenizer(),
              splitMode = Tokenizer.SplitMode.C,
              cache = AnalysisCache(4, CopyingInputExtractor(95)))
      override fun analyze(input: CharSequence): MorphemeIterator {
        return cache.tokenize(StringReader(input.toString()))
      }
    }
  }
}

class Len95CachedAnalysisTestBadInput : ExtractedAnalysisTest() {
  private val dic = InMemoryDictionary()
  override fun makeAnalyzer(): TestAna {
    return object : TestAna {
      private val cache =
          CachingTokenizer(
              tokenizer = dic.dic.newTokenizer(),
              splitMode = Tokenizer.SplitMode.C,
              cache = AnalysisCache(4, CopyingInputExtractor(95)))
      override fun analyze(input: CharSequence): MorphemeIterator {
        return cache.tokenize(BadReader(StringReader(input.toString()), 16))
      }
    }
  }
}

class NonCachedAnalysisTest : ExtractedAnalysisTest() {
  private val dic = InMemoryDictionary()
  override fun makeAnalyzer(): TestAna {
    return object : TestAna {
      private val cache =
          CachingTokenizer(
              tokenizer = dic.dic.newTokenizer(),
              splitMode = Tokenizer.SplitMode.C,
              cache = AnalysisCache(4, NoopInputExtractor.INSTANCE))
      override fun analyze(input: CharSequence): MorphemeIterator {
        return cache.tokenize(StringReader(input.toString()))
      }
    }
  }
}

class NonCachedBadReaderAnalysisTest : ExtractedAnalysisTest() {
  private val dic = InMemoryDictionary()
  override fun makeAnalyzer(): TestAna {
    return object : TestAna {
      private val cache =
          CachingTokenizer(
              tokenizer = dic.dic.newTokenizer(),
              splitMode = Tokenizer.SplitMode.C,
              cache = AnalysisCache(4, NoopInputExtractor.INSTANCE))
      override fun analyze(input: CharSequence): MorphemeIterator {
        return cache.tokenize(BadReader(StringReader(input.toString()), 16))
      }
    }
  }
}
