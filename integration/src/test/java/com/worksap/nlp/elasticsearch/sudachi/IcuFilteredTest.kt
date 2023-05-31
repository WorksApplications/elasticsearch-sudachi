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

class IcuFilteredTest : SudachiEnvTest() {
  @Test
  fun icuFilteredStuffIsNotTrimmed() {
    val req = AnalyzeActionRequestAlias("sudachi_test")
    req.tokenizer("sudachi_tokenizer")
    req.addCharFilter(mapOf("type" to "icu_normalizer", "name" to "nfkc_cf", "mode" to "compose"))
    req.text("white")
    val analyzers = analysisRegistry()
    val response =
        TransportAnalyzeActionAlias.analyze(
            req,
            analyzers,
            null,
            1000,
        )
    Assert.assertEquals(1, response.tokens.size)
    val tokens = response.tokens
    tokens[0].let {
      Assert.assertEquals("white", it.term)
      Assert.assertEquals(0, it.position)
      Assert.assertEquals(0, it.startOffset)
      Assert.assertEquals(5, it.endOffset)
    }
  }
}
