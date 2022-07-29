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

package com.worksap.nlp.elasticsearch.sudachi

import com.worksap.nlp.sudachi.Config
import com.worksap.nlp.sudachi.PathAnchor
import com.worksap.nlp.sudachi.Tokenizer.SplitMode
import java.nio.file.Path
import java.util.*
import kotlin.io.path.exists
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.env.Environment
import org.elasticsearch.index.IndexSettings

@Suppress("UNUSED_PARAMETER")
class ConfigAdapter(index: IndexSettings, name: String, settings: Settings, env: Environment) {

  private val basePath = resourcesPath(env, settings)

  val compiled: Config = run {
    val base = settingsFile(basePath, settings)
    val additional = settingsInlineString(basePath, settings)
    additional.withFallback(base)
  }

  val discardPunctuation = settings.getAsBoolean(PARAM_DISCARD_PUNCTUATION, true)

  val mode = splitMode(settings)

  companion object {
    const val PARAM_SPLIT_MODE = "split_mode"
    const val PARAM_SPLIT_MODE_DEPRECATED = "mode"
    const val PARAM_SETTINGS_PATH = "settings_path"
    const val PARAM_ADDITIONAL_SETTINGS = "additional_settings"
    const val PARAM_DISCARD_PUNCTUATION = "discard_punctuation"

    @JvmStatic
    fun splitMode(settings: Settings): SplitMode {
      if (settings.get(PARAM_SPLIT_MODE_DEPRECATED, null) != null) {
        throw IllegalArgumentException(
            "Setting $PARAM_SPLIT_MODE_DEPRECATED is deprecated, use SudachiSplitFilter instead")
      }
      return when (val modeStr = settings.get(PARAM_SPLIT_MODE)?.lowercase(Locale.ROOT) ?: "c") {
        "a" -> SplitMode.A
        "b" -> SplitMode.B
        "c" -> SplitMode.C
        else -> throw IllegalArgumentException("invalid SplitMode $modeStr")
      }
    }

    @JvmStatic
    fun resourcesPath(env: Environment, settings: Settings): Path {
      return env.configFile().resolve(settings.get("resources_path", "sudachi"))
    }

    private fun readDefaultConfig(root: Path): Config {
      val anchor = PathAnchor.filesystem(root).andThen(PathAnchor.classpath())
      val resolved = root.resolve("sudachi.json")
      val exists =
          try {
            resolved.exists()
          } catch (e: SecurityException) {
            false
          }
      return if (exists) {
        Config.fromFile(resolved, anchor)
      } else {
        Config.defaultConfig(anchor)
      }
    }

    @JvmStatic
    fun settingsFile(root: Path, settings: Settings): Config {
      val settingsPath = settings.get(PARAM_SETTINGS_PATH)
      return if (settingsPath == null) {
        readDefaultConfig(root)
      } else {
        val resolved = root.resolve(settingsPath)
        Config.fromFile(resolved)
      }
    }

    @JvmStatic
    fun settingsInlineString(root: Path, settings: Settings): Config {
      val settingsString = settings.get(PARAM_ADDITIONAL_SETTINGS)
      return if (settingsString == null) {
        Config.empty()
      } else {
        Config.fromJsonString(
            settingsString, PathAnchor.filesystem(root).andThen(PathAnchor.classpath()))
      }
    }
  }
}
