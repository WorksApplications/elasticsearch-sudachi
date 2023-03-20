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

package com.worksap.nlp.elasticsearch.sudachi.index

import org.junit.Rule
import org.junit.Test

class CustomAnalyzerTest : SearchEngineTestBase {
  @JvmField @Rule var engine = SearchEngineEnv()

  @Test
  fun customAnalyzerWithSudachi() {
    val settings =
        """
      {
        "index.analysis": {          
          "analyzer": {
            "sudachi_basic": {
              "type": "custom",
              "tokenizer": "sudachi_tokenizer"
            }
          },
          "tokenizer": {
            "sudachi_tokenizer": {
              "type": "sudachi_tokenizer",
              "split_mode": "C"
            }
          }
        }
      }
    """.jsonSettings()
    val analyzers = engine.indexAnalyzers(settings)
    val basic = analyzers.get("sudachi_basic")
    basic.assertTerms("東京に行く", "東京", "に", "行く")
  }

  @Test
  fun stoptagsEmpty() {
    val settings =
        """
      {
        "index.analysis": {          
          "analyzer": {
            "sudachi_basic": {
              "type": "custom",
              "tokenizer": "sudachi_tokenizer",
              "filter": [
                "pos"
              ]
            }
          },
          "tokenizer": {
            "sudachi_tokenizer": {
              "type": "sudachi_tokenizer",
              "split_mode": "C"
            }
          },
          "filter": {
            "pos": {
              "type": "sudachi_part_of_speech"              
            }
          }
        }
      }
    """.jsonSettings()
    val analyzers = engine.indexAnalyzers(settings)
    val basic = analyzers.get("sudachi_basic")
    basic.assertTerms("東京に行く", "東京", "に", "行く")
  }

  @Test
  fun stoptagsNotEmpty() {
    val settings =
        """
      {
        "index.analysis": {          
          "analyzer": {
            "sudachi_basic": {
              "type": "custom",
              "tokenizer": "sudachi_tokenizer",
              "filter": [
                "pos"
              ]
            }
          },
          "tokenizer": {
            "sudachi_tokenizer": {
              "type": "sudachi_tokenizer",
              "split_mode": "C"
            }
          },
          "filter": {
            "pos": {
              "type": "sudachi_part_of_speech",
              "stoptags": ["助詞", "名詞,固有名詞"]
            }
          }
        }
      }
    """.jsonSettings()
    val analyzers = engine.indexAnalyzers(settings)
    val basic = analyzers.get("sudachi_basic")
    basic.assertTerms("東京に行く", "行く")
  }

  @Test
  fun baseform() {
    val settings =
        """
      {
        "index.analysis": {          
          "analyzer": {
            "sudachi_test": {
              "type": "custom",
              "tokenizer": "sudachi_tokenizer",
              "filter": [
                "test"
              ]
            }
          },
          "tokenizer": {
            "sudachi_tokenizer": {
              "type": "sudachi_tokenizer",
              "split_mode": "C"
            }
          },
          "filter": {
            "test": {
              "type": "sudachi_baseform"
            }
          }
        }
      }
    """.jsonSettings()
    val analyzers = engine.indexAnalyzers(settings)
    val basic = analyzers.get("sudachi_test")
    basic.assertTerms("東京にいった", "東京", "に", "いく", "た")
  }

  @Test
  fun normalizedForm() {
    val settings =
        """
      {
        "index.analysis": {          
          "analyzer": {
            "sudachi_test": {
              "type": "custom",
              "tokenizer": "sudachi_tokenizer",
              "filter": [
                "test"
              ]
            }
          },
          "tokenizer": {
            "sudachi_tokenizer": {
              "type": "sudachi_tokenizer",
              "split_mode": "C"
            }
          },
          "filter": {
            "test": {
              "type": "sudachi_normalizedform"
            }
          }
        }
      }
    """.jsonSettings()
    val analyzers = engine.indexAnalyzers(settings)
    val basic = analyzers.get("sudachi_test")
    basic.assertTerms("東京にふく", "東京", "に", "吹く")
  }

