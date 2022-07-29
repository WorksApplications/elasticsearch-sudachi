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

package com.worksap.nlp.test

import org.apache.lucene.analysis.TokenStream

/**
 * Builder interface for `assertTokenStreamContents` method to make autoformatting better
 *
 * Method names have the same length so indices should (mostly) align
 */
class TokenStreamContents(private val finalOffset: Int, private vararg val tokens: String) {

  private var _endOffsets: IntArray? = null

  fun offsetEnd(vararg data: Int): TokenStreamContents {
    _endOffsets = data
    return this
  }

  private var _startOffsets: IntArray? = null

  fun offsetBeg(vararg data: Int): TokenStreamContents {
    _startOffsets = data
    return this
  }

  private var _posIncrements: IntArray? = null

  fun posIncrem(vararg data: Int): TokenStreamContents {
    _posIncrements = data
    return this
  }

  private var _posLengths: IntArray? = null

  fun posLength(vararg data: Int): TokenStreamContents {
    _posLengths = data
    return this
  }

  fun check(stream: TokenStream) {
    // static methods seems to be non-callable from Kotlin classes
    // so going via Java class indirection
    AssertRunnerTest.assertTokenStreamContents(
        stream, tokens, _startOffsets, _endOffsets, _posIncrements, _posLengths, finalOffset)
  }

  companion object {
    @JvmStatic
    fun tokens(finalOffset: Int, vararg tokens: String): TokenStreamContents {
      return TokenStreamContents(finalOffset, *tokens)
    }
  }
}
