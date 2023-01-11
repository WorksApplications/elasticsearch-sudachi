import argparse
from multiprocessing import Pool
import urllib3.request
import json
from pathlib import Path


def parse_args():
    p = argparse.ArgumentParser()
    p.add_argument("--port", type=int, default=9200)
    p.add_argument("--host", default="http://localhost")
    p.add_argument("--docs", default=20000, type=int)
    p.add_argument("--index", default="test_sudachi")
    p.add_argument("--no-data", action="store_true")
    return p.parse_args()


def main(args):
    if args.no_data:
        es = ElasticSearch(args)
        for i in range(args.docs):
            es.put(f"これはドキュメント＃{i}です")
    else:
        put_actual(args)


es_instance = None


def setup_es(args):
    global es_instance
    es_instance = ElasticSearch(args)


def worker(line, id):
    global es_instance
    es_instance.put(line, id)


def put_actual(args):
    cur_dir = Path(__file__).parent

    with Pool(None, setup_es, [args]) as p:
        futures = []
        with (cur_dir / "test-sentences.txt").open(encoding="utf-8") as inf:
            for i, line in enumerate(inf):
                if i >= args.docs:
                    return
                futures.append(p.apply_async(worker, [line.rstrip(), i]))
        for f in futures:
            f.wait()
        print(f"inserted {len(futures)} documents")

    ElasticSearch(args).refresh()


class ElasticSearch(object):
    def __init__(self, args):
        self.url = "{0}:{1}/{2}".format(args.host, args.port, args.index)
        self.count = 0
        self.mgr = urllib3.PoolManager()

    def put(self, data, doc_id=None):
        if doc_id is None:
            doc_id = self.count
            self.count += 1
        doc = {"text": data}
        url = f"{self.url}/_create/{doc_id}"
        r = self.mgr.urlopen(
            "PUT",
            url,
            headers={"Content-Type": "application/json"},
            body=json.dumps(doc),
        )
        return r.data

    def refresh(self):
        url = f"{self.url}/_refresh"
        return self.mgr.urlopen("POST", url).data


if __name__ == "__main__":
    main(parse_args())
