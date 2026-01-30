import argparse
import json
import os
from pathlib import Path
import unittest
import urllib3


def should_run_test() -> bool:
    # define based on `buildSrc/**/EsTestEnvPlugin.groovy`
    es_kind = os.environ.get("ES_KIND", "elasticsearch")
    es_version = os.environ.get("ES_VERSION", "8.2.3")

    if es_kind == "elasticsearch":
        vers = list(map(int, es_version.split(".")))
        if vers[0] == 7 and vers[1] < 14:
            return False
        if vers[0] == 8 and vers[1] < 5:
            return False
        return True
    elif es_kind == "opensearch":
        return True
    else:
        raise ValueError(f"not supported search engine: {es_kind}")


def has_security_manager() -> bool:
    """Check if the current environment uses Java Security Manager.
    OpenSearch 3.0+ uses Java agent-based security instead of Security Manager."""
    es_kind = os.environ.get("ES_KIND", "elasticsearch")
    es_version = os.environ.get("ES_VERSION", "8.2.3")

    if es_kind == "opensearch":
        vers = list(map(int, es_version.split(".")))
        # OpenSearch 3.0+ doesn't use Security Manager
        if vers[0] >= 3:
            return False
    return True


def parse_args():
    p = argparse.ArgumentParser(
        description="runs tests migrated from :integraion")
    p.add_argument("--port", type=int, default=9200)
    p.add_argument("--host", default="http://localhost")
    p.add_argument("--index", default="test_sudachi")
    return p.parse_args()


def main():
    if not should_run_test():
        return

    global es_instance
    es_instance = ElasticSearch(parse_args())
    unittest.main()
    return


es_instance = None


class ElasticSearch(object):
    def __init__(self, args):
        self.base_url = f"{args.host}:{args.port}"
        self.index_url = f"{self.base_url}/{args.index}"
        self.mgr = urllib3.PoolManager()

    def analyze(self, body):
        r = self.mgr.urlopen(
            "GET",
            f"{self.base_url}/_analyze",
            headers={"Content-Type": "application/json"},
            body=json.dumps(body),
        )
        return r


class TestBasic(unittest.TestCase):
    def test_can_instanciate_sudachi_analyzer(self):
        body = {"analyzer": "sudachi", "text": ""}
        resp = es_instance.analyze(body)
        self.assertEqual(200, resp.status)
        return

    def test_tokenize_using_sudachi_tokenizer(self):
        body = {"tokenizer": "sudachi_tokenizer", "text": "京都に行った"}
        resp = es_instance.analyze(body)
        self.assertEqual(200, resp.status)

        tokens = json.loads(resp.data)["tokens"]
        self.assertEqual(4, len(tokens))
        self.assertEqual("京都", tokens[0]["token"])
        self.assertEqual(0, tokens[0]["position"])
        self.assertEqual(0, tokens[0]["start_offset"])
        self.assertEqual(2, tokens[0]["end_offset"])

        self.assertEqual("に", tokens[1]["token"])
        self.assertEqual(1, tokens[1]["position"])
        self.assertEqual(2, tokens[1]["start_offset"])
        self.assertEqual(3, tokens[1]["end_offset"])

        self.assertEqual("行っ", tokens[2]["token"])
        self.assertEqual(2, tokens[2]["position"])
        self.assertEqual(3, tokens[2]["start_offset"])
        self.assertEqual(5, tokens[2]["end_offset"])

        self.assertEqual("た", tokens[3]["token"])
        self.assertEqual(3, tokens[3]["position"])
        self.assertEqual(5, tokens[3]["start_offset"])
        self.assertEqual(6, tokens[3]["end_offset"])
        return

    def test_explain_tokenizer_details(self):
        body = {"tokenizer": "sudachi_tokenizer",
                "text": "すだち", "explain": True}
        resp = es_instance.analyze(body)
        self.assertEqual(200, resp.status)

        morpheme = json.loads(resp.data)[
            "detail"]["tokenizer"]["tokens"][0]["morpheme"]
        self.assertIn("surface", morpheme)
        self.assertEqual("すだち", morpheme["surface"])
        self.assertIn("dictionaryForm", morpheme)
        self.assertEqual("すだち", morpheme["dictionaryForm"])
        self.assertIn("normalizedForm", morpheme)
        self.assertEqual("酢橘", morpheme["normalizedForm"])
        self.assertIn("readingForm", morpheme)
        self.assertEqual("スダチ", morpheme["readingForm"])
        self.assertIn("partOfSpeech", morpheme)
        self.assertEqual(["名詞", "普通名詞", "一般", "*", "*", "*"],
                         morpheme["partOfSpeech"])
        return


