/*
 * Copyright (c) 2025 Works Applications Co., Ltd.
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

package com.worksap.nlp.elasticsearch.sudachi

import kotlin.io.path.exists
import kotlin.test.Test
import org.junit.Assert

/**
 * Integration test to verify that the entitlement policy grants proper permissions for the plugin
 * to operate in Elasticsearch 9+.
 *
 * This test verifies:
 * 1. Plugin and dictionary files are accessible
 * 2. Default config can be read from plugin JAR (requires file read entitlement)
 * 3. Tokenizer works end-to-end without NotEntitledException
 * 4. Entitlement policy file is valid and parseable
 */
class EntitlementTest : SudachiEnvTest() {

  @Test
  fun pluginAndDictionaryFilesAccessible() {
    // Verify plugin directory structure and dictionary files are accessible
    // This requires entitlement policy to grant read access to both plugin and config directories
    val pluginDir = sudachiEnv.pluginsPath.resolve("analysis-sudachi")
    Assert.assertTrue("Plugin directory should exist: $pluginDir", pluginDir.exists())

    // Check SPI directory if it exists
    val spiDir = pluginDir.resolve("spi")
    if (spiDir.exists()) {
      Assert.assertTrue("SPI directory should be readable: $spiDir", spiDir.toFile().canRead())
    }

    // Verify dictionary files in config/sudachi directory
    val sudachiConfigDir = sudachiEnv.configPath.resolve("sudachi")
    Assert.assertTrue("Config directory should exist: $sudachiConfigDir", sudachiConfigDir.exists())

    val systemDict = sudachiConfigDir.resolve("system_core.dic")
    Assert.assertTrue("System dictionary should exist: $systemDict", systemDict.exists())
  }

  @Test
  fun tokenizerWorksWithDefaultConfig() {
    // End-to-end test: Create tokenizer without settings_path (forces default config from JAR)
    // This exercises the full entitlement chain:
    // 1. Plugin loads without NotEntitledException
    // 2. Read default config from plugin JAR
    // 3. Read dictionary from config directory
    // 4. Initialize and use tokenizer

    val req = AnalyzeActionRequestAlias("sudachi_test")
    req.tokenizer(
        mapOf(
            "type" to "sudachi_tokenizer",
            // No settings_path - forces Config.defaultConfig() call from JAR
            "split_mode" to "C"))
    req.text("東京都")

    val analyzers = analysisRegistry()

    // If entitlement is missing, this would throw NotEntitledException
    val response =
        TransportAnalyzeActionAlias.analyze(
            req,
            analyzers,
            null,
            1000,
        )

    // Verify tokenization works correctly
    Assert.assertTrue("Should tokenize successfully", response.tokens.isNotEmpty())
    Assert.assertEquals("東京都", response.tokens[0].term)
  }

  @Test
  fun entitlementPolicyIsValidAndParseable() {
    // This test validates that:
    // 1. The entitlement-policy.yaml file is packaged with the plugin
    // 2. The policy would be accepted during real plugin installation
    // It uses Elasticsearch's actual policy parser to ensure the syntax is correct
    val pluginDir = sudachiEnv.pluginsPath.resolve("analysis-sudachi")
    val entitlementPolicyFile = pluginDir.resolve("entitlement-policy.yaml")

    Assert.assertTrue(
        "entitlement-policy.yaml must exist in plugin package: $entitlementPolicyFile",
        entitlementPolicyFile.exists())

    try {
      // Use Elasticsearch's PolicyUtils to parse the policy file
      // This is the same parser used during plugin installation
      val policy =
          org.elasticsearch.entitlement.runtime.policy.PolicyUtils.parsePolicyIfExists(
              "analysis-sudachi", entitlementPolicyFile, true)

      // If we get here without an exception, the policy file is valid!
      Assert.assertNotNull(
          "Policy should be parsed successfully - this means the YAML syntax is correct and would be accepted during real plugin installation",
          policy)

      println("SUCCESS: entitlement-policy.yaml parsed successfully by Elasticsearch!")
      println("Policy name: ${policy.name}")
    } catch (e: org.elasticsearch.entitlement.runtime.policy.PolicyParserException) {
      Assert.fail(
          "entitlement-policy.yaml has invalid syntax and would fail during plugin installation: ${e.message}")
    } catch (e: Exception) {
      Assert.fail(
          "Unexpected error while parsing entitlement-policy.yaml: ${e.javaClass.simpleName}: ${e.message}")
    }
  }
}
