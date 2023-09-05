# How to use user-provided dictionaries in Sudachi inside ElasticSearch or OpenSearch

You can use user dictionaries to improve analysis accuracy.
You prepare the dictionary in the csv format, compile it into binary form and set up index using (possibly multiple)
user dictionaries in addition to the system dictionary.

**IMPORTANT**: You compile a user dictionary with respect to a system dictionary.
The compiled user dictionary will contain references to the system dictionary it was compiled with.
If you later will use the binary user dictionary with a different system dictionary, you can have very strange errors in
analysis.

# Preparations

* You can already set up ES/OS instance with Sudachi plugin
* Sudachi Java jar (for dictionary compilation)
* Binary system dictionary (we will
  use [core dictionary](http://sudachi.s3-website-ap-northeast-1.amazonaws.com/sudachidict/) from the latest release)
* CSV file of user dictionary you want to use

## CSV of user dictionary

We will use a test CSV dictionary [consisting of a single word](./materials/user_test.csv).
Suppose, however, that for some reason instead of the default analysis for 国立博物館 (国立/博物館) we want to have
split (国立/博/物/館).

In this case, we create single token 国立博物館 and provide a custom A level split for it.

User dictionary has the following columns:
1. Surface form (it will be indexed). 
   Some characters will produce non-lookupable entries because of input normalization which is not done during dictionary compilation.
2. Left connection id {i16} (consult the system dictionary csv to select a good value for a similar part of speech)
3. Right connection id {i16}
4. Word cost {i16, can be negative} (lower values will have higher priority)
5. Word form for display (legacy field, is not used for display)
6. POS level 1
7. POS level 2
8. POS level 3
9. POS level 4
10. Conjugation type
11. Conjugation form
12. Reading form
13. Normalized form
14. Dictionary form reference
15. Word type, can be A, B or C
16. A split (how to split this token to A-level tokens, `/`-separated list of references)
17. B split (how to split this token to B-level tokens, `/`-separated list of references))
18. Word structure (`/`-separated list of references)
19. List of synonym groups (used by chikkar plugin)

Sudachi uses [RFC 4180](https://datatracker.ietf.org/doc/html/rfc4180) format for CSV with an extension
that strings can contain escaped unicode characters in `\U000a` form (must have exactly 4 hexadecimal characters)
or `\U{12345}` form (can contain any unicode codepoint in hex, zeros are optional).
Use this extension if you would need to form a reference to `"`, `,` or `/` characters.

### Word references

Word reference is an 8-tuple separated by "," of (surface, 6 fields of POS, reading).
All 8 fields must match equivalent fields of either a system or the current user dictionary.
Reference will be resolved to the first entry from system, and then user dictionary, 
if the entry is not present in the system dictionary.

# Process

1. Compile CSV of user dictionary to the binary format
1. Put the compiled user dictionary into the `<es>/config/sudachi`
1. Update index settings to refer the newly created config file

## Compile binary user dictionary

```bash
java -Dfile.encoding=UTF-8 -cp sudachi-*.jar \
    com.worksap.nlp.sudachi.dictionary.UserDictionaryBuilder \
    -o user_test.dic -s system_core.dic \
    -d 'my first user dictionary' \
    input.csv
```

## Prepare the config directory

ElasticSearch forbids plugins to read everywhere except the `config` directory.
You need to place the compiled user dictionary into that folder.
Because `sudachi-analysis` plugin resolves all paths inside configuration relatively to
`<es>/config/sudachi` directory, we recommend placing the dictionary into that directory.

```bash
cp user_test.dic `<es>/config/sudachi/user_test.dic`
```

## Create or update index to use user dictionary

Let's create three indices: 
* Baseline which does not use the user dictionary
* Which uses the user dictionary and C split mode
* Which uses the user dictionary and A split mode

### Baseline: Do not use user dictionary
```bash
curl -X PUT "localhost:9200/test_sudachi_nouser" -H 'Content-Type: application/json' -d @- <<EOF
{
  "settings": {
    "analysis": {
      "tokenizer" : {
        "sudachi_tokenizer": {
            "type": "sudachi_tokenizer"          
        }
      }
    }
  }
}
EOF
```
### User Dictionary/C split mode (use long tokens)
We specify user dictionary as an `additional_settings` key in the index configuration.
The configuration works both for `sudachi_tokenizer` (if creating a custom analyzer chain)
or for `sudachi_analyzer` if using a simple configuration of the analysis.

```bash
curl -X PUT "localhost:9200/test_sudachi_user" -H 'Content-Type: application/json' -d @- <<EOF
{
  "settings": {
    "analysis": {
      "tokenizer" : {
        "sudachi_tokenizer": {
            "type": "sudachi_tokenizer",
            "additional_settings": "{\"userDict\": [\"user_test.dic\"]}"          
        }
      }
    }
  }
}
EOF
```
### User Dictionary/A split mode (use short tokens)
```bash
curl -X PUT "localhost:9200/test_sudachi_user_a" -H 'Content-Type: application/json' -d @- <<EOF
{
  "settings": {
    "analysis": {
      "tokenizer" : {
        "sudachi_tokenizer": {
            "type": "sudachi_tokenizer",
            "additional_settings": "{\"userDict\": [\"user_test.dic\"]}",
            "split_mode": "A"          
        }
      }
    }
  }
}
EOF
```

Creating all indices should return acknowledgement like
```json
{"acknowledged":true,"shards_acknowledged":true,"index":"test_sudachi_nouser"}
```

## Test Analysis

Let's use analysis test API to check analysis.

First we check the baseline:

```bash
curl -X GET "localhost:9200/test_sudachi_nouser/_analyze?pretty" -H 'Content-Type: application/json' -d'{"tokenizer":"sudachi_tokenizer", "text" : "国立博物館"}'
```

```json
{
  "tokens" : [
    {
      "token" : "国立",
      "start_offset" : 0,
      "end_offset" : 2,
      "type" : "word",
      "position" : 0
    },
    {
      "token" : "博物館",
      "start_offset" : 2,
      "end_offset" : 5,
      "type" : "word",
      "position" : 1
    }
  ]
}
```
It produces two tokens as it should.

User/C tokenizer should return a single token:
```bash
curl -X GET "localhost:9200/test_sudachi_nouser/_analyze?pretty" -H 'Content-Type: application/json' -d'{"tokenizer":"sudachi_tokenizer", "text" : "国立博物館"}'
```

```json
{
  "tokens" : [
    {
      "token" : "国立博物館",
      "start_offset" : 0,
      "end_offset" : 5,
      "type" : "word",
      "position" : 0
    }
  ]
}
```

And User/A tokenizer should return four tokens:
```bash
curl -X GET "localhost:9200/test_sudachi_nouser/_analyze?pretty" -H 'Content-Type: application/json' -d'{"tokenizer":"sudachi_tokenizer", "text" : "国立博物館"}'
```
```json
{
  "tokens" : [
    {
      "token" : "国立",
      "start_offset" : 0,
      "end_offset" : 2,
      "type" : "word",
      "position" : 0
    },
    {
      "token" : "博",
      "start_offset" : 2,
      "end_offset" : 3,
      "type" : "word",
      "position" : 1
    },
    {
      "token" : "物",
      "start_offset" : 3,
      "end_offset" : 4,
      "type" : "word",
      "position" : 2
    },
    {
      "token" : "館",
      "start_offset" : 4,
      "end_offset" : 5,
      "type" : "word",
      "position" : 3
    }
  ]
}
```