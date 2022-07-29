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

package com.worksap.nlp.lucene.sudachi.ja

import com.worksap.nlp.lucene.sudachi.ja.attributes.MorphemeAttribute
import com.worksap.nlp.lucene.sudachi.ja.attributes.MorphemeConsumerAttribute
import com.worksap.nlp.sudachi.Morpheme
import org.apache.logging.log4j.LogManager
import org.apache.lucene.analysis.TokenFilter
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute

/**
 * Sets the content of [CharTermAttribute] to the returned value of [MorphemeFieldFilter.value]
 * method.
 *
 * To prevent terms from being rewritten use an instance of
 * [org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter] or a custom [TokenFilter] that
 * sets the [KeywordAttribute] before this [TokenStream].
 *
 * Values of [MorphemeAttribute] are used to produce the
 */
abstract class MorphemeFieldFilter(input: TokenStream) : TokenFilter(input) {
  @JvmField protected val morpheme = existingAttribute<MorphemeAttribute>()
  @JvmField protected val keywordAtt = addAttribute<KeywordAttribute>()
  @JvmField protected val termAtt = addAttribute<CharTermAttribute>()
  @JvmField protected val consumer = addAttribute<MorphemeConsumerAttribute> { it.instance = this }

  /**
   * Override this method to customize returned value. This method will not be called if
   * [MorphemeAttribute] contained `null`.
   */
  protected open fun value(m: Morpheme): CharSequence? = m.surface()

  override fun incrementToken(): Boolean {
    if (!input.incrementToken()) {
      return false
    }
    val m = morpheme.morpheme ?: return true
    var needToSet = consumer.shouldConsume(this)
    if (!keywordAtt.isKeyword) {
      val term = value(m)
      if (term != null) {
        termAtt.setEmpty().append(term)
        needToSet = false
      }
    }
    if (needToSet) {
      termAtt.setEmpty().append(m.surface())
    }
    return true
  }

  override fun reset() {
    super.reset()
    if (!consumer.shouldConsume(this)) {
      logger.warn("consumer")
    }
  }

  companion object {
    private val logger = LogManager.getLogger(MorphemeFieldFilter::class.java)
  }
}
