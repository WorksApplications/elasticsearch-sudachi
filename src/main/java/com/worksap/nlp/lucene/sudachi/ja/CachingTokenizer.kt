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

package com.worksap.nlp.lucene.sudachi.ja

import com.worksap.nlp.elasticsearch.sudachi.plugin.ReloadableDictionary
import com.worksap.nlp.elasticsearch.sudachi.plugin.ReloadableTokenizer
import com.worksap.nlp.lucene.sudachi.ja.util.AnalysisCache
import com.worksap.nlp.lucene.sudachi.ja.util.AnalysisCacheStats
import com.worksap.nlp.sudachi.Tokenizer.SplitMode
import java.io.Reader

/** Objective of this class is to combine reloadable tokenizer with analysis cache. */
class CachingTokenizer(
    val tokenizer: ReloadableTokenizer,
    private val splitMode: SplitMode,
    private val cache: AnalysisCache
) {
  fun tokenize(input: Reader): MorphemeIterator {
    return cache.analyze(tokenizer.get(), splitMode, input)
  }

  val dictionary: ReloadableDictionary
    get() = tokenizer.dictionary

  fun cacheStats(): AnalysisCacheStats = cache.stats()
}
