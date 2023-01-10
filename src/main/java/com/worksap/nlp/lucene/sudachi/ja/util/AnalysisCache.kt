/*
 * Copyright (c) 2022-2023 Works Applications Co., Ltd.
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
import org.elasticsearch.common.cache.CacheBuilder

/**
 * Cache analysis results using ES-based cache logic.
 *
 * Analysis are always done in C mode and cached. Splitting is done after caching on-demand as it is
 * a relatively cheap operation.
 */
class AnalysisCache(private val capacity: Int, private val extractor: InputExtractor) {
  private val cache =
      CacheBuilder.builder<String, MorphemeList>()
          .setMaximumWeight(capacity * 64 * 1024L)
          .weigher { i, ml -> i.length * 4L + ml.size * 64L }
          .build()

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
    val list = cache.computeIfAbsent(input) { k -> tokenizer.tokenize(SplitMode.C, k) }
    return CachedAnalysis(list.split(mode))
  }

  fun stats(): AnalysisCacheStats {
    val stats = cache.stats()
    return AnalysisCacheStats(hits = stats.hits, misses = stats.misses, evictions = stats.evictions)
  }
}

data class AnalysisCacheStats(val hits: Long, val misses: Long, val evictions: Long)
