/*
 * Copyright (c) 2022-2026 Works Applications Co., Ltd.
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

import com.worksap.nlp.lucene.sudachi.ja.CurrentDictionary
import com.worksap.nlp.lucene.sudachi.ja.plugin.ReloadableDictionary
import org.apache.lucene.util.AttributeImpl
import org.apache.lucene.util.AttributeReflector

class SudachiAttributeImpl : AttributeImpl(), SudachiAttribute {
  /** No-op because all tokens from a Sudachi tokenizer shares same Sudahi instance. */
  override fun clear() {}

  /** Skip explain api. */
  override fun reflectWith(reflector: AttributeReflector?) {}

  override fun copyTo(target: AttributeImpl?) {
    (target as? SudachiAttributeImpl)?.let {
      it.dictionary = this.dictionary
    }
  }

  private var dictionary: ReloadableDictionary? = null

  override fun getDictionary(): ReloadableDictionary {
    return dictionary ?: throw NullPointerException("Dictionary was not initialized")
  }

  override fun setDictionary(dictionary: CurrentDictionary?) {
    if (dictionary is ReloadableDictionary) {
      this.dictionary = dictionary
    } else {
      throw IllegalArgumentException("dictionary was of unsupported type: ${dictionary?.javaClass}")
    }
  }
}
