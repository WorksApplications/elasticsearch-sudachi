/*
 * Copyright (c) 2026 Works Applications Co., Ltd.
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

import com.worksap.nlp.sudachi.PathAnchor
import com.worksap.nlp.sudachi.Tokenizer.SplitMode
import com.worksap.nlp.test.TestDictionary
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.env.Environment
import org.elasticsearch.env.TestEnvironment
import org.junit.Rule
import org.junit.Test

class ConfigAdapterTest {
  @JvmField @Rule var testDic = TestDictionary("system")

  /// This test should be removed with PARAM_SPLIT_MODE_DEPRECATED settings field
  @Test
  fun splitModeRejectsDeprecatedSetting() {
    val settings = Settings.builder().put(ConfigAdapter.PARAM_SPLIT_MODE_DEPRECATED, "C").build()
    assertFailsWith<IllegalArgumentException> { ConfigAdapter.splitMode(settings) }
  }

  @Test
  fun configAdapterUsesDefaultsFromConfigDir() {
    val nodeSettings =
        Settings.builder().put(Environment.PATH_HOME_SETTING.key, testDic.root.path).build()
    val env = TestEnvironment.newEnvironment(nodeSettings)
    val settings = Settings.builder().build()
    val adapter = ConfigAdapter(PathAnchor.none(), settings, env)

    assertEquals(SplitMode.C, adapter.mode)
    assertEquals(true, adapter.discardPunctuation)
    assertEquals(false, adapter.allowEmptyMorpheme)
    assertNotNull(adapter.compiled)
  }

  @Test
  fun settingsInlineStringHandlesAdditionalSettings() {
    val settings =
        Settings.builder()
            .put(ConfigAdapter.PARAM_ADDITIONAL_SETTINGS, "{\"allowEmptyMorpheme\": true}")
            .build()
    val config = ConfigAdapter.settingsInlineString(settings, PathAnchor.none())

    assertNotNull(config)
    assertEquals(true, config.isAllowEmptyMorpheme)
  }
}
