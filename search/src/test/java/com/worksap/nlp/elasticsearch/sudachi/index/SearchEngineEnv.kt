/*
 * Copyright (c) 2023-2026 Works Applications Co., Ltd.
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

import com.worksap.nlp.elasticsearch.sudachi.plugin.AnalysisSudachiPlugin
import com.worksap.nlp.test.TestDictionary
import java.nio.file.Path
import org.elasticsearch.Version
import org.elasticsearch.cluster.metadata.IndexMetadata
import org.elasticsearch.common.settings.IndexScopedSettings
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.env.Environment
import org.elasticsearch.env.TestEnvironment
import org.elasticsearch.index.IndexService.IndexCreationContext
import org.elasticsearch.index.IndexSettings
import org.elasticsearch.index.IndexVersion
import org.elasticsearch.index.analysis.IndexAnalyzers
import org.elasticsearch.index.analysis.TokenizerFactory
import org.elasticsearch.indices.analysis.AnalysisModule
import org.elasticsearch.plugins.AnalysisPlugin
import org.junit.rules.ExternalResource
import org.junit.runner.Description
import org.junit.runners.model.Statement

class SearchEngineEnv(vararg components: String = arrayOf("system")) : ExternalResource() {
  private val testDic = TestDictionary(*components)

  init {
    SearchEngineLogging.touch()
  }

  override fun apply(base: Statement, description: Description): Statement {
    val s1 = testDic.apply(base, description)
    return super.apply(s1, description)
  }

  val root: Path
    get() = testDic.root.toPath()

  private val analysisModule by lazy {
    val nodeSettings =
        Settings.builder().put(Environment.PATH_HOME_SETTING.key, testDic.root.path).build()
    val env = TestEnvironment.newEnvironment(nodeSettings)
    makeAnalysisModule(env, AnalysisSudachiPlugin(nodeSettings))
  }

  val analysisRegistry
    get() = analysisModule.analysisRegistry

  fun indexAnalyzers(settings: Settings): IndexAnalyzers {
    val indexSettings = newIndexSettings("test", settings)
    val context = IndexCreationContext.RELOAD_ANALYZERS
    return analysisRegistry.build(context, indexSettings)
  }

  fun tokenizers(settings: Map<String, String>): Map<String, TokenizerFactory> {
    val builder = Settings.builder()
    settings.forEach { (key: String?, value: String?) -> builder.put(key, value) }
    builder.put(IndexMetadata.SETTING_VERSION_CREATED, Version.CURRENT)
    val indexSettings = newIndexSettings("test", builder.build())
    return analysisRegistry.buildTokenizerFactories(indexSettings)
  }

  fun newIndexSettings(name: String, settings: Settings): IndexSettings {
    val build =
        Settings.builder()
            .put(IndexMetadata.SETTING_NUMBER_OF_SHARDS, 1)
            .put(IndexMetadata.SETTING_NUMBER_OF_REPLICAS, 1)
            .put(IndexMetadata.SETTING_VERSION_CREATED, IndexVersion.current())
            .put(settings)
            .build()
    val metadata =
        IndexMetadata.builder(name)
            .system(
                IndexSettings.INDEX_FAST_REFRESH_SETTING.get(settings)
            ) // using fast refresh requires a system index
            .settings(build)
            .build()
    val settingSet = HashSet(IndexScopedSettings.BUILT_IN_INDEX_SETTINGS)
    val scopedSettings = IndexScopedSettings(Settings.EMPTY, settingSet)

    val indexSettings = IndexSettings(metadata, Settings.EMPTY, scopedSettings)
    return indexSettings
  }

  /**
   * Reflection hack for instantiating AnalysisModule
   *
   * From ES 8.5 it has StablePluginsRegistry mechanism which require a different constructor of the
   * AnalysisModule
   */
  private fun makeAnalysisModule(env: Environment, vararg plugins: AnalysisPlugin): AnalysisModule {
    val constructors = AnalysisModule::class.java.declaredConstructors
    val pluginList = plugins.asList()
    val clz =
        try {
          Class.forName("org.elasticsearch.plugins.scanners.StablePluginsRegistry")
        } catch (_: ClassNotFoundException) {
          null
        }

    if (clz == null) {
      constructors
          .find { it.parameterCount == 2 }
          ?.let {
            return it.newInstance(env, pluginList) as AnalysisModule
          }
    } else {
      constructors
          .find { it.parameterCount == 3 }
          ?.let {
            val stablePluginRegistry = clz.getConstructor().newInstance()
            return it.newInstance(env, pluginList, stablePluginRegistry) as AnalysisModule
          }
    }
    throw IllegalStateException("failed to instantiate AnalysisModule")
  }
}
