/*
 * Copyright (c) 2022-2023 Works Applications Co., Ltd.
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
import java.lang.reflect.ParameterizedType
import org.elasticsearch.common.settings.Settings

/**
 * Helper for extracting converting enums from property values
 *
 * Usage is `object: EnumFlag<FooEnum>("foo", FooEnum.DEFAULT)`
 */
abstract class EnumFlag<T : Enum<T>>(private val name: String, private val default: T? = null) {
  @Suppress("UNCHECKED_CAST")
  private val enumClazz = run {
    val superclazz = javaClass.annotatedSuperclass.type as ParameterizedType
    val typeArgs = superclazz.actualTypeArguments
    typeArgs[0] as Class<T>
  }
  private val values: Array<T> = enumClazz.enumConstants

  /** Extract value from Lucene settings */
  fun extract(args: MutableMap<String, String>): T {
    val raw = args.remove(name)
    return convert(raw)
  }

  /** Extract value from ElasticSearch settings */
  fun get(s: Settings): T {
    val raw = s.get(name, null)
    return convert(raw)
  }

  private fun convert(raw: String?): T {
    if (raw == null) {
      if (default != null) {
        return default
      } else {
        throw IllegalArgumentException("required property $name was not present")
      }
    }

    for (v in values) {
      if (v.name.equals(raw, true)) {
        return v
      }
    }
    val acceptedValues = values.joinToString(", ", "[", "]") { it.name }
    throw IllegalArgumentException(
        "property $name had unknown value $raw, accepted values: $acceptedValues")
  }
}
