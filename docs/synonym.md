# Using SudachiDict Synonyms

Here we describe a temporary way to use Sudachi Dictionary's synonym resource ([Sudachi 同義語辞書](https://github.com/WorksApplications/SudachiDict/blob/develop/docs/synonyms.md)) with Elasticsearch. We plan to create a dedicated Sudachi synonym filter for Elasticsearch in the future.

You can convert the synonym file to "Solr synonyms" format, and use it via Elasticsearch's default synonym filters.


## Format Converion

You can simply convert the Sudachi synonym file into the Solr synonyms format. The Sudachi format is described in detail [here](https://github.com/WorksApplications/SudachiDict/blob/develop/docs/synonyms.md).

You can use [our example script (ssyn2es.py)](./ssyn2es.py) for the conversion;

```sh
$ python ssyn2es.py SudachiDict/src/main/text/synonyms.txt > synonym.txt
```

### Expansion Suppresion

You can partially make use of the Sudachi synonym resource's detailed information with the Solr format's `=>` notation, which controls the expansion direction.

```
# synonym entry
アイスクリーム,ice cream,ice=>アイスクリーム,ice cream,ice,アイス

# expansion example
# `アイスクリーム` => `アイスクリーム`, `ice cream`, `ice`, `アイス`
# `アイス` => `アイス` (**no expansion**)
```

### Punctuation Symbols

You may need to remove certain synonym words such as `€` and `＆` when you use the analyzer with setting `"discard_punctuation": true` (Otherwise you will be get an error, e.g., `"term: € was completely eliminated by analyzer"`). Alternatively, you can set `"lenient": true` for the synonym filter to ignore the exceptions.

These symbols are defined as punctuations; See [SudachiTokenizer.java](https://github.com/WorksApplications/elasticsearch-sudachi/blob/develop/src/main/java/com/worksap/nlp/lucene/sudachi/ja/SudachiTokenizer.java#L140) for the detail.


## Synonym Filter

You can use the converted Solr format file with Elasticsearch's default synonym filters, [Synonym token filter](https://www.elastic.co/guide/en/elasticsearch/reference/current/analysis-synonym-tokenfilter.html) or [Synonym graph filter](https://www.elastic.co/guide/en/elasticsearch/reference/current/analysis-synonym-graph-tokenfilter.html).


### Example: Set up

```json
{
    "settings": {
        "analysis": {
            "filter": {
                "sudachi_synonym": {
                    "type": "synonym_graph",
                    "synonyms_path": "sudachi/synonym.txt"
                }
            },
            "tokenizer": {
                "sudachi_tokenizer": {
                    "type": "sudachi_tokenizer"
                }
            },
            "analyzer": {
                "sudachi_synonym_analyzer": {
                    "type": "custom",
                    "tokenizer": "sudachi_tokenizer",
                    "filter": [
                        "sudachi_synonym"
                    ]
                }
            }
        }
    }
}
```

Here we assume that the converted synonym file is placed as `$ES_PATH_CONF/sudachi/synonym.txt`.

If you would like to use `sudachi_split` filter, set it *after* the synonym filter (otherwise you will get an error, e.g., `term: 不明確 analyzed to a token (不) with position increment != 1 (got: 0)`).


### Example: Analysis

#### Case 1.

```json
{
  "analyzer": "sudachi_synonym_analyzer",
  "text": "アイスクリーム"
}
```

Returns 

```json
{
    "tokens": [
        {
            "token": "アイスクリーム",
            "start_offset": 0,
            "end_offset": 7,
            "type": "SYNONYM",
            "position": 0
        },
        {
            "token": "ice cream",
            "start_offset": 0,
            "end_offset": 7,
            "type": "SYNONYM",
            "position": 0
        },
        {
            "token": "ice",
            "start_offset": 0,
            "end_offset": 7,
            "type": "SYNONYM",
            "position": 0
        },
        {
            "token": "アイス",
            "start_offset": 0,
            "end_offset": 7,
            "type": "SYNONYM",
            "position": 0
        }
    ]
}
```

#### Case 2.

```json
{
    "analyzer": "sudachi_synonym_analyzer",
    "text": "アイス"
}
```

Returns 

```json
{
    "tokens": [
        {
            "token": "アイス",
            "start_offset": 0,
            "end_offset": 3,
            "type": "word",
            "position": 0
        }
    ]
}
```