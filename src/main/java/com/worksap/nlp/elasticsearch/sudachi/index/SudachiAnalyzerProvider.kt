/*
 * Copyright (c) 2017-2022 Works Applications Co., Ltd.
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

package com.worksap.nlp.elasticsearch.sudachi.index

import com.worksap.nlp.elasticsearch.sudachi.ConfigAdapter
import com.worksap.nlp.elasticsearch.sudachi.plugin.AnalysisCacheService
import com.worksap.nlp.elasticsearch.sudachi.plugin.DictionaryService
import com.worksap.nlp.lucene.sudachi.ja.SudachiAnalyzer
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.CharArraySet
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.env.Environment
import org.elasticsearch.index.IndexSettings
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider
import org.elasticsearch.index.analysis.Analysis
import org.elasticsearch.index.analysis.AnalyzerProvider
import org.elasticsearch.indices.analysis.AnalysisModule.AnalysisProvider

class SudachiAnalyzerProvider(
    analysisCache: AnalysisCacheService,
    dictionaryService: DictionaryService,
    indexSettings: IndexSettings,
    env: Environment?,
    name: String?,
    settings: Settings?
) : AbstractIndexAnalyzerProvider<SudachiAnalyzer>(indexSettings, name, settings) {
  private val analyzer: SudachiAnalyzer

  init {
    val stopWords: Set<*> =
        Analysis.parseStopWords(env, settings, SudachiAnalyzer.getDefaultStopSet(), false)
    val configs = ConfigAdapter(indexSettings, name!!, settings!!, env!!)
    val dictionary = dictionaryService.forConfig(configs.compiled)
    val cache = analysisCache.analysisCache(indexSettings.index.name, configs.mode, settings)
    analyzer =
        SudachiAnalyzer(
            dictionary,
            cache,
            configs.discardPunctuation,
            configs.mode,
            CharArraySet.copy(stopWords),
            SudachiAnalyzer.getDefaultStopTags(),
        )
  }

  override fun get(): SudachiAnalyzer {
    return analyzer
  }

  companion object {
    @JvmStatic
    fun maker(
        dictionaryService: DictionaryService,
        cacheService: AnalysisCacheService
    ): AnalysisProvider<AnalyzerProvider<out Analyzer?>> {
      return AnalysisProvider { a, b, c, d ->
        SudachiAnalyzerProvider(
            cacheService,
            dictionaryService,
            a,
            b,
            c,
            d,
        )
      }
    }
  }
}
