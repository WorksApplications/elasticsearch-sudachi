/*
 * Copyright (c) 2022-2026 Works Applications Co., Ltd.
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
import com.worksap.nlp.lucene.sudachi.ja.CachingTokenizer
import com.worksap.nlp.lucene.sudachi.ja.SudachiTokenizer
import com.worksap.nlp.lucene.sudachi.ja.plugin.AnalysisCacheService
import com.worksap.nlp.lucene.sudachi.ja.plugin.DictionaryService
import com.worksap.nlp.search.aliases.AbstractTokenizerFactory
import com.worksap.nlp.search.aliases.AnalysisProvider
import com.worksap.nlp.search.aliases.Environment
import com.worksap.nlp.search.aliases.IndexSettings
import com.worksap.nlp.search.aliases.Settings
import com.worksap.nlp.search.aliases.TokenizerFactory
import org.apache.lucene.analysis.Tokenizer

class SudachiTokenizerFactory(
    private val service: DictionaryService,
    private val caches: AnalysisCacheService,
    indexSettings: IndexSettings,
    private val env: Environment,
    name: String,
    settings: Settings
) : AbstractTokenizerFactory(indexSettings, env, name, settings) {

  companion object {
    @JvmStatic
    fun maker(
        service: DictionaryService,
        caches: AnalysisCacheService
    ): AnalysisProvider<TokenizerFactory> {
      return AnalysisProvider { indexSettings, environment, name, settings ->
        SudachiTokenizerFactory(service, caches, indexSettings, environment, name, settings)
      }
    }
  }

  private val config = ConfigAdapter(service.anchor, settings, env)
  private val mode = config.mode

  private val dictionary by lazy { service.forConfig(config.compiled) }

  private val cache by lazy {
    caches.analysisCache(
        indexSettings.index.name,
        config.compiled,
        mode,
        settings.getAsInt(AnalysisCacheService.CACHE_SIZE_SETTING_KEY, null),
        settings.getAsInt(AnalysisCacheService.MAX_INPUT_SETTING_KEY, null),
    )
  }

  override fun create(): Tokenizer {
    val tok = CachingTokenizer(dictionary.newTokenizer(), mode, cache)
    return SudachiTokenizer(tok, config.discardPunctuation)
  }
}
