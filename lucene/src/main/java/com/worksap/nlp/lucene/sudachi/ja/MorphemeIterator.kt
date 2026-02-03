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

package com.worksap.nlp.lucene.sudachi.ja

import com.worksap.nlp.lucene.sudachi.ja.util.Strings
import com.worksap.nlp.sudachi.Morpheme
import com.worksap.nlp.sudachi.Tokenizer
import com.worksap.nlp.sudachi.Tokenizer.SplitMode
import java.io.Reader

interface MorphemeIterator {
  companion object {
    @JvmField
    val EMPTY: MorphemeIterator =
        object : MorphemeIterator {
          override fun next(): Morpheme? = null
        }
  }

  /**
   * Non-Java interface for iterator to be more in line with Lucene TokenStream API
   *
   * @return `null` if iteration was ended, [Morpheme] if not
   */
  fun next(): Morpheme?

  val baseOffset: Int
    get() = 0
}

class CachedAnalysis(source: Iterable<Morpheme>) : MorphemeIterator {
  private val iter = source.iterator()

  private var sequenceEnd = 0

  override fun next(): Morpheme? {
    if (iter.hasNext()) {
      val m = iter.next()
      sequenceEnd = m.end()
      return m
    }
    baseOffset = sequenceEnd
    return null
  }

  override var baseOffset: Int = 0
}

class NonCachedAnalysis(tokenizer: Tokenizer, input: Reader, splitMode: SplitMode) :
    MorphemeIterator {

  private object EmptyIterator : Iterator<Morpheme> {
    override fun hasNext() = false
    override fun next() = throw IllegalStateException()
  }

  private val sentenceIterator = tokenizer.lazyTokenizeSentences(splitMode, input)
  private var morphemeIterator: Iterator<Morpheme> = EmptyIterator
  private var currentLength = 0

  override fun next(): Morpheme? {
    val mi = morphemeIterator
    if (mi.hasNext()) {
      return mi.next()
    } else {
      baseOffset += currentLength
      if (sentenceIterator.hasNext()) {
        val morphs = sentenceIterator.next()
        currentLength = morphs.lastOrNull()?.end() ?: 0
        morphemeIterator = morphs.iterator()
        // try once more with a recursive call
        return this.next()
      } else {
        currentLength = 0
      }
    }
    return null
  }

  override var baseOffset = 0
}

class NonPunctuationMorphemes(private val inner: MorphemeIterator) : MorphemeIterator {
  override fun next(): Morpheme? {
    while (true) {
      val next = inner.next() ?: return null
      if (!Strings.isPunctuation(next.normalizedForm())) {
        return next
      }
    }
  }

  override val baseOffset: Int
    get() = inner.baseOffset
}
