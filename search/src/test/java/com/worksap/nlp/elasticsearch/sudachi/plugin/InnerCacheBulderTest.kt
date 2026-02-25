/*
 * Copyright (c) 2026 Works Applications Co., Ltd.
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

package com.worksap.nlp.elasticsearch.sudachi.plugin

import com.worksap.nlp.sudachi.Config
import com.worksap.nlp.sudachi.DictionaryFactory
import com.worksap.nlp.test.TestDictionary
import kotlin.test.assertSame
import kotlin.test.assertTrue
import org.junit.Rule
import org.junit.Test

class InnerCacheImplTest {
  @JvmField @Rule var testDic = TestDictionary("system")

  @Test
  fun innerCacheComputesAndCachesValues() {
    val configDir = testDic.root.toPath().resolve("config/sudachi")
    val config = Config.fromFile(configDir.resolve("sudachi.json")).allowEmptyMorpheme(false)
    val dict = DictionaryFactory().create(config)
    val tokenizer = dict.create()

    val cache = InnerCacheBuilderImpl().build(2)
    val first = cache.computeIfAbsent("東京") { tokenizer.tokenize("東京") }
    val second = cache.computeIfAbsent("東京") { tokenizer.tokenize("東京") }
    assertSame(first, second)

    val stats = cache.stats()
    assertTrue(stats.hits + stats.misses >= 1)
  }
}
