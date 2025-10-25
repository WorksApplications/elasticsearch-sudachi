/*
 * Copyright (c) 2022-2025 Works Applications Co., Ltd.
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
import org.elasticsearch.plugins.scanners.StablePluginsRegistry

// For ES 9.x tests, we manually instantiate plugins since PluginsService API changed
class TestPluginsService(private val plugins: List<AnalysisPlugin>) {
  fun <T> filterPlugins(clazz: Class<T>): List<T> = plugins.filterIsInstance(clazz)
}

typealias PluginsServiceAlias = TestPluginsService

private fun loadTestPlugins(): List<AnalysisPlugin> {
  return listOfNotNull(
      tryLoadPlugin("com.worksap.nlp.elasticsearch.sudachi.plugin.AnalysisSudachiPlugin", true),
      tryLoadPlugin("org.elasticsearch.plugin.analysis.icu.AnalysisICUPlugin", false))
}

private fun tryLoadPlugin(className: String, requiresSettings: Boolean): AnalysisPlugin? {
  return try {
    val clazz = Class.forName(className)
    if (requiresSettings) {
      clazz.getConstructor(org.elasticsearch.common.settings.Settings::class.java).newInstance(null)
          as AnalysisPlugin
    } else {
      clazz.getDeclaredConstructor().newInstance() as AnalysisPlugin
    }
  } catch (e: Exception) {
    null // Plugin not available or failed to load
  }
}

fun SudachiInSearchEngineEnv.makePluginService() = TestPluginsService(loadTestPlugins())

fun SudachiInSearchEngineEnv.makeAnalysisModule() =
    AnalysisModule(environment(), loadTestPlugins(), StablePluginsRegistry())
