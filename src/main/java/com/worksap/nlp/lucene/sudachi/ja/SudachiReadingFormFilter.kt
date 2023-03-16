/*
 * Copyright (c) 2017-2023 Works Applications Co., Ltd.
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
import com.worksap.nlp.lucene.sudachi.ja.util.Romanizer
import com.worksap.nlp.sudachi.Morpheme
import org.apache.lucene.analysis.TokenStream

class SudachiReadingFormFilter
@JvmOverloads
constructor(input: TokenStream, private val useRomaji: Boolean = false) :
    MorphemeFieldFilter(
        input,
    ) {
  private val buffer = StringBuilder()
  override fun value(m: Morpheme): CharSequence? {
    val reading = m.readingForm() ?: return null
    return if (useRomaji) {
      buffer.setLength(0)
      Romanizer.romanize(reading, buffer)
      buffer
    } else {
      reading
    }
  }
}

class SudachiReadingFormFilterFactory(args: MutableMap<String, String>) : TokenFilterFactory(args) {
  private val useRomaji = getBoolean(args, ROMAJI_PARAM, false)
  init {
    require(args.isEmpty()) { "Unknown parameters: $args" }
  }

  override fun create(input: TokenStream): TokenStream {
    return SudachiReadingFormFilter(input, useRomaji)
  }

  companion object {
    private const val ROMAJI_PARAM = "useRomaji"
  }
}
