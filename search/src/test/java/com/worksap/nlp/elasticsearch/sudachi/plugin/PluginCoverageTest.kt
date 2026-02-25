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

package com.worksap.nlp.elasticsearch.sudachi.plugin

import com.worksap.nlp.search.aliases.Settings
import kotlin.test.assertTrue
import org.junit.Test

class PluginCoverageTest {
  @Test
  fun analysisSudachiPluginExposesFactories() {
    val plugin = AnalysisSudachiPlugin(Settings.EMPTY)
    val tokenFilters = plugin.getTokenFilters()
    assertTrue(tokenFilters.containsKey("sudachi_baseform"))
    assertTrue(tokenFilters.containsKey("sudachi_normalizedform"))
    assertTrue(tokenFilters.containsKey("sudachi_readingform"))
    assertTrue(tokenFilters.containsKey("sudachi_part_of_speech"))
    assertTrue(tokenFilters.containsKey("sudachi_split"))
    assertTrue(tokenFilters.containsKey("sudachi_ja_stop"))

    val tokenizers = plugin.getTokenizers()
    assertTrue(tokenizers.containsKey("sudachi_tokenizer"))

    val analyzers = plugin.getAnalyzers()
    assertTrue(analyzers.containsKey("sudachi"))
  }
}
