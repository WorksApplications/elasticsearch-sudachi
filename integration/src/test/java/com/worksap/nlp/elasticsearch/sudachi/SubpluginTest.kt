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

import kotlin.test.Test
import org.junit.Assert

class SubpluginTest : SudachiEnvTest() {

  @Test
  fun loadsConfigAndPluginFromSubplugin() {
    val req = AnalyzeActionRequestAlias("sudachi_test")

    req.tokenizer(mapOf("type" to "sudachi_tokenizer", "settings_path" to "sudachi_subplugin.json"))
    req.text("ゲゲゲの鬼太郎")
    val analyzers = analysisRegistry()
    val response =
        TransportAnalyzeActionAlias.analyze(
            req,
            analyzers,
            null,
            1000,
        )
    Assert.assertEquals(1, response.tokens.size)
    response.tokens[0].let {
      Assert.assertEquals("ゲゲゲの鬼太郎", it.term)
      Assert.assertEquals(0, it.startOffset)
      Assert.assertEquals(7, it.endOffset)
    }
  }
}
