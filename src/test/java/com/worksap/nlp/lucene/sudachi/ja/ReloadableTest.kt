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

package com.worksap.nlp.lucene.sudachi.ja

import com.worksap.nlp.test.InMemoryDictionary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame

class ReloadableTest {
  @Test
  fun reloadedTokenizer() {
    val imd = InMemoryDictionary()
    val d1 = imd.newDictionary()
    val d2 = imd.newDictionary()

    val t1 = assertNotNull(d1.newTokenizer())

    d1.maybeReload(d2)
    assertSame(d1.get(), d2.get())

    val t2 = assertNotNull(d2.newTokenizer())
    t1.maybeReload(d2) // call different overloads for coverage
    t2.maybeReload()
    assertEquals(t1.dictionary().version, t2.dictionary().version)
  }
}
