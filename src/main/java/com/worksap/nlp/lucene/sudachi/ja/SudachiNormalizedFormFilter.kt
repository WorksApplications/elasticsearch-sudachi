/*
 * Copyright (c) 2018-2023 Works Applications Co., Ltd.
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

import com.worksap.nlp.lucene.aliases.TokenFilterFactory
import com.worksap.nlp.sudachi.Morpheme
import org.apache.lucene.analysis.TokenStream

/**
 * Replaces term text with the value of normalized text field.
 *
 * This acts as a lemmatizer for verbs and adjectives.
 *
 * To prevent terms from being stemmed use an instance of
 * [org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter] or a custom
 * [org.apache.lucene.analysis.TokenFilter] that sets the
 * [org.apache.lucene.analysis.tokenattributes.KeywordAttribute] before this [TokenStream].
 */
class SudachiNormalizedFormFilter(input: TokenStream) :
    MorphemeFieldFilter(
        input,
    ) {
  override fun value(m: Morpheme): CharSequence? {
    return m.normalizedForm()
  }
}

class SudachiNormalizedFormFilterFactory(args: MutableMap<String, String>) :
    TokenFilterFactory(args) {
  init {
    require(args.isEmpty()) { "Unknown parameters: $args" }
  }

  override fun create(input: TokenStream): TokenStream {
    return SudachiNormalizedFormFilter(input)
  }
}
