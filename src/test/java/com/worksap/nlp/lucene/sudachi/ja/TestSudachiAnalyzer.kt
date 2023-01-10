/*
 * Copyright (c) 2017-2023 Works Applications Co., Ltd.
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

package com.worksap.nlp.lucene.sudachi.ja

import com.worksap.nlp.lucene.sudachi.aliases.DirectoryForTests
import com.worksap.nlp.lucene.sudachi.ja.input.NoopInputExtractor
import com.worksap.nlp.lucene.sudachi.ja.util.AnalysisCache
import com.worksap.nlp.lucene.test.hits
import com.worksap.nlp.sudachi.Tokenizer
import com.worksap.nlp.test.InMemoryDictionary
import java.io.IOException
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.TextField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.Directory
import org.apache.lucene.util.BytesRef
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Test

// Test of character segmentation using analyzer
class TestSudachiAnalyzer {
  private var analyzer: SudachiAnalyzer? = null
  private val dir: Directory = DirectoryForTests()
  private val dic = InMemoryDictionary()

  @Before
  fun setUp() {
    analyzer =
        SudachiAnalyzer(
            dic.dic,
            AnalysisCache(0, NoopInputExtractor.INSTANCE),
            true,
            Tokenizer.SplitMode.C,
            SudachiAnalyzer.getDefaultStopSet(),
            SudachiAnalyzer.getDefaultStopTags())
    val config = IndexWriterConfig(analyzer)
    IndexWriter(dir, config).use { writer -> createIndex(writer) }
  }

  @After
  @Throws(IOException::class)
  fun tearDown() {
    dir.close()
    if (analyzer != null) {
      analyzer!!.close()
    }
  }

  @Test
  @Throws(IOException::class)
  fun testTerms() {
    DirectoryReader.open(dir).use { reader ->
      val atomicReaderContextList = reader.leaves()
      MatcherAssert.assertThat(atomicReaderContextList.size, CoreMatchers.`is`(1))
      val leafReaderContext = atomicReaderContextList[0]
      val leafReader = leafReaderContext.reader()
      val fields = leafReader.fieldInfos
      MatcherAssert.assertThat(fields.size(), CoreMatchers.`is`(1))
      val fieldName = fields.iterator().next().name
      MatcherAssert.assertThat(fieldName, CoreMatchers.`is`(FIELD_NAME))
      val terms = leafReader.terms(fieldName)
      MatcherAssert.assertThat(terms.size(), CoreMatchers.`is`(2L))
      val termList: MutableList<String> = ArrayList()
      val termsEnum = terms.iterator()
      var bytesRef: BytesRef?
      while (termsEnum.next().also { bytesRef = it } != null) {
        val fieldText = bytesRef!!.utf8ToString()
        termList.add(fieldText)
      }
      MatcherAssert.assertThat<List<String>>(termList, CoreMatchers.hasItems("東京都", "行く"))
    }
  }

  @Test
  fun testQuery() {
    DirectoryReader.open(dir).use { reader ->
      val searcher = IndexSearcher(reader)
      val queryParser = QueryParser(FIELD_NAME, analyzer)
      var query = queryParser.parse("東京都")
      val topDocs = searcher.search(query, 5)
      val scoreDocs = topDocs.scoreDocs
      MatcherAssert.assertThat(scoreDocs.size, CoreMatchers.`is`(1))
      val scoreDoc = scoreDocs[0]
      val docId = scoreDoc.doc
      MatcherAssert.assertThat(docId, CoreMatchers.`is`(0))
      val doc = searcher.doc(docId)
      val values = doc.getValues(FIELD_NAME)
      MatcherAssert.assertThat(values.size, CoreMatchers.`is`(1))
      MatcherAssert.assertThat(values[0], CoreMatchers.`is`(INPUT_TEXT))
      query = queryParser.parse("京都")
      MatcherAssert.assertThat(searcher.search(query, 5).hits(), CoreMatchers.`is`(0L))
      query = queryParser.parse("岩波")
      MatcherAssert.assertThat(searcher.search(query, 5).hits(), CoreMatchers.`is`(0L))
    }
  }

  companion object {
    private const val INPUT_TEXT = "東京都へ行った。"
    private const val FIELD_NAME = "txt"
    private fun createIndex(writer: IndexWriter) {
      val doc = Document()
      doc.add(TextField(FIELD_NAME, INPUT_TEXT, Field.Store.YES))
      writer.addDocument(doc)
      writer.commit()
      writer.isOpen
    }
  }
}
