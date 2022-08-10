import argparse
import urllib3.request
import json


def parse_args():
    p = argparse.ArgumentParser()
    p.add_argument('--port', type=int, default=9200)
    p.add_argument('--host', default='localhost')
    p.add_argument('--docs', default="1000")
    p.add_argument('--index', default="test_sudachi")
    return p.parse_args()


def main(args):
    pass


class ElasticSearch(object):
    def __init__(self, args):
        self.url = "{0}:{1}/{2}".format(args.host, args.port, args.index)
        self.count = 0
        self.mgr = urllib3.PoolManager()

    def put(self, data, doc_id=None):
        if doc_id is None:
            doc_id = self.count
            self.count += 1
        doc = {
            "text": data
        }
        url = "{}/_doc/{}".format(self.url, doc_id)
        self.mgr.request_encode_body("PUT", url, headers={
            "Content-Type": "application/json"
        }, encode_multipart=False, fields=[json.dumps(doc)])


if __name__ == '__main__':
    main(parse_args())
