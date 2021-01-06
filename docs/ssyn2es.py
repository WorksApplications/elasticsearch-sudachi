#!/usr/bin/env python

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