/*
 * Copyright (c) 2023-2024 Works Applications Co., Ltd.
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
import com.worksap.nlp.elasticsearch.sudachi.plugin.AnalysisSudachiPlugin
import com.worksap.nlp.search.aliases.*
import com.worksap.nlp.test.TestDictionary
import java.nio.file.Path
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
    val indexSettings = IndexSettingsModule.newIndexSettings(Index("test", "_na_"), settings)
    return analysisRegistry.build(indexSettings)
  }

  fun tokenizers(settings: Map<String, String>): Map<String, TokenizerFactory> {
    val builder = Settings.builder()
    settings.forEach { (key: String?, value: String?) -> builder.put(key, value) }
    builder.put(MetadataConstants.SETTING_VERSION_CREATED, Version.CURRENT)
    val indexSettings = builder.build()
    val nodeSettings =
        Settings.builder().put(Environment.PATH_HOME_SETTING.key, testDic.root.path).build()
    val env = TestEnvironment.newEnvironment(nodeSettings)
    val analysisModule = makeAnalysisModule(env, AnalysisSudachiPlugin(nodeSettings))
    val analysisRegistry = analysisModule.analysisRegistry
    return analysisRegistry.buildTokenizerFactories(
        IndexSettingsModule.newIndexSettings(Index("test", "_na_"), indexSettings))
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
