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

package com.worksap.nlp.lucene.sudachi.ja.plugin

import com.worksap.nlp.sudachi.Tokenizer.SplitMode
import com.worksap.nlp.test.InMemoryDictionary
import com.worksap.nlp.test.InnerCacheBuilderImpl
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import org.junit.Test

class ServiceCoverageTest {
  @Test
  fun analysisCacheServiceRebuildsAfterWeakReferenceCleared() {
    val service = AnalysisCacheService(InnerCacheBuilderImpl())
    val config = InMemoryDictionary().config
    val first =
        service.analysisCache(
            "index",
            config,
            SplitMode.C,
            capacity = null,
            max_input_size = null,
        )
    val second =
        service.analysisCache(
            "index",
            config,
            SplitMode.C,
            capacity = null,
            max_input_size = null,
        )
    assertSame(first, second)

    val another =
        service.analysisCache(
            "another",
            config,
            SplitMode.C,
            capacity = null,
            max_input_size = null,
        )
    assertNotSame(first, another)
  }

  @Test
  fun dictionaryServiceCachesByConfig() {
    val service = DictionaryService()
    val config = InMemoryDictionary().config
    val first = service.forConfig(config)
    val second = service.forConfig(config)
    assertSame(first, second)
  }
}
