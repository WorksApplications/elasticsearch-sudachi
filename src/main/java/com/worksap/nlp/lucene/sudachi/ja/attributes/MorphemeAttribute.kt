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

package com.worksap.nlp.lucene.sudachi.ja.attributes

import com.worksap.nlp.lucene.sudachi.ja.reflect
import com.worksap.nlp.sudachi.Morpheme
import org.apache.lucene.util.Attribute
import org.apache.lucene.util.AttributeImpl
import org.apache.lucene.util.AttributeReflector

/** Communicate the current Sudachi morpheme to the token stream */
interface MorphemeAttribute : Attribute {
  var morpheme: Morpheme?
}

class MorphemeAttributeImpl : AttributeImpl(), MorphemeAttribute {
  override var morpheme: Morpheme? = null

  override fun clear() {
    morpheme = null
  }

  override fun reflectWith(reflector: AttributeReflector) {
    reflector.reflect<MorphemeAttribute>("morpheme", morpheme)
  }

  override fun copyTo(target: AttributeImpl?) {
    (target as? MorphemeAttributeImpl)?.let { it.morpheme = morpheme }
  }
}
