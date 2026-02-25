/*
 * Copyright (c) 2023-2024 Works Applications Co., Ltd.
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

package com.worksap.nlp.lucene.sudachi.ja.attributes

import org.apache.lucene.util.Attribute
import org.apache.lucene.util.AttributeFactory
import org.apache.lucene.util.AttributeImpl

// simply hardcore all our attributes without doing smart things
class SudachiAttributeFactory(private val parent: AttributeFactory) : AttributeFactory() {
  override fun createAttributeInstance(attClass: Class<out Attribute>?): AttributeImpl {
    return when (attClass) {
      MorphemeAttribute::class.java -> MorphemeAttributeImpl()
      SudachiAttribute::class.java -> SudachiAttributeImpl()
      else -> parent.createAttributeInstance(attClass)
    }
  }
}
