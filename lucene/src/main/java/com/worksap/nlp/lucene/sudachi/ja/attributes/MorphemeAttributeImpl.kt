/*
 * Copyright (c) 2022-2024 Works Applications Co., Ltd.
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

import com.worksap.nlp.lucene.aliases.ToXContent
import com.worksap.nlp.lucene.aliases.ToXContentParams
import com.worksap.nlp.lucene.aliases.XContentBuilder
import com.worksap.nlp.lucene.sudachi.ja.reflect
import com.worksap.nlp.sudachi.Morpheme
import org.apache.lucene.util.AttributeImpl
import org.apache.lucene.util.AttributeReflector

class MorphemeAttributeImpl : AttributeImpl(), MorphemeAttribute {
  private var morpheme: Morpheme? = null
  // mapping from the character offset to the original reader offset
  private var offsetMap: List<Int> = listOf()

  // wrapper class to convert data ToXContent-able
  private class ToXContentWrapper(morpheme: Morpheme, offsetMap: List<Int>) : ToXContent {
    private val morpheme = morpheme
    private val offsetMap = offsetMap

    override fun toXContent(builder: XContentBuilder, params: ToXContentParams): XContentBuilder {
      builder.value(
          mapOf(
              "surface" to morpheme.surface(),
              "dictionaryForm" to morpheme.dictionaryForm(),
              "normalizedForm" to morpheme.normalizedForm(),
              "readingForm" to morpheme.readingForm(),
              "partOfSpeech" to morpheme.partOfSpeech(),
              "offsetMap" to offsetMap,
          ))
      return builder
    }
  }

  override fun clear() {
    morpheme = null
    offsetMap = listOf()
  }

  override fun reflectWith(reflector: AttributeReflector) {
    // show only when a morpheme is set
    reflector.reflect<MorphemeAttribute>(
        "morpheme", morpheme?.let { m -> ToXContentWrapper(m, offsetMap) })
  }

  override fun copyTo(target: AttributeImpl?) {
    (target as? MorphemeAttributeImpl)?.let {
      it.setMorpheme(getMorpheme())
      it.setOffsets(getOffsets())
    }
  }

  override fun getMorpheme(): Morpheme? {
    return morpheme
  }

  override fun setMorpheme(morpheme: Morpheme?) {
    this.morpheme = morpheme
  }

  override fun getOffsets(): List<Int> {
    return offsetMap
  }

  override fun setOffsets(offsets: List<Int>) {
    this.offsetMap = offsets
  }
}
