/*
 * Copyright (c) 2022 Works Applications Co., Ltd.
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

package com.worksap.nlp.lucene.sudachi.ja.input

import com.worksap.nlp.lucene.sudachi.aliases.DirectoryForTests
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.Tokenizer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.TextField
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.junit.Assert.*
import org.junit.Test

class InputExtractorTest {
  private val dir = DirectoryForTests()

  private class ExtractorTextStream(
      val extractor: InputExtractor,
      val storage: ExtractingAnalyzer
  ) : Tokenizer() {
    override fun incrementToken(): Boolean {
      storage.data = extractor.extract(this.input)
      return false
    }
  }

  private class ExtractingAnalyzer(private val extractor: InputExtractor) : Analyzer() {
    var data: ExtractionResult = ExtractionResult.EMPTY

    override fun createComponents(fieldName: String?): TokenStreamComponents {
      val tokenizer = ExtractorTextStream(extractor, this)
      return TokenStreamComponents(tokenizer, tokenizer)
    }
  }

  private fun extract(extractor: InputExtractor, text: String): ExtractionResult {
    val doc = Document()
    doc.add(TextField("t", text, Field.Store.YES))
    val analyzer = ExtractingAnalyzer(extractor)
    val iwc = IndexWriterConfig(analyzer)
    val iw = IndexWriter(dir, iwc)
    iw.addDocument(doc)
    return analyzer.data
  }

  @Test
  fun varHandleImpl() {
    val text = "asdf"
    val extracted = extract(ReusableReaderVarHandleExtractor.INSTANCE, text)
    assertFalse(extracted.remaining)
    assertSame(text, extracted.data) // should be the same instance
  }

  @Test
  fun copyImplShort() {
    val extracted = extract(CopyingInputExtractor(512), "fake")
    assertFalse(extracted.remaining)
    assertEquals("fake", extracted.data)
  }

  @Test
  fun copyImplLong() {
    val text = (0..127).fold(StringBuilder()) { sb, c -> sb.append(c.toChar()) }.toString()
    val extracted = extract(CopyingInputExtractor(100), text)
    assertTrue(extracted.remaining)
    assertEquals(text.substring(0 until 100), extracted.data)
  }
}