class TestICUFiltered(unittest.TestCase):
    # requires analysis-icu plugin installed
    def test_icu_filtered_stuff_is_not_trimmed(self):
        body = {
            "tokenizer": "sudachi_tokenizer",
            "char_filter": {
                "type": "icu_normalizer",
                "name": "nfkc_cf",
                "mode": "compose"
            },
            "text": "white",
        }
        resp = es_instance.analyze(body)
        self.assertEqual(200, resp.status, f"data: {resp.data}")

        tokens = json.loads(resp.data.decode())["tokens"]
        self.assertEqual(1, len(tokens))
        self.assertEqual("white", tokens[0]["token"])
        self.assertEqual(0, tokens[0]["position"])
        self.assertEqual(0, tokens[0]["start_offset"])
        self.assertEqual(5, tokens[0]["end_offset"])
        return

    def test_correct_split_offset_with_icu_filter(self):
        body = {
            "tokenizer": "sudachi_tokenizer",
            "char_filter": {
                "type": "icu_normalizer",
                "name": "nfkc_cf",
                "mode": "compose"
            },
            "filter": {
                "type": "sudachi_split",
                "mode": "search"
            },
            "text": "六三四㍿のアッフ\u309Aルハ\u309Aイ",
        }
        resp = es_instance.analyze(body)
        self.assertEqual(200, resp.status, f"data: {resp.data}")

        tokens = json.loads(resp.data.decode())["tokens"]
        self.assertEqual(8, len(tokens))
        self.assertEqual("株式会社", tokens[1]["token"])
        self.assertEqual(1, tokens[1]["position"])
        self.assertEqual(2, tokens[1]["positionLength"])
        self.assertEqual(3, tokens[1]["start_offset"])
        self.assertEqual(4, tokens[1]["end_offset"])

        self.assertEqual("株式", tokens[2]["token"])
        self.assertEqual(1, tokens[2]["position"])
        self.assertEqual(3, tokens[2]["start_offset"])
        self.assertEqual(3, tokens[2]["end_offset"])
        self.assertEqual("会社", tokens[3]["token"])
        self.assertEqual(2, tokens[3]["position"])
        self.assertEqual(3, tokens[3]["start_offset"])
        self.assertEqual(4, tokens[3]["end_offset"])

        self.assertEqual("アップルパイ", tokens[5]["token"])
        self.assertEqual(4, tokens[5]["position"])
        self.assertEqual(2, tokens[1]["positionLength"])
        self.assertEqual(5, tokens[5]["start_offset"])
        self.assertEqual(13, tokens[5]["end_offset"])

        self.assertEqual("アップル", tokens[6]["token"])
        self.assertEqual(4, tokens[6]["position"])
        self.assertEqual(5, tokens[6]["start_offset"])
        self.assertEqual(10, tokens[6]["end_offset"])
        self.assertEqual("パイ", tokens[7]["token"])
        self.assertEqual(5, tokens[7]["position"])
        self.assertEqual(10, tokens[7]["start_offset"])
        self.assertEqual(13, tokens[7]["end_offset"])
        return

    def test_correct_OOV_offset_with_icu_filter(self):
        body = {
            "tokenizer": "sudachi_tokenizer",
            "char_filter": {
                "type": "icu_normalizer",
                "name": "nfkc_cf",
                "mode": "compose"
            },
            "filter": {
                "type": "sudachi_split",
                "mode": "extended"
            },
            "text": "10㍉㌢進んでホ\u3099ホ\u3099ホ\u3099",
        }
        resp = es_instance.analyze(body)
        self.assertEqual(200, resp.status, f"data: {resp.data}")

        tokens = json.loads(resp.data.decode())["tokens"]
        self.assertEqual(13, len(tokens))
        self.assertEqual("ミリセンチ", tokens[1]["token"])
        self.assertEqual(1, tokens[1]["position"])
        self.assertEqual(5, tokens[1]["positionLength"])
        self.assertEqual(2, tokens[1]["start_offset"])
        self.assertEqual(4, tokens[1]["end_offset"])

        self.assertEqual("ミ", tokens[2]["token"])
        self.assertEqual(1, tokens[2]["position"])
        self.assertEqual(2, tokens[2]["start_offset"])
        self.assertEqual(2, tokens[2]["end_offset"])
        self.assertEqual("リ", tokens[3]["token"])
        self.assertEqual(2, tokens[3]["position"])
        self.assertEqual(2, tokens[3]["start_offset"])
        self.assertEqual(3, tokens[3]["end_offset"])
        self.assertEqual("セ", tokens[4]["token"])
        self.assertEqual(3, tokens[4]["position"])
        self.assertEqual(3, tokens[4]["start_offset"])
        self.assertEqual(3, tokens[4]["end_offset"])
        self.assertEqual("ン", tokens[5]["token"])
        self.assertEqual(4, tokens[5]["position"])
        self.assertEqual(3, tokens[5]["start_offset"])
        self.assertEqual(3, tokens[5]["end_offset"])
        self.assertEqual("チ", tokens[6]["token"])
        self.assertEqual(5, tokens[6]["position"])
        self.assertEqual(3, tokens[6]["start_offset"])
        self.assertEqual(4, tokens[6]["end_offset"])

        self.assertEqual("ボボボ", tokens[9]["token"])
        self.assertEqual(8, tokens[9]["position"])
        self.assertEqual(3, tokens[9]["positionLength"])
        self.assertEqual(7, tokens[9]["start_offset"])
        self.assertEqual(13, tokens[9]["end_offset"])

        self.assertEqual("ボ", tokens[10]["token"])
        self.assertEqual(8, tokens[10]["position"])
        self.assertEqual(7, tokens[10]["start_offset"])
        self.assertEqual(9, tokens[10]["end_offset"])
        self.assertEqual("ボ", tokens[11]["token"])
        self.assertEqual(9, tokens[11]["position"])
        self.assertEqual(9, tokens[11]["start_offset"])
        self.assertEqual(11, tokens[11]["end_offset"])
        self.assertEqual("ボ", tokens[12]["token"])
        self.assertEqual(10, tokens[12]["position"])
        self.assertEqual(11, tokens[12]["start_offset"])
        self.assertEqual(13, tokens[12]["end_offset"])
        return


