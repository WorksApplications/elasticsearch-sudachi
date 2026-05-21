/*
 * Copyright (c) 2022-2026 Works Applications Co., Ltd.
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

package com.worksap.nlp.lucene.sudachi.ja.input

import java.io.Reader
import java.lang.ref.SoftReference

data class ExtractionResult(
    val data: String,
    val remaining: Boolean,
) {
  companion object {
    @JvmField val EMPTY_HAS_REMAINING = ExtractionResult("", remaining = true)
    @JvmField val EMPTY_NO_REMAINING = ExtractionResult("", remaining = false)
  }
}

interface InputExtractor {
  fun extract(input: Reader): ExtractionResult

  fun canExtract(input: Reader): Boolean

  companion object {
    const val MAX_INPUT_SETTING_KEY = "cache-max-input"
    const val DEFAUlT_MAX_INPUT = Short.MAX_VALUE.toInt()

    @JvmStatic
    fun make(maxSize: Int?): InputExtractor = CopyingInputExtractor(maxSize ?: DEFAUlT_MAX_INPUT)
  }
}

class NoopInputExtractor : InputExtractor {
  override fun extract(input: Reader) = ExtractionResult.EMPTY_HAS_REMAINING

  override fun canExtract(input: Reader) = false

  companion object {
    @JvmField val INSTANCE = NoopInputExtractor()
  }
}

class CopyingInputExtractor(private val maxSize: Int) : InputExtractor {
  private val bufferLocal = ThreadLocal<SoftReference<CharArray>>()

  override fun extract(input: Reader): ExtractionResult {
    val buf = getBuffer()
    var offset = input.read(buf)
    if (offset == -1) {
      return ExtractionResult.EMPTY_NO_REMAINING
    }
    var toRead = maxSize - offset
    while (toRead > 0) {
      val nread = input.read(buf, offset, toRead)
      if (nread < 0) { // end of stream mark
        val data = String(buf, 0, offset)
        return ExtractionResult(data, remaining = false)
      }
      offset += nread
      toRead -= nread
    }
    val data = String(buf, 0, offset)
    return ExtractionResult(data, remaining = true)
  }

  private fun getBuffer(): CharArray {
    val ref = bufferLocal.get()
    if (ref != null) {
      val buf = ref.get()
      if (buf != null) {
        return buf
      }
    }
    val buf = CharArray(maxSize)
    bufferLocal.set(SoftReference(buf))
    return buf
  }

  override fun canExtract(input: Reader): Boolean = true

  override fun toString(): String {
    return super.toString() + "(maxSize=$maxSize)"
  }
}
