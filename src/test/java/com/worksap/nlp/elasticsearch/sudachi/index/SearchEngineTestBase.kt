/*
 * Copyright (c) 2023 Works Applications Co., Ltd.
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

package com.worksap.nlp.elasticsearch.sudachi.index

import com.worksap.nlp.search.aliases.LogConfigurator
import com.worksap.nlp.search.aliases.NamedAnalyzer
import com.worksap.nlp.search.aliases.Settings
import com.worksap.nlp.search.aliases.TokenizerFactory
import com.worksap.nlp.search.aliases.XContentType
import java.io.StringReader
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.intellij.lang.annotations.Language

/** Interface for search engine-based tests. It performs logging initialization. */
interface SearchEngineTestBase {}

fun @receiver:Language("json") String.jsonSettings(): Settings {
  val set = Settings.builder()
  set.loadFromSource(this, XContentType.JSON)
  return set.build()
}

fun NamedAnalyzer?.assertTerms(text: String, vararg expected: String) {
  val named = assertNotNull(this)
  val analyzer = assertNotNull(named.analyzer())
  analyzer.tokenStream("text", text).use { tokenStream -> assertTerms(tokenStream, *expected) }
}

fun TokenizerFactory?.assertTerms(text: String, vararg expected: String) {
  val factory = assertNotNull(this)
  assertNotNull(factory.create()).use { tokenizer ->
    tokenizer.setReader(StringReader(text))
    assertTerms(tokenizer, *expected)
  }
}

fun assertTerms(stream: TokenStream, vararg expected: String) {
  stream.reset()
  val termAttr = stream.getAttribute(CharTermAttribute::class.java)
  val actual: MutableList<String> = ArrayList()
  while (stream.incrementToken()) {
    actual.add(termAttr.toString())
  }
  assertEquals(expected.asList(), actual)
}

object SearchEngineLogging {
  fun touch() {
    // do nothing, constructor does everything
  }

  init {
    initLogging()
  }

  private fun initLogging() {
    val clz = LogConfigurator::class.java
    try {
      // since ES 8.5
      val m = clz.getMethod("configureESLogging")
      m.invoke(null)
    } catch (_: NoSuchMethodException) {
      // do nothing
    }

    LogConfigurator.configureWithoutConfig(Settings.EMPTY)
  }
}
