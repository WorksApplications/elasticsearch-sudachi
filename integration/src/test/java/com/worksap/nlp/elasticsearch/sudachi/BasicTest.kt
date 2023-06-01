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

import org.junit.Assert
import org.junit.Test

class BasicTest : SudachiEnvTest() {
  @Test
  fun canLoadPlugin() {
    val plugins = sudachiEnv.makePluginService()
    val analysisPlugins = plugins.filterPlugins(AnalysisPluginAlias::class.java)
    Assert.assertEquals(2, analysisPlugins.size)
    val plugin =
        analysisPlugins.find {
          it.javaClass.name == "com.worksap.nlp.elasticsearch.sudachi.plugin.AnalysisSudachiPlugin"
        }
    Assert.assertNotNull(plugin)
  }

  @Test
  fun canInstantiateTokenizer() {
    val analysisSvc = analysisRegistry()
    val sudachi = analysisSvc.getAnalyzer("sudachi")
    Assert.assertNotNull(sudachi)
  }

  @Test
  fun testAnalysisAction() {
    val req = AnalyzeActionRequestAlias("sudachi_test")
    req.tokenizer("sudachi_tokenizer")
    req.text("京都に行った")
    val analyzers = analysisRegistry()
    val response =
        TransportAnalyzeActionAlias.analyze(
            req,
            analyzers,
            null,
            1000,
        )
    Assert.assertEquals(4, response.tokens.size)
    val tokens = response.tokens
    tokens[0].let {
      Assert.assertEquals("京都", it.term)
      Assert.assertEquals(0, it.position)
      Assert.assertEquals(0, it.startOffset)
      Assert.assertEquals(2, it.endOffset)
    }
    tokens[1].let {
      Assert.assertEquals("に", it.term)
      Assert.assertEquals(1, it.position)
      Assert.assertEquals(2, it.startOffset)
      Assert.assertEquals(3, it.endOffset)
    }
    tokens[2].let {
      Assert.assertEquals("行っ", it.term)
      Assert.assertEquals(2, it.position)
      Assert.assertEquals(3, it.startOffset)
      Assert.assertEquals(5, it.endOffset)
    }
    tokens[3].let {
      Assert.assertEquals("た", it.term)
      Assert.assertEquals(3, it.position)
      Assert.assertEquals(5, it.startOffset)
      Assert.assertEquals(6, it.endOffset)
    }
  }
}
