/*
 * Copyright (c) 2017-2022 Works Applications Co., Ltd.
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

import com.worksap.nlp.elasticsearch.sudachi.plugin.ReloadAware
import com.worksap.nlp.lucene.sudachi.ja.attributes.MorphemeAttribute
import com.worksap.nlp.sudachi.PosMatcher
import org.apache.lucene.analysis.FilteringTokenFilter
import org.apache.lucene.analysis.TokenStream

/** Removes tokens that match a set of part-of-speech tags. */
class SudachiPartOfSpeechStopFilter(
    input: TokenStream?,
    private val matcher: ReloadAware<PosMatcher>
) : FilteringTokenFilter(input) {
  private val morpheme = addAttribute<MorphemeAttribute>()

  override fun reset() {
    super.reset()
    matcher.maybeReload()
  }

  override fun accept(): Boolean {
    return !matcher.get().test(morpheme.morpheme)
  }
}
