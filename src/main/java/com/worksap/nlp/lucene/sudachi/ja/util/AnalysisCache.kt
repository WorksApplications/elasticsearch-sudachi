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

import com.worksap.nlp.lucene.sudachi.ja.CachedAnalysis
import com.worksap.nlp.lucene.sudachi.ja.MorphemeIterator
import com.worksap.nlp.lucene.sudachi.ja.NonCachedAnalysis
import com.worksap.nlp.lucene.sudachi.ja.input.ConcatenatingReader
import com.worksap.nlp.lucene.sudachi.ja.input.InputExtractor
import com.worksap.nlp.sudachi.MorphemeList
import com.worksap.nlp.sudachi.Tokenizer
import com.worksap.nlp.sudachi.Tokenizer.SplitMode
import java.io.Reader
import java.util.concurrent.ConcurrentHashMap

/**
 * This cache implements pseudo-LRU strategy with two hashmaps. Caches analysis results when
 * [InputExtractor] allows it and [capacity] `> 0`.
 *
 * This is thread safe and a single instance for an index should be created.
 */
class AnalysisCache(private val capacity: Int, private val extractor: InputExtractor) {
  @Volatile private var main = ConcurrentHashMap<String, MorphemeList>(capacity)
  @Volatile private var fallback = ConcurrentHashMap<String, MorphemeList>(capacity)

  private fun swap() {
    synchronized(this) {
      if (main.size >= capacity) {
        val main1 = main
        val fallback1 = fallback
        main = fallback1
        fallback = main1
        fallback1.clear()
      }
    }
  }

  /** Use [com.worksap.nlp.lucene.sudachi.ja.CachingTokenizer.tokenize] instead of this method. */
  internal fun analyze(tokenizer: Tokenizer, mode: SplitMode, input: Reader): MorphemeIterator {
    if (capacity <= 0) {
      return NonCachedAnalysis(tokenizer, input, mode)
    }
    if (extractor.canExtract(input)) {
      val extracted = extractor.extract(input)
      if (extracted.remaining) {
        if (extracted.data.isEmpty()) {
          return NonCachedAnalysis(tokenizer, input, mode)
        }
        val reader = ConcatenatingReader(extracted.data, input)
        return NonCachedAnalysis(tokenizer, reader, mode)
      } else {
        return cached(extracted.data, mode, tokenizer)
      }
    } else {
      return NonCachedAnalysis(tokenizer, input, mode)
    }
  }

  private fun cached(input: String, mode: SplitMode, tokenizer: Tokenizer): MorphemeIterator {
    if (main.size >= capacity) {
      swap()
    }
    val list =
        main.computeIfAbsent(input) { k -> fallback[k] ?: tokenizer.tokenize(SplitMode.C, input) }
    return CachedAnalysis(list.split(mode))
  }

  val mainSize: Int
    get() = main.size
}
