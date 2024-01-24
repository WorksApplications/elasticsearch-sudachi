/*
 * Copyright (c) 2020-2024 Works Applications Co., Ltd.
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

import com.worksap.nlp.lucene.sudachi.ja.ResourceUtil
import org.junit.Rule
import org.junit.Test

open class TestSudachiAnalysis : SearchEngineTestBase {
  @JvmField @Rule var env = SearchEngineEnv("system", "user0.dic")

  @Test
  fun tokenizer() {
    val settings =
        mapOf(
            "index.analysis.tokenizer.sudachi_tokenizer.type" to "sudachi_tokenizer",
            "index.analysis.tokenizer.sudachi_tokenizer.settings_path" to "sudachi.json")

    val tokenizer = env.tokenizers(settings)["sudachi_tokenizer"]
    tokenizer.assertTerms("東京へ行く。", "東京", "へ", "行く")
  }

  @Test
  fun tokenizerWithAdditionalSettings() {
    val additional: String = ResourceUtil.resource("additional.json").readText()
    val settings =
        mapOf(
            "index.analysis.tokenizer.sudachi_tokenizer.type" to "sudachi_tokenizer",
            "index.analysis.tokenizer.sudachi_tokenizer.additional_settings" to additional)

    val tokenizer = env.tokenizers(settings)["sudachi_tokenizer"]
    tokenizer.assertTerms("自然言語", "自然", "言語")
  }

  @Test
  fun useSudachiAnalyzer() {
    val settings =
        """
      {
        "index.analysis": {          
          "analyzer": {
            "sudachi": {
              "type": "sudachi",
              "settings_path": "sudachi.json"
            }
          }
        }
      }
    """.jsonSettings()

    val sudachi = env.indexAnalyzers(settings)["sudachi"]
    sudachi.assertTerms("東京へ行く。", "東京", "行く")
  }

  @Test
  fun withoutCache() {
    val settings =
        """
      {
        "index.analysis": {          
          "analyzer": {
            "sudachi": {
              "type": "sudachi",
              "settings_path": "sudachi.json",
              "cache-size": 0
            }
          }
        }
      }
    """.jsonSettings()

    val sudachi = env.indexAnalyzers(settings)["sudachi"]
    sudachi.assertTerms("東京へ行く。", "東京", "行く")
  }

  @Test
  fun twoAnalyzersWithDifferentSettingsWorkCorrectly() {
    val settings =
        """{
      "index.analysis": {
        "analyzer": {
          "sudachi_1_a": {
            "type": "custom",
            "tokenizer": "sudachi_1_t"
          },
          "sudachi_2_a": {
            "type": "custom",
            "tokenizer": "sudachi_2_t"
          }
        },
        "tokenizer": {
          "sudachi_1_t": {
            "type": "sudachi_tokenizer"
          },
          "sudachi_2_t": {
            "type": "sudachi_tokenizer",
            "additional_settings": "{\"userDict\":[\"user0.dic\"]}"
          }
        }
      }
    }""".jsonSettings()
    val analyzers = env.indexAnalyzers(settings)
    analyzers["sudachi_1_a"].assertTerms("にアイ都に行く", "に", "アイ", "都", "に", "行く")
    analyzers["sudachi_2_a"].assertTerms("にアイ都に行く", "にアイ都", "に", "行く")
  }
}