class TestSubplugin(unittest.TestCase):
    # requires :subplugin is installed with :testlib
    # requires test dictionary from `src/test/resources/dict/``
    def test_loads_config_and_plugin_from_subplugin(self):
        body = {
            "tokenizer": {
                "type": "sudachi_tokenizer",
                "settings_path": "sudachi_subplugin.json",
                # override to use test dictionary
                "additional_settings": "{\"systemDict\":\"system_test.dic\"}",
            },
            "text": "ゲゲゲの鬼太郎",
        }
        resp = es_instance.analyze(body)
        self.assertEqual(200, resp.status, f"data: {resp.data}")

        tokens = json.loads(resp.data)["tokens"]
        self.assertEqual(1, len(tokens), f"{tokens}")
        self.assertEqual("ゲゲゲの鬼太郎", tokens[0]["token"])
        self.assertEqual(0, tokens[0]["position"])
        self.assertEqual(0, tokens[0]["start_offset"])
        self.assertEqual(7, tokens[0]["end_offset"])
        return


class TestSecurityManager(unittest.TestCase):
    def test_fail_loading_files_outside_configdir(self):
        # Skip this test on OpenSearch 3.0+ which uses Java agent-based security
        # instead of Security Manager
        if not has_security_manager():
            self.skipTest("Security Manager is not available (OpenSearch 3.0+ uses agent-based security)")

        test_sudachi_config = Path(__file__).parent / "sudachi.json"
        self.assertTrue(test_sudachi_config.exists(),
                        f"config file should exists: {test_sudachi_config}")

        body = {
            "tokenizer": {
                "type": "sudachi_tokenizer",
                "settings_path": str(test_sudachi_config.resolve()),
            },
            "text": "",
        }
        resp = es_instance.analyze(body)

        self.assertEqual(500, resp.status, f"data: {resp.data}")
        err = json.loads(resp.data)["error"]
        # sudachi raises notfound error when SecurityException occurs during file search
        self.assertTrue(err["reason"].startswith(
            "com.worksap.nlp.sudachi.Config$Resource$NotFound"))
        return


class TestWithCommonFilter(unittest.TestCase):
    def test_synonym_then_normalizedform(self):
        body = {
            "tokenizer": {
                "type": "sudachi_tokenizer",
            },
            "filter": [
                {
                    "type": "synonym_graph",
                    "synonyms": ["すだち, みかん"]
                },
                "sudachi_normalizedform",
            ],
            "text": "すだちを食べた",
        }
        resp = es_instance.analyze(body)
        self.assertEqual(200, resp.status, f"data: {resp.data}")

        tokens = json.loads(resp.data)["tokens"]
        # synonym filter inserts synonym beforehand.
        # it is not normalized since it does not have morpheme.
        self.assertListEqual(["みかん", "酢橘", "を", "食べる", "た"],
                             [t["token"] for t in tokens])
        return

    def test_normalizedform_then_synonym(self):
        body = {
            "tokenizer": {
                "type": "sudachi_tokenizer",
            },
            "filter": [
                "sudachi_normalizedform",
                {
                    "type": "synonym_graph",
                    "synonyms": ["すだち, みかん"]
                },
            ],
            "text": "すだちを食べた",
        }
        resp = es_instance.analyze(body)
        self.assertEqual(200, resp.status, f"data: {resp.data}")

        tokens = json.loads(resp.data)["tokens"]
        # "みかん" is normalized since synonym filter uses filters before it to parse synonym.
        self.assertListEqual(["蜜柑", "酢橘", "を", "食べる", "た"],
                             [t["token"] for t in tokens])
        return


if __name__ == "__main__":
    main()
