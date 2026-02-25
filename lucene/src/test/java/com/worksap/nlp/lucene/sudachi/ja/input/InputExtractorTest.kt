/*
 * Copyright (c) 2022-2026 Works Applications Co., Ltd.
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

import java.io.Reader
import java.io.StringReader
import kotlin.test.*
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.Tokenizer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.TextField
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.ByteBuffersDirectory
import org.junit.Test

class InputExtractorTest {
  @Test
  fun inputExtractorMakeUsesExpectedImplementation() {
    val extractor = InputExtractor.make(16)
    if (InputExtractorBootstrap.ZERO_COPY === NoopInputExtractor.INSTANCE) {
      assertTrue(extractor is CopyingInputExtractor)
    } else {
      assertTrue(extractor is ChainedExtractor)
    }
  }

  @Test
  fun useCopyingInputExtractor() {
    val extractor = CopyingInputExtractor(2);

    val extracted = extractor.extract(StringReader("hello"))
    val expected = ExtractionResult("he", true)
    assertEquals(expected, extracted)
    assertTrue(extractor.canExtract(StringReader("x")))
  }

  @Test
  fun useReusableReaderVarHandleExtractor() {
    val extracted = ReusableReaderVarHandleExtractor.INSTANCE.extract(StringReader("hello"))
    assertSame(ExtractionResult.EMPTY_HAS_REMAINING, extracted)
    assertFalse(ReusableReaderVarHandleExtractor.INSTANCE.canExtract(StringReader("x")))
  }

  private val dir = ByteBuffersDirectory()

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
    var data: ExtractionResult = ExtractionResult.EMPTY_HAS_REMAINING

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

  @Test
  fun copyImplZeroLength() {
    val extracted = extract(CopyingInputExtractor(512), "")
    assertEquals("", extracted.data)
    assertFalse(extracted.remaining)
  }

  private class StubExtractor(
      private val name: String,
      private val canExtractValue: Boolean,
      private val result: ExtractionResult,
  ) : InputExtractor {
    override fun extract(input: Reader): ExtractionResult = result

    override fun canExtract(input: Reader): Boolean = canExtractValue

    override fun toString(): String = name
  }

  @Test
  fun chainedExtractorUsesFirstAndDescribesChain() {
    val first = StubExtractor("first", true, ExtractionResult("first", remaining = false))
    val fallback = StubExtractor("fallback", true, ExtractionResult("fallback", remaining = true))
    val chained = ChainedExtractor(first, fallback)
    val extracted = chained.extract(StringReader("ignored"))
    assertEquals("first", extracted.data)
    assertFalse(extracted.remaining)
    assertTrue(chained.canExtract(StringReader("ignored")))

    val description = chained.toString()
    assertTrue(description.startsWith("ChainedExtractor["))
    assertTrue(description.contains("first"))
    assertTrue(description.contains("fallback"))
  }

  @Test
  fun chainedExtractorUsesFallback() {
    val first = StubExtractor("first", false, ExtractionResult("first", remaining = false))
    val fallback = StubExtractor("fallback", true, ExtractionResult("fallback", remaining = true))
    val chained = ChainedExtractor(first, fallback)
    val extracted = chained.extract(StringReader("ignored"))
    assertEquals("fallback", extracted.data)
    assertTrue(extracted.remaining)
    assertTrue(chained.canExtract(StringReader("ignored")))
  }
}