  @Test
  fun readingFormKatakana() {
    val settings =
        """
      {
        "index.analysis": {          
          "analyzer": {
            "sudachi_test": {
              "type": "custom",
              "tokenizer": "sudachi_tokenizer",
              "filter": [
                "test"
              ]
            }
          },
          "tokenizer": {
            "sudachi_tokenizer": {
              "type": "sudachi_tokenizer",
              "split_mode": "C"
            }
          },
          "filter": {
            "test": {
              "type": "sudachi_readingform"
            }
          }
        }
      }
    """.jsonSettings()
    val analyzers = engine.indexAnalyzers(settings)
    val basic = analyzers.get("sudachi_test")
    basic.assertTerms("東京にふく", "トウキョウ", "ニ", "フク")
  }

  @Test
  fun readingFormRomaji() {
    val settings =
        """
      {
        "index.analysis": {          
          "analyzer": {
            "sudachi_test": {
              "type": "custom",
              "tokenizer": "sudachi_tokenizer",
              "filter": [
                "test"
              ]
            }
          },
          "tokenizer": {
            "sudachi_tokenizer": {
              "type": "sudachi_tokenizer",
              "split_mode": "C"
            }
          },
          "filter": {
            "test": {
              "type": "sudachi_readingform",
              "use_romaji": true
            }
          }
        }
      }
    """.jsonSettings()
    val analyzers = engine.indexAnalyzers(settings)
    val basic = analyzers.get("sudachi_test")
    basic.assertTerms("東京にふく", "toukyou", "ni", "huku")
  }

  @Test
  fun splitA() {
    val settings =
        """
      {
        "index.analysis": {          
          "analyzer": {
            "sudachi_test": {
              "type": "custom",
              "tokenizer": "sudachi_tokenizer",
              "filter": [
                "test"
              ]
            }
          },
          "tokenizer": {
            "sudachi_tokenizer": {
              "type": "sudachi_tokenizer",
              "split_mode": "C"
            }
          },
          "filter": {
            "test": {
              "type": "sudachi_split",
              "split_mode": "A"
            }
          }
        }
      }
    """.jsonSettings()
    val analyzers = engine.indexAnalyzers(settings)
    val basic = analyzers.get("sudachi_test")
    basic.assertTerms("東京都にふく", "東京都", "東京", "都", "に", "ふく")
  }

  @Test
  fun stopwordsInline() {
    val settings =
        """
      {
        "index.analysis": {          
          "analyzer": {
            "sudachi_test": {
              "type": "custom",
              "tokenizer": "sudachi_tokenizer",
              "filter": [
                "test"
              ]
            }
          },
          "tokenizer": {
            "sudachi_tokenizer": {
              "type": "sudachi_tokenizer",
              "split_mode": "C"
            }
          },
          "filter": {
            "test": {
              "type": "sudachi_ja_stop",
              "stopwords": ["に", "ふく"]
            }
          }
        }
      }
    """.jsonSettings()
    val analyzers = engine.indexAnalyzers(settings)
    val basic = analyzers.get("sudachi_test")
    basic.assertTerms("東京にふく", "東京")
  }

  @Test
  fun stopwordsInlineNonTrailing() {
    val settings =
        """
      {
        "index.analysis": {          
          "analyzer": {
            "sudachi_test": {
              "type": "custom",
              "tokenizer": "sudachi_tokenizer",
              "filter": [
                "test"
              ]
            }
          },
          "tokenizer": {
            "sudachi_tokenizer": {
              "type": "sudachi_tokenizer",
              "split_mode": "C"
            }
          },
          "filter": {
            "test": {
              "type": "sudachi_ja_stop",
              "stopwords": ["に", "ふく"],
              "remove_trailing": false
            }
          }
        }
      }
    """.jsonSettings()
    val analyzers = engine.indexAnalyzers(settings)
    val basic = analyzers.get("sudachi_test")
    basic.assertTerms("東京にふく", "東京", "ふく")
  }
}
