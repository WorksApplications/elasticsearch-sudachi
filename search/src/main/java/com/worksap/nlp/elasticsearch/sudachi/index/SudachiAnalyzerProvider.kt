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

package com.worksap.nlp.elasticsearch.sudachi.index

import com.worksap.nlp.elasticsearch.sudachi.ConfigAdapter
import com.worksap.nlp.lucene.sudachi.ja.SudachiAnalyzer
import com.worksap.nlp.lucene.sudachi.ja.plugin.AnalysisCacheService
import com.worksap.nlp.lucene.sudachi.ja.plugin.DictionaryService
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.CharArraySet
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.env.Environment
import org.elasticsearch.index.IndexSettings
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider
import org.elasticsearch.index.analysis.Analysis.parseStopWords
import org.elasticsearch.index.analysis.AnalyzerProvider
import org.elasticsearch.indices.analysis.AnalysisModule.AnalysisProvider

class SudachiAnalyzerProvider(
    cacheService: AnalysisCacheService,
    dictionaryService: DictionaryService,
    indexSettings: IndexSettings,
    env: Environment,
    name: String,
    settings: Settings,
) : AbstractIndexAnalyzerProvider<SudachiAnalyzer>(name) {

  private val stopWords: Set<*> by lazy {
    parseStopWords(env, settings, SudachiAnalyzer.getDefaultStopSet(), false)
  }
  private val configs by lazy { ConfigAdapter(dictionaryService.anchor, settings, env) }
  private val dictionary by lazy { dictionaryService.forConfig(configs.compiled) }
  private val cache by lazy {
    cacheService.analysisCache(
        indexSettings.index.name,
        configs.compiled,
        configs.mode,
        settings.getAsInt(AnalysisCacheService.CACHE_SIZE_SETTING_KEY, null),
        settings.getAsInt(AnalysisCacheService.MAX_INPUT_SETTING_KEY, null),
    )
  }

  override fun get(): SudachiAnalyzer {
    return SudachiAnalyzer(
        dictionary,
        cache,
        configs.discardPunctuation,
        configs.mode,
        CharArraySet.copy(stopWords),
        SudachiAnalyzer.getDefaultStopTags(),
    )
  }

  companion object {
    @JvmStatic
    fun maker(
        dictionaryService: DictionaryService,
        cacheService: AnalysisCacheService,
    ): AnalysisProvider<AnalyzerProvider<out Analyzer?>> {
      return AnalysisProvider { indexSettings, environment, name, settings ->
        SudachiAnalyzerProvider(
            cacheService,
            dictionaryService,
            indexSettings,
            environment,
            name,
            settings,
        )
      }
    }
  }
}
