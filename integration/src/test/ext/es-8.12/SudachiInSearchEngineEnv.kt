/*
 * Copyright (c) 2024-2025 Works Applications Co., Ltd.
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

package com.worksap.nlp.elasticsearch.sudachi

import org.elasticsearch.indices.analysis.AnalysisModule
import org.elasticsearch.plugins.AnalysisPlugin
import org.elasticsearch.plugins.PluginsService

typealias PluginsServiceAlias = PluginsService

fun SudachiInSearchEngineEnv.makePluginService(): PluginsServiceAlias {
  return PluginsService(settings(), configPath, null, pluginsPath)
}

fun SudachiInSearchEngineEnv.makeAnalysisModule(): AnalysisModule {
  val plugins = makePluginService()
  val analysisPlugins = plugins.filterPlugins(AnalysisPlugin::class.java).toList()
  val env = environment()
  return AnalysisModule(env, analysisPlugins, plugins.stablePluginRegistry)
}
