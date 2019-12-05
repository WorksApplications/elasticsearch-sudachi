# analysis-sudachi

analysis-sudachi is an Elasticsearch plugin for tokenization of Japanese text using Sudachi the Japanese morphological analyzer.

[![Build Status](https://travis-ci.org/WorksApplications/elasticsearch-sudachi.svg?branch=develop)](https://travis-ci.org/WorksApplications/elasticsearch-sudachi)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=com.worksap.nlp%3Aanalysis-sudachi&metric=alert_status)](https://sonarcloud.io/dashboard/index/com.worksap.nlp%3Aanalysis-sudachi)

# What's new?

- version 1.3.2
    - Upgrade sudachi morphological analyzer to 0.3.1

- version 1.3.1
    - Upgrade sudachi morphological analyzer to 0.3.0
    - Minor bug fix

- version 1.3.0
    - Upgrade sudachi morphological analyzer to 0.2.0
    - Import sudachi from maven central repository
    - Minor bug fix

- version 1.2.0
    - Upgrading sudachi morphological analyzer to 0.2.0-SNAPSHOT
    - New filter `sudachi_normalizedform` was added; see [sudachi_normalizedform](#sudachi_normalizedform)
    - Default normalization behavior was changed; neather baseform filter and normalziedform filter not applied
    - `sudachi_readingform` filter was changed with new romaji mappings based on MS-IME


- version 1.1.0
    - `part-of-speech forward matching` is available on `stoptags`; see [sudachi_part_of_speech](#sudachi_part_of_speech)

- version 1.0.0
    - first release

# Build

1. Build analysis-sudachi.
```
   $ mvn package
```

# Installation

1. Download analysis-sudachi-elasticsearch zip archive file
2. Move current dir to $ES_HOME
3. Execute "bin/elasticsearch-plugin install file:///plugin-zip-path"
4. Download sudachi dictionary archive from https://github.com/WorksApplications/SudachiDict
5. Extract dic file and place it to config/sudachi_tokenizer/system_core.dic
6. Execute "bin/elasticsearch"

# Configuration

- tokenizer: Select tokenizer. (sudachi) (string)
- mode: Select mode. (normal or search or extended) (string, default: search)
	- normal: Regular segmentataion. (Use C mode of Sudachi)  
      Ex) 関西国際空港 / アバラカダブラ
	- search: Additional segmentation useful for search. (Use C and A mode)  
	  Ex）関西国際空港, 関西, 国際, 空港 / アバラカダブラ
	- extended: Similar to search mode, but also unigram unknown words.  
	  Ex）関西国際空港, 関西, 国際, 空港 / アバラカダブラ, ア, バ, ラ, カ, ダ, ブ, ラ
- discard\_punctuation: Select to discard punctuation or not. (bool, default: true)
- settings\_path: Sudachi setting file path. The path may be absolute or relative; relative paths are resolved with respect to ES\_HOME. (string, default: null)
- resources_path: Sudachi dictionary path. The path may be absolute or relative; relative paths are resolved with respect to ES\_HOME. (string, default: null)

## Example
```json
{
  "settings": {
    "index": {
      "analysis": {
        "tokenizer": {
          "sudachi_tokenizer": {
            "type": "sudachi_tokenizer",
            "mode": "search",
	    "discard_punctuation": true,
            "resources_path": "/etc/elasticsearch/sudachi"
          }
        },
        "analyzer": {
          "sudachi_analyzer": {
            "filter": [],
            "tokenizer": "sudachi_tokenizer",
            "type": "custom"
          }
        }
      }
    }
  }
}
```

# Filters

## sudachi\_part\_of\_speech

The sudachi\_part\_of\_speech token filter removes tokens that match a set of part-of-speech tags. It accepts the following setting:

The `stopatgs` is an array of part-of-speech and/or inflection tags that should be removed. It defaults to the stoptags.txt file embedded in the lucene-analysis-sudachi.jar.

Sudachi POS information is a csv list, consisting 6 items;

- 1-4 `part-of-speech hierarchy (品詞階層)`
- 5 `inflectional type (活用型)`
- 6 `inflectional form (活用形)`

With the `stoptags`, you can filter out the result in any of these forward matching forms;

- 1 - e.g., `名詞`
- 1,2 - e.g., `名詞,固有名詞`
- 1,2,3 - e.g., `名詞,固有名詞,地名`
- 1,2,3,4 - e.g., `名詞,固有名詞,地名,一般`
- 5 - e.g., `五段-カ行`
- 6 - e.g., `終止形-一般`
- 5,6 - e.g., `五段-カ行,終止形-一般`

### PUT sudachi_sample
```json
{
  "settings": {
    "index": {
      "analysis": {
        "tokenizer": {
          "sudachi_tokenizer": {
            "type": "sudachi_tokenizer",
            "resources_path": "/etc/elasticsearch/sudachi"
          }
        },
        "analyzer": {
          "sudachi_analyzer": {
            "filter": [
              "my_posfilter"
	    ],
            "tokenizer": "sudachi_tokenizer",
            "type": "custom"
          }
        },
        "filter":{
         "my_posfilter":{
          "type":"sudachi_part_of_speech",
          "stoptags":[
           "助詞",
           "助動詞",
           "補助記号,句点",
           "補助記号,読点"
          ]
         }
        }
      }
    }
  }
}
```

### POST sudachi_sample
```json
{
    "analyzer":"sudachi_analyzer",
    "text":"寿司がおいしいね"
}
```

### Which responds with:
```json
{
    "tokens": [
        {
            "token": "寿司",
            "start_offset": 0,
            "end_offset": 2,
            "type": "word",
            "position": 0
        },
        {
            "token": "美味しい",
            "start_offset": 3,
            "end_offset": 7,
            "type": "word",
            "position": 2
        }
    ]
}
```

## sudachi\_ja\_stop

The sudachi\_ja\_stop token filter filters out Japanese stopwords (_japanese_), and any other custom stopwords specified by the user. This filter only supports the predefined _japanese_ stopwords list. If you want to use a different predefined list, then use the stop token filter instead.

### PUT sudachi_sample
```json
{
  "settings": {
    "index": {
      "analysis": {
        "tokenizer": {
          "sudachi_tokenizer": {
            "type": "sudachi_tokenizer",
            "resources_path": "/etc/elasticsearch/sudachi"
          }
        },
        "analyzer": {
          "sudachi_analyzer": {
            "filter": [
              "my_stopfilter"
	    ],
            "tokenizer": "sudachi_tokenizer",
            "type": "custom"
          }
        },
        "filter":{
         "my_stopfilter":{
          "type":"sudachi_ja_stop",
          "stoptags":[
            "_japanese_",
            "は",
            "です"
          ]
         }
        }
      }
    }
  }
}
```

### POST sudachi_sample
```json
{
 "analyzer":"sudachi_analyzer",
 "text":"私は宇宙人です。"
}
```

### Which responds with:
```json
{
    "tokens": [
        {
            "token": "私",
            "start_offset": 0,
            "end_offset": 1,
            "type": "word",
            "position": 0
        },
        {
            "token": "宇宙",
            "start_offset": 2,
            "end_offset": 4,
            "type": "word",
            "position": 2
        },
        {
            "token": "人",
            "start_offset": 4,
            "end_offset": 5,
            "type": "word",
            "position": 3
        }
    ]
}
```

## sudachi\_baseform

The sudachi\_baseform token filter replaces terms with their SudachiBaseFormAttribute. This acts as a lemmatizer for verbs and adjectives.

### PUT sudachi_sample
```json
{
  "settings": {
    "index": {
      "analysis": {
        "tokenizer": {
          "sudachi_tokenizer": {
            "type": "sudachi_tokenizer",
            "resources_path": "/etc/elasticsearch/sudachi"
          }
        },
        "analyzer": {
          "sudachi_analyzer": {
            "filter": [
              "sudachi_baseform"
            ],
            "tokenizer": "sudachi_tokenizer",
            "type": "custom"
          }
        }
      }
    }
  }
}
```

### POST sudachi_sample
```json
{
  "analyzer": "sudachi_analyzer",
  "text": "飲み"
}
```

### Which responds with:
```json
{
    "tokens": [
        {
            "token": "飲む",
            "start_offset": 0,
            "end_offset": 2,
            "type": "word",
            "position": 0
        }
    ]
}
```

## sudachi\_normalizedform

The sudachi\_normalizedform token filter replaces terms with their SudachiNormalizedFormAttribute. This acts as a normalizer for spelling variants.

This filter lemmatizes verbs and adjectives too. You don't need to use sudachi\_baseform filter with this filter.

### PUT sudachi_sample
```json
{
  "settings": {
    "index": {
      "analysis": {
        "tokenizer": {
          "sudachi_tokenizer": {
            "type": "sudachi_tokenizer",
            "resources_path": "/etc/elasticsearch/sudachi"
          }
        },
        "analyzer": {
          "sudachi_analyzer": {
            "filter": [
              "sudachi_normalizedform"
            ],
            "tokenizer": "sudachi_tokenizer",
            "type": "custom"
          }
        }
      }
    }
  }
}
```

### POST sudachi_sample
```json
{
  "analyzer": "sudachi_analyzer",
  "text": "呑み"
}
```

### Which responds with:
```json
{
    "tokens": [
        {
            "token": "飲む",
            "start_offset": 0,
            "end_offset": 2,
            "type": "word",
            "position": 0
        }
    ]
}
```

## sudachi\_readingform

Convert to katakana or romaji reading.
The sudachi\_readingform token filter replaces the token with its reading form in either katakana or romaji. It accepts the following setting:

### use_romaji

Whether romaji reading form should be output instead of katakana. Defaults to false.

When using the pre-defined sudachi_readingform filter, use_romaji is set to true. The default when defining a custom sudachi_readingform, however, is false. The only reason to use the custom form is if you need the katakana reading form:

### PUT sudachi_sample
```json
{
    "settings": {
        "index": {
            "analysis": {
                "filter": {
                    "romaji_readingform": {
                        "type": "sudachi_readingform",
                        "use_romaji": true
                    },
                    "katakana_readingform": {
                        "type": "sudachi_readingform",
                        "use_romaji": false
                    }
                },
                "tokenizer": {
                    "sudachi_tokenizer": {
                        "type": "sudachi_tokenizer",
                        "resources_path": "/etc/elasticsearch/sudachi"
                    }
                },
                "analyzer": {
                    "romaji_analyzer": {
                        "tokenizer": "sudachi_tokenizer",
                        "filter": [
                            "romaji_readingform"
                        ]
                    },
                    "katakana_analyzer": {
                        "tokenizer": "sudachi_tokenizer",
                        "filter": [
                            "katakana_readingform"
                        ]
                    }
                }
            }
        }
    }
}
```

### POST sudachi_sample

```json
{
  "analyzer": "katakana_analyzer",
  "text": "寿司"
}
```
Returns `スシ`.

```
{
  "analyzer": "romaji_analyzer",
  "text": "寿司"
}
```
Returns `susi`.

# License

Copyright (c) 2017-2019 Works Applications Co., Ltd.
Originally under elasticsearch, https://www.elastic.co/jp/products/elasticsearch
Originally under lucene, https://lucene.apache.org/
