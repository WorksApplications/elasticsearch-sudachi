/*
 * Copyright (c) 2023 Works Applications Co., Ltd.
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

class SudachiInSearchEngineEnv {
  val rootPath = Path(requireNotNull(System.getProperty("sudachi.es.root")))

  fun settings(parent: SettingsAlias = SettingsAlias.EMPTY): SettingsAlias {
    val bldr = SettingsAlias.builder()
    bldr.put(parent)
    bldr.put(EnvironmentAlias.PATH_HOME_SETTING.key, rootPath.toString())
    return bldr.build()
  }

  val pluginsPath: Path
    get() = rootPath.resolve("plugins")
  val configPath: Path
    get() = rootPath.resolve("config")

  fun environment(): EnvironmentAlias {
    return EnvironmentAlias(settings(), configPath)
  }
}

abstract class SudachiEnvTest : SearchEngineTestCase() {
  internal val sudachiEnv = SudachiInSearchEngineEnv()

  private val analysisModule by lazy { sudachiEnv.makeAnalysisModule() }
  fun analysisRegistry(): AnalysisRegistryAlias = analysisModule.analysisRegistry
}
