import argparse
from multiprocessing import Pool
import urllib3.request
import json
from pathlib import Path


def parse_args():
    p = argparse.ArgumentParser()
    p.add_argument("--port", type=int, default=9200)
    p.add_argument("--host", default="http://localhost")
    p.add_argument("--index", default="test_sudachi")
    return p.parse_args()


class Test(object):
    def __init__(self, args):
        self.args = args
        self.es = ElasticSearch(args)

    def assertEq(self, a, b):
        if a != b:
            raise Exception("a != b", a, b)

    def run(self):
        for name in dir(self):
            if name.startswith("test"):
                obj = getattr(self, name)
                if hasattr(obj, "__call__"):
                    obj()

    def test57Games(self):
        games = self.es.find("ゲーム")
        self.assertEq(57, games["hits"]["total"]["value"])

    def test107Daigaku(self):
        docs = self.es.find("大学")
        self.assertEq(107, docs["hits"]["total"]["value"])

    def test33Susumu(self):
        docs = self.es.find("進む")
        self.assertEq(33, docs["hits"]["total"]["value"])


class ElasticSearch(object):
    def __init__(self, args):
        self.url = "{0}:{1}/{2}".format(args.host, args.port, args.index)
        self.count = 0
        self.mgr = urllib3.PoolManager()

    def find(self, query):
        doc = {
            "query": {
                "multi_match": {
                    "query": query,
                    "fields": ["text*"],
                    "type": "most_fields",
                }
            }
        }
        url = f"{self.url}/_search"
        r = self.mgr.urlopen(
            "GET",
            url,
            headers={"Content-Type": "application/json"},
            body=json.dumps(doc),
        )
        return json.loads(r.data)


if __name__ == "__main__":
    Test(parse_args()).run()
