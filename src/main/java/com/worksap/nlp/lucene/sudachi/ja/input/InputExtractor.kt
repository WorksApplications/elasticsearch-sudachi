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

package com.worksap.nlp.lucene.sudachi.ja.input

import com.worksap.nlp.search.aliases.Settings
import java.io.Reader
import java.lang.ref.SoftReference
import kotlin.text.StringBuilder

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
    @JvmStatic
    fun make(settings: Settings): InputExtractor {
      val maxSize = settings.getAsInt("cache-max-input", Short.MAX_VALUE.toInt())
      return if (InputExtractorBootstrap.ZERO_COPY === NoopInputExtractor.INSTANCE) {
        CopyingInputExtractor(maxSize)
      } else {
        ChainedExtractor(InputExtractorBootstrap.ZERO_COPY, CopyingInputExtractor(maxSize))
      }
    }
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

class ChainedExtractor(private val first: InputExtractor, private val fallback: InputExtractor) :
    InputExtractor {
  override fun extract(input: Reader): ExtractionResult {
    if (first.canExtract(input)) {
      return first.extract(input)
    }
    return fallback.extract(input)
  }

  override fun canExtract(input: Reader): Boolean {
    return first.canExtract(input) || fallback.canExtract(input)
  }

  private fun describe(builder: StringBuilder) {
    if (first is ChainedExtractor) {
      first.describe(builder)
    } else {
      builder.append(first.toString())
    }
    builder.append(", ")
    if (fallback is ChainedExtractor) {
      fallback.describe(builder)
    } else {
      builder.append(fallback.toString())
    }
  }

  override fun toString(): String {
    val bldr = StringBuilder("ChainedExtractor[")
    describe(bldr)
    bldr.append("]")
    return bldr.toString()
  }
}
