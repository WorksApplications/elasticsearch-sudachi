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

package com.worksap.nlp.lucene.sudachi.ja.attributes

import com.worksap.nlp.lucene.sudachi.ja.reflect
import org.apache.lucene.util.Attribute
import org.apache.lucene.util.AttributeImpl
import org.apache.lucene.util.AttributeReflector

/**
 * Sudachi-based TokenStream chain uses to communicate which component produces
 * [org.apache.lucene.analysis.tokenattributes.CharTermAttribute]
 *
 * This is not a token-based attribute, so impl's clear/copyTo do nothing
 */
interface MorphemeConsumerAttribute : Attribute {
  fun shouldConsume(check: Any): Boolean = check === instance
  var instance: Any
}

class MorphemeConsumerAttributeImpl : AttributeImpl(), MorphemeConsumerAttribute {
  override var instance: Any = Companion
  // does nothing
  override fun clear() {}

  override fun reflectWith(reflector: AttributeReflector) {
    reflector.reflect<MorphemeConsumerAttribute>("instance", instance.javaClass.name)
  }

  override fun copyTo(target: AttributeImpl?) {}

  // need something to use as initial value of [instance] variable
  private companion object
}
