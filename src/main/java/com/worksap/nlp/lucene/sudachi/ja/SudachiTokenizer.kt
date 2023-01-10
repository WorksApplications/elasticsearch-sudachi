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

import com.worksap.nlp.lucene.sudachi.ja.attributes.MorphemeAttribute
import com.worksap.nlp.lucene.sudachi.ja.attributes.MorphemeConsumerAttribute
import com.worksap.nlp.lucene.sudachi.ja.attributes.SudachiAttribute
import org.apache.lucene.analysis.Tokenizer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute
import org.apache.lucene.util.AttributeFactory

@Suppress("jol")
class SudachiTokenizer(
    private val tokenizer: CachingTokenizer,
    private val discardPunctuation: Boolean,
    factory: AttributeFactory = AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY
) : Tokenizer(factory) {

  private val termAtt = addAttribute<CharTermAttribute>()
  private val morphemeAtt = addAttribute<MorphemeAttribute>()
  private val offsetAtt = addAttribute<OffsetAttribute>()
  private val posIncAtt = addAttribute<PositionIncrementAttribute>()
  private val posLenAtt = addAttribute<PositionLengthAttribute>()
  private val consumer = addAttribute<MorphemeConsumerAttribute> { it.instance = this }

  init {
    addAttribute<SudachiAttribute> { it.dictionary = tokenizer.dictionary }
  }

  private var iterator: MorphemeIterator = MorphemeIterator.EMPTY

  override fun reset() {
    super.reset()
    var iter = tokenizer.tokenize(input)
    if (discardPunctuation) {
      iter = NonPunctuationMorphemes(iter)
    }
    iterator = iter
  }

  override fun incrementToken(): Boolean {
    clearAttributes()
    val m = iterator.next() ?: return false
    morphemeAtt.morpheme = m
    posLenAtt.positionLength = 1
    posIncAtt.positionIncrement = 1
    val baseOffset = iterator.baseOffset
    offsetAtt.setOffset(correctOffset(baseOffset + m.begin()), correctOffset(baseOffset + m.end()))
    if (consumer.shouldConsume(this)) {
      termAtt.append(m.surface())
    }
    return true
  }

  override fun end() {
    super.end()
    val lastOffset = correctOffset(iterator.baseOffset)
    offsetAtt.setOffset(lastOffset, lastOffset)
    iterator = MorphemeIterator.EMPTY
  }
}
