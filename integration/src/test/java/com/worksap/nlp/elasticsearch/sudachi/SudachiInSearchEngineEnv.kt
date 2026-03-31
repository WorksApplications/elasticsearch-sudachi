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

package com.worksap.nlp.elasticsearch.sudachi

import java.nio.file.Path
import kotlin.io.path.Path
import org.opensearch.common.settings.Settings
import org.opensearch.env.Environment
import org.opensearch.index.analysis.AnalysisRegistry
import org.opensearch.indices.analysis.AnalysisModule
import org.opensearch.plugins.AnalysisPlugin
import org.opensearch.plugins.PluginsService
import org.opensearch.test.OpenSearchTestCase

class SudachiInSearchEngineEnv {
  val rootPath = Path(requireNotNull(System.getProperty("sudachi.es.root")))

  fun settings(parent: Settings = Settings.EMPTY): Settings {
    val bldr = Settings.builder()
    bldr.put(parent)
    bldr.put(Environment.PATH_HOME_SETTING.key, rootPath.toString())
    return bldr.build()
  }

  val pluginsPath: Path
    get() = rootPath.resolve("plugins")
  val configPath: Path
    get() = rootPath.resolve("config")

  fun environment(): Environment {
    return Environment(settings(), configPath)
  }

  fun makePluginService(): PluginsService {
    return PluginsService(settings(), configPath, null, pluginsPath, emptyList())
  }

  fun makeAnalysisModule(): AnalysisModule {
    val plugins = makePluginService()
    val analysisPlugins = plugins.filterPlugins(AnalysisPlugin::class.java)
    val env = environment()
    return AnalysisModule(env, analysisPlugins)
  }
}

abstract class SudachiEnvTest : OpenSearchTestCase() {
  internal val sudachiEnv = SudachiInSearchEngineEnv()

  private val analysisModule by lazy { sudachiEnv.makeAnalysisModule() }
  fun analysisRegistry(): AnalysisRegistry = analysisModule.analysisRegistry
}
