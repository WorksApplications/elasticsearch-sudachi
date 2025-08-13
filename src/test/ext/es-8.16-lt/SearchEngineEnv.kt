/*
 * Copyright (c) 2023-2025 Works Applications Co., Ltd.
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

import com.worksap.nlp.elasticsearch.sudachi.aliases.MetadataConstants
import com.worksap.nlp.search.aliases.*

fun SearchEngineEnv.indexAnalyzers(settings: Settings): IndexAnalyzers {
  val indexSettings = IndexSettingsModule.newIndexSettings(Index("test", "_na_"), settings)
  val context = IndexCreationContext.RELOAD_ANALYZERS
  return analysisRegistry.build(context, indexSettings)
}

fun SearchEngineEnv.tokenizers(settings: Map<String, String>): Map<String, TokenizerFactory> {
  val builder = Settings.builder()
  settings.forEach { (key: String?, value: String?) -> builder.put(key, value) }
  builder.put(MetadataConstants.SETTING_VERSION_CREATED, Version.CURRENT)
  val indexSettings = builder.build()
  return analysisRegistry.buildTokenizerFactories(
      IndexSettingsModule.newIndexSettings(Index("test", "_na_"), indexSettings))
}
