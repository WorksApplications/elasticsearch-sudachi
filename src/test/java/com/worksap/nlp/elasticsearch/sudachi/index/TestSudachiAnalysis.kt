/*
 * Copyright (c) 2020-2022 Works Applications Co., Ltd.
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

import com.worksap.nlp.elasticsearch.sudachi.aliases.MetadataConstants
import com.worksap.nlp.elasticsearch.sudachi.plugin.AnalysisCacheService
import com.worksap.nlp.elasticsearch.sudachi.plugin.AnalysisSudachiPlugin
import com.worksap.nlp.elasticsearch.sudachi.plugin.DictionaryService
import com.worksap.nlp.elasticsearch.test.TestEnvironment.newEnvironment
import com.worksap.nlp.lucene.sudachi.aliases.BaseTokenStreamTestCase
import com.worksap.nlp.lucene.sudachi.ja.ResourceUtil
import com.worksap.nlp.test.TestDictionary
import java.io.StringReader
import java.util.*
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.elasticsearch.Version
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.env.Environment
import org.elasticsearch.index.Index
import org.elasticsearch.index.analysis.TokenizerFactory
import org.elasticsearch.indices.analysis.AnalysisModule
import org.elasticsearch.plugins.AnalysisPlugin
import org.elasticsearch.test.IndexSettingsModule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

open class TestSudachiAnalysis : BaseTokenStreamTestCase() {
  @JvmField @Rule var testDic = TestDictionary("system")

  @Test
  fun tokenizer() {
    val settings =
        mapOf(
            "index.analysis.tokenizer.sudachi_tokenizer.type" to "sudachi_tokenizer",
            "index.analysis.tokenizer.sudachi_tokenizer.settings_path" to "sudachi.json")

    val tokenizer = createTestTokenizers(settings)["sudachi_tokenizer"]!!.create()
    tokenizer.setReader(StringReader("東京へ行く。"))
    assertTerms(tokenizer, "東京", "へ", "行く")
  }

  @Test
  fun tokenizerWithAdditionalSettings() {
    val additional: String = ResourceUtil.resource("additional.json").readText()
    val settings =
        mapOf(
            "index.analysis.tokenizer.sudachi_tokenizer.type" to "sudachi_tokenizer",
            "index.analysis.tokenizer.sudachi_tokenizer.additional_settings" to additional)

    val tokenizer = createTestTokenizers(settings)["sudachi_tokenizer"]!!.create()
    tokenizer.setReader(StringReader("自然言語"))
    assertTerms(tokenizer, "自然", "言語")
  }

  @Test
  fun analyzerProvider() {
    val indexSettings =
        Settings.builder().put(MetadataConstants.SETTING_VERSION_CREATED, Version.CURRENT).build()
    val nodeSettings =
        Settings.builder().put(Environment.PATH_HOME_SETTING.key, testDic.root.path).build()
    val env = newEnvironment(nodeSettings)
    val settings = Settings.builder().put("settings_path", "sudachi.json").build()
    val provider =
        SudachiAnalyzerProvider(
            AnalysisCacheService(),
            DictionaryService(),
            IndexSettingsModule.newIndexSettings(Index("test", "_na_"), indexSettings),
            env,
            "sudachi",
            settings)
    provider.get().tokenStream("_na_", "東京へ行く。").use { stream ->
      assertTokenStreamContents(stream, arrayOf("東京", "行く"))
    }
  }

  private fun createTestTokenizers(settings: Map<String, String>): Map<String, TokenizerFactory> {
    val builder = Settings.builder()
    settings.forEach { (key: String?, value: String?) -> builder.put(key, value) }
    builder.put(MetadataConstants.SETTING_VERSION_CREATED, Version.CURRENT)
    val indexSettings = builder.build()
    val nodeSettings =
        Settings.builder().put(Environment.PATH_HOME_SETTING.key, testDic.root.path).build()
    val env = newEnvironment(nodeSettings)
    val analysisModule =
        AnalysisModule(env, listOf<AnalysisPlugin>(AnalysisSudachiPlugin(nodeSettings)))
    val analysisRegistry = analysisModule.analysisRegistry
    return analysisRegistry.buildTokenizerFactories(
        IndexSettingsModule.newIndexSettings(Index("test", "_na_"), indexSettings))
  }

  companion object {
    fun assertTerms(stream: TokenStream, vararg expected: String?) {
      stream.reset()
      val termAttr = stream.getAttribute(CharTermAttribute::class.java)
      val actual: MutableList<String> = ArrayList()
      while (stream.incrementToken()) {
        actual.add(termAttr.toString())
      }
      Assert.assertEquals(expected.asList(), actual)
    }
  }
}
