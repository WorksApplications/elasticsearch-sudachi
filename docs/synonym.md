# Using SudachiDict Synonyms

Here we describe a temporary way to use Sudachi Dictionary's synonym resource ([Sudachi 同義語辞書](https://github.com/WorksApplications/SudachiDict/blob/develop/docs/synonyms.md)) with Elasticsearch. We plan to create a dedicated Sudachi synonym filter for Elasticsearch in the future.

You can convert the synonym file to "Solr synonyms" format, and use it via Elasticsearch's default synonym filters.


## Format Converion

You can simply convert the Sudachi synonym file into the Solr synonyms format. The Sudachi format is described in detail [here](https://github.com/WorksApplications/SudachiDict/blob/develop/docs/synonyms.md).

Here is an example script for conversion;

```py
import argparse
import fileinput

def main():
    parser = argparse.ArgumentParser(prog="ssyn2es.py", description="convert Sudachi synonyms to ES")
    parser.add_argument('files', metavar='FILE', nargs='*', help='files to read, if empty, stdin is used')
    parser.add_argument('-p', '--output-predicate', action='store_true', help='output predicates')
    args = parser.parse_args()

    synonyms = {}
    with fileinput.input(files = args.files) as input:
        for line in input:
            line = line.strip()
            if line == "":
                continue
            entry = line.split(",")[0:9]
            if entry[2] == "2" or (not args.output_predicate and entry[1] == "2"):
                continue
            group = synonyms.setdefault(entry[0], [[], []])
            group[1 if entry[2] == "1" else 0].append(entry[8])

    for groupid in sorted(synonyms):
        group = synonyms[groupid]
        if not group[1]:
            if len(group[0]) > 1:
                print(",".join(group[0]))
        else:
            if len(group[0]) > 0 and len(group[1]) > 0:
                print(",".join(group[0]) + "=>" + ",".join(group[0] + group[1]))


if __name__ == "__main__":
    main()
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