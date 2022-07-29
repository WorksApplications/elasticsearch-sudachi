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

package com.worksap.nlp.tools

import java.lang.IllegalArgumentException
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith

enum class Foo {
  FOO,
  BAR,
  BAZ
}

class EnumFlagTest {
  private object FooFlag1 : EnumFlag<Foo>("foo")
  private object FooFlag2 : EnumFlag<Foo>("foo", Foo.BAR)

  @Test
  fun worksWithMap() {
    val map = mutableMapOf("foo" to "baz")
    assertEquals(Foo.BAZ, FooFlag1.extract(map))
  }

  @Test
  fun defaultValue() {
    val map = mutableMapOf<String, String>()
    assertEquals(Foo.BAR, FooFlag2.extract(map))
  }

  @Test
  fun invalidValue() {
    val map = mutableMapOf("foo" to "xxx")
    val ex = assertFails { FooFlag1.extract(map) }
    assertContains(ex.message!!, "[FOO, BAR, BAZ]")
  }

  @Test
  fun noDefaultValue() {
    val map = mutableMapOf<String, String>()
    assertFailsWith<IllegalArgumentException> { FooFlag1.extract(map) }
  }
}
