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

package com.worksap.nlp.elasticsearch.sudachi.index

import com.worksap.nlp.elasticsearch.sudachi.ConfigAdapter
import com.worksap.nlp.elasticsearch.sudachi.aliases.AbstractTokenizerFactory
import com.worksap.nlp.elasticsearch.sudachi.plugin.AnalysisCacheService
import com.worksap.nlp.elasticsearch.sudachi.plugin.DictionaryService
import com.worksap.nlp.lucene.sudachi.ja.CachingTokenizer
import com.worksap.nlp.lucene.sudachi.ja.SudachiTokenizer
import org.apache.lucene.analysis.Tokenizer
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.env.Environment
import org.elasticsearch.index.IndexSettings
import org.elasticsearch.index.analysis.TokenizerFactory
import org.elasticsearch.indices.analysis.AnalysisModule.AnalysisProvider

class SudachiTokenizerFactory(
    private val service: DictionaryService,
    private val caches: AnalysisCacheService,
    indexSettings: IndexSettings,
    private val env: Environment,
    name: String,
    settings: Settings
) : AbstractTokenizerFactory(indexSettings, settings, name) {

  companion object {
    @JvmStatic
    fun maker(
        service: DictionaryService,
        caches: AnalysisCacheService
    ): AnalysisProvider<TokenizerFactory> {
      return AnalysisProvider { a, b, c, d -> SudachiTokenizerFactory(service, caches, a, b, c, d) }
    }
  }

  private val mode = ConfigAdapter.splitMode(settings)

  private val config = ConfigAdapter(indexSettings, name, settings, env)

  private val dictionary by lazy { service.forConfig(config.compiled) }

  private val cache by lazy { caches.analysisCache(indexSettings.index.name, mode, settings) }

  override fun create(): Tokenizer {
    val tok = CachingTokenizer(dictionary.newTokenizer(), mode, cache)
    return SudachiTokenizer(tok, config.discardPunctuation)
  }
}
