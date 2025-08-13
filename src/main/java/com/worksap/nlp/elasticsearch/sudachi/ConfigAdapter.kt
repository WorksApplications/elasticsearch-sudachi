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

import com.worksap.nlp.search.aliases.Environment
import com.worksap.nlp.search.aliases.Settings
import com.worksap.nlp.sudachi.Config
import com.worksap.nlp.sudachi.PathAnchor
import com.worksap.nlp.sudachi.Tokenizer.SplitMode
import com.worksap.nlp.tools.EnumFlag
import java.nio.file.Path
import kotlin.io.path.exists

class ConfigAdapter(anchor: PathAnchor, settings: Settings, env: Environment) {
  private val basePath = resourcesPath(env, settings)
  private val fullAnchor = PathAnchor.filesystem(basePath).andThen(anchor)

  val discardPunctuation: Boolean = settings.getAsBoolean(PARAM_DISCARD_PUNCTUATION, true)
  // default false to let every morpheme have non-null span in the input text
  val allowEmptyMorpheme: Boolean = settings.getAsBoolean(PARAM_ALLOW_EMPTY_MORPHEME, false)
  val mode = splitMode(settings)

  val compiled: Config = run {
    val base = settingsFile(settings)
    val additional = settingsInlineString(settings, fullAnchor)
    additional.withFallback(base).anchoredWith(fullAnchor)
  }

  private fun settingsFile(settings: Settings): Config {
    val settingsPath = settings.get(PARAM_SETTINGS_PATH)
    val base =
        if (settingsPath == null) {
          readDefaultConfig(basePath, fullAnchor)
        } else {
          val configObject = fullAnchor.resource<Any>(settingsPath)
          Config.fromResource(configObject, fullAnchor)
        }
    return base.allowEmptyMorpheme(allowEmptyMorpheme)
  }

  companion object {
    const val PARAM_SPLIT_MODE_DEPRECATED = "mode"
    const val PARAM_SPLIT_MODE = "split_mode"
    const val PARAM_SETTINGS_PATH = "settings_path"
    const val PARAM_RESOURCES_PATH = "resources_path"
    const val PARAM_ADDITIONAL_SETTINGS = "additional_settings"
    const val PARAM_DISCARD_PUNCTUATION = "discard_punctuation"
    const val PARAM_ALLOW_EMPTY_MORPHEME = "allow_empty_morpheme"

    const val DEFAULT_SETTINGS_FILENAME = "sudachi.json"
    const val DEFAULT_RESOURCE_PATH = "sudachi"

    private object SplitModeFlag : EnumFlag<SplitMode>(PARAM_SPLIT_MODE, SplitMode.C)

    @JvmStatic
    fun splitMode(settings: Settings): SplitMode {
      if (settings.get(PARAM_SPLIT_MODE_DEPRECATED, null) != null) {
        throw IllegalArgumentException(
            "Setting $PARAM_SPLIT_MODE_DEPRECATED is deprecated, use $PARAM_SPLIT_MODE instead",
        )
      }
      return SplitModeFlag.get(settings)
    }

    private fun readDefaultConfig(root: Path, baseAnchor: PathAnchor): Config {
      val anchor = PathAnchor.filesystem(root).andThen(baseAnchor)
      val resolved = root.resolve(DEFAULT_SETTINGS_FILENAME)
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
    fun settingsInlineString(settings: Settings, anchor: PathAnchor): Config {
      val settingsString = settings.get(PARAM_ADDITIONAL_SETTINGS)
      return if (settingsString == null) {
        Config.empty()
      } else {
        Config.fromJsonString(
            settingsString,
            anchor,
        )
      }
    }
  }
}
