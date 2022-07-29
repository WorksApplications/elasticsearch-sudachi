/*
 * Copyright (c) 2017-2022 Works Applications Co., Ltd.
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

package com.worksap.nlp.lucene.sudachi.ja;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.worksap.nlp.lucene.sudachi.ja.input.NoopInputExtractor;
import com.worksap.nlp.lucene.sudachi.ja.util.AnalysisCache;
import com.worksap.nlp.lucene.test.Top_docsKt;
import com.worksap.nlp.sudachi.Tokenizer;

import com.worksap.nlp.test.InMemoryDictionary;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

// Test of character segmentation using analyzer
public class TestSudachiAnalyzer {
    private static final String INPUT_TEXT = "東京都へ行った。";
    private static final String FIELD_NAME = "txt";

    private SudachiAnalyzer analyzer;
    private Directory dir;
    private final InMemoryDictionary dic = new InMemoryDictionary();

    @Before
    public void setUp() throws IOException {
        analyzer = new SudachiAnalyzer(dic.getDic(), new AnalysisCache(0, NoopInputExtractor.INSTANCE), true,
                Tokenizer.SplitMode.C, SudachiAnalyzer.getDefaultStopSet(), SudachiAnalyzer.getDefaultStopTags());
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        dir = new RAMDirectory();

        try (IndexWriter writer = new IndexWriter(dir, config)) {
            createIndex(writer);
        }
    }

    @After
    public void tearDown() throws IOException {
        if (dir != null) {
            dir.close();
        }
        if (analyzer != null) {
            analyzer.close();
        }
    }

    private static void createIndex(IndexWriter writer) throws IOException {
        Document doc = new Document();
        doc.add(new TextField(FIELD_NAME, INPUT_TEXT, Field.Store.YES));
        writer.addDocument(doc);
        writer.commit();
        writer.isOpen();
    }

    @Test
    public void testTerms() throws IOException {
        try (IndexReader reader = DirectoryReader.open(dir)) {
            List<LeafReaderContext> atomicReaderContextList = reader.leaves();
            assertThat(atomicReaderContextList.size(), is(1));
            LeafReaderContext leafReaderContext = atomicReaderContextList.get(0);

            LeafReader leafReader = leafReaderContext.reader();
            FieldInfos fields = leafReader.getFieldInfos();
            assertThat(fields.size(), is(1));

            String fieldName = fields.iterator().next().name;
            assertThat(fieldName, is(FIELD_NAME));

            Terms terms = leafReader.terms(fieldName);
            assertThat(terms.size(), is(2L));

            List<String> termList = new ArrayList<>();
            TermsEnum termsEnum = terms.iterator();
            BytesRef bytesRef = null;
            while ((bytesRef = termsEnum.next()) != null) {
                String fieldText = bytesRef.utf8ToString();
                termList.add(fieldText);
            }
            assertThat(termList, hasItems("東京都", "行く"));
        }
    }

    @Test
    public void testQuery() throws Exception {
        try (IndexReader reader = DirectoryReader.open(dir)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            QueryParser queryParser = new QueryParser(FIELD_NAME, analyzer);

            Query query = queryParser.parse("東京都");
            TopDocs topDocs = searcher.search(query, 5);
            ScoreDoc[] scoreDocs = topDocs.scoreDocs;
            assertThat(scoreDocs.length, is(1));
            ScoreDoc scoreDoc = scoreDocs[0];
            int docId = scoreDoc.doc;
            assertThat(docId, is(0));
            Document doc = searcher.doc(docId);
            String[] values = doc.getValues(FIELD_NAME);
            assertThat(values.length, is(1));
            assertThat(values[0], is(INPUT_TEXT));

            query = queryParser.parse("京都");
            assertThat(Top_docsKt.hits(searcher.search(query, 5)), is(0L));

            query = queryParser.parse("岩波");
            assertThat(Top_docsKt.hits(searcher.search(query, 5)), is(0L));
        }
    }
}
