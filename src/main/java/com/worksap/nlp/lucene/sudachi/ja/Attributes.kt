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

package com.worksap.nlp.lucene.sudachi.ja

import org.apache.lucene.util.Attribute
import org.apache.lucene.util.AttributeReflector
import org.apache.lucene.util.AttributeSource

inline fun <reified T : Attribute> AttributeSource.addAttribute(fn: (T) -> Unit = {}): T {
  val attr = addAttribute(T::class.java)
  fn(attr)
  return attr
}

inline fun <reified T : Attribute> AttributeSource.getAttribute(fn: (T) -> Unit = {}): T? {
  val attr = getAttribute(T::class.java)
  if (attr != null) {
    fn(attr)
  }
  return attr
}

inline fun <reified T : Attribute> AttributeSource.existingAttribute(fn: (T) -> Unit = {}): T {
  val attr = getAttribute(fn)
  return checkNotNull(attr) { "Attribute ${T::class.java.simpleName} was not present" }
}

inline fun <reified T : Attribute> AttributeReflector.reflect(key: String, value: Any?) {
  reflect(T::class.java, key, value)
}
