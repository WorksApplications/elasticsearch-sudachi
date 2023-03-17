/*
 * Copyright (c) 2017-2023 Works Applications Co., Ltd.
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

import com.worksap.nlp.elasticsearch.sudachi.index.*
import com.worksap.nlp.search.aliases.*
import java.io.IOException
import java.util.*
import org.apache.logging.log4j.LogManager
import org.apache.lucene.analysis.Analyzer

private inline fun <reified T> provider(
    crossinline fn: (IndexSettings, Environment, String, Settings) -> T
): AnalysisProvider<T> {
  return AnalysisProvider { a, b, c, d -> fn(a, b, c, d) }
}

class AnalysisSudachiPlugin(settings: Settings?) : Plugin(), AnalysisPlugin {
  private val dictionaryService = DictionaryService()
  private val cacheService = AnalysisCacheService()

  init {
    logger.info("loaded Sudachi plugin")
  }

  override fun getTokenFilters(): Map<String, AnalysisProvider<TokenFilterFactory>> {
    return mapOf(
        "sudachi_baseform" to provider(::SudachiBaseFormFilterFactory),
        "sudachi_normalizedform" to provider(::SudachiNormalizedFormFilterFactory),
        "sudachi_part_of_speech" to provider(::SudachiPartOfSpeechFilterFactory),
        "sudachi_readingform" to provider(::SudachiReadingFormFilterFactory),
        "sudachi_split" to provider(::SudachiSplitFilterFactory),
        "sudachi_ja_stop" to provider(::SudachiStopTokenFilterFactory),
    )
  }

  override fun getTokenizers(): Map<String, AnalysisProvider<TokenizerFactory>> {
    return mapOf(
        "sudachi_tokenizer" to SudachiTokenizerFactory.maker(dictionaryService, cacheService))
  }

  override fun getAnalyzers(): Map<String, AnalysisProvider<AnalyzerProvider<out Analyzer>>> {
    return mapOf(
        "sudachi" to SudachiAnalyzerProvider.maker(dictionaryService, cacheService),
    )
  }

  @Throws(IOException::class)
  override fun close() {
    super.close()
  }

  companion object {
    private val logger = LogManager.getLogger(AnalysisSudachiPlugin::class.java)
  }
}
