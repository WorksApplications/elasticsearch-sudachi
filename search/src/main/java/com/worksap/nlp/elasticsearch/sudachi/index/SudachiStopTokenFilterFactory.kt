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

import com.worksap.nlp.lucene.sudachi.ja.SudachiAnalyzer
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.core.StopFilter
import org.apache.lucene.search.suggest.analyzing.SuggestStopFilter
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.env.Environment
import org.elasticsearch.index.IndexSettings
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory
import org.elasticsearch.index.analysis.Analysis.parseWords

class SudachiStopTokenFilterFactory(
    indexSettings: IndexSettings?,
    env: Environment?,
    name: String?,
    settings: Settings,
) : AbstractTokenFilterFactory(name) {
  private val ignoreCase = settings.getAsBoolean("ignore_case", false)
  private val removeTrailing = settings.getAsBoolean("remove_trailing", true)
  private val stopWords =
      parseWords(
          env,
          settings,
          "stopwords",
          SudachiAnalyzer.getDefaultStopSet(),
          NAMED_STOP_WORDS,
          ignoreCase,
      )

  override fun create(tokenStream: TokenStream?): TokenStream {
    return if (removeTrailing) {
      StopFilter(tokenStream, stopWords)
    } else {
      SuggestStopFilter(tokenStream, stopWords)
    }
  }

  companion object {
    private val NAMED_STOP_WORDS =
        mapOf(
            "_japanese_" to SudachiAnalyzer.getDefaultStopSet(),
        )
  }
}
