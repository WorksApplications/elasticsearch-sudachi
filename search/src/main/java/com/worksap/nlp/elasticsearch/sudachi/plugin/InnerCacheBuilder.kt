/*
 * Copyright (c) 2026 Works Applications Co., Ltd.
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

package com.worksap.nlp.elasticsearch.sudachi.plugin

import com.worksap.nlp.lucene.sudachi.ja.plugin.AnalysisCache
import com.worksap.nlp.lucene.sudachi.ja.plugin.AnalysisCacheStats
import com.worksap.nlp.lucene.sudachi.ja.plugin.InnerCache
import com.worksap.nlp.lucene.sudachi.ja.plugin.InnerCacheBuilder
import com.worksap.nlp.search.aliases.Cache
import com.worksap.nlp.search.aliases.CacheBuilder
import com.worksap.nlp.sudachi.MorphemeList

class InnerCacheBuilderImpl : InnerCacheBuilder {
  override fun build(capacity: Int): InnerCacheImpl {
    val cache =
        CacheBuilder.builder<String, MorphemeList>()
            .setMaximumWeight(AnalysisCache.calculateMaxWeight(capacity))
            .weigher(AnalysisCache.weigher)
            .build()

    return InnerCacheImpl(cache, capacity)
  }
}

class InnerCacheImpl(
    private val cache: Cache<String, MorphemeList>,
    override public val capacity: Int,
) : InnerCache {
  override fun computeIfAbsent(
      input: String,
      lazyTokenize: (String) -> MorphemeList
  ): MorphemeList {
    return cache.computeIfAbsent(input, lazyTokenize)
  }

  override fun stats(): AnalysisCacheStats {
    val stats = cache.stats()
    return AnalysisCacheStats(hits = stats.hits, misses = stats.misses, evictions = stats.evictions)
  }
}
