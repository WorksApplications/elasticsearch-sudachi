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

package com.worksap.nlp.elasticsearch.sudachi.plugin

import com.worksap.nlp.lucene.sudachi.ja.input.InputExtractor
import com.worksap.nlp.lucene.sudachi.ja.util.AnalysisCache
import com.worksap.nlp.sudachi.Tokenizer.SplitMode
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import org.apache.logging.log4j.LogManager
import org.elasticsearch.common.settings.Settings

class AnalysisCacheService {
  data class Key(val name: String)
  // we use WeakReference here because the main reference will reside in per-index factories
  private val caches = ConcurrentHashMap<Key, WeakReference<AnalysisCache>>()

  companion object {
    private val logger = LogManager.getLogger(AnalysisCacheService::class.java)
  }

  fun analysisCache(indexName: String, mode: SplitMode, settings: Settings): AnalysisCache {
    val key = Key(indexName)
    val entry =
        caches.computeIfAbsent(key) {
          val capacity = settings.getAsInt("cache-size", 32)
          val extractor = InputExtractor.make(settings)
          logger.debug(
              "creating new cache service for {}, size={}, extractor={}", key, capacity, extractor)
          val x = AnalysisCache(capacity, extractor)
          WeakReference(x)
        }
    val result = entry.get()
    if (result == null) {
      caches.remove(key)
      // retry creation via recursion
      return analysisCache(indexName, mode, settings)
    }
    return result
  }
}
