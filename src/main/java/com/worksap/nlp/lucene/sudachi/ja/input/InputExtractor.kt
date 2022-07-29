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

package com.worksap.nlp.lucene.sudachi.ja.input

import java.io.Reader
import org.elasticsearch.common.settings.Settings

data class ExtractionResult(
    val data: String,
    val remaining: Boolean,
) {
  companion object {
    @JvmField val EMPTY = ExtractionResult("", remaining = true)
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
  override fun extract(input: Reader) = ExtractionResult.EMPTY
  override fun canExtract(input: Reader) = false

  companion object {
    @JvmField val INSTANCE = NoopInputExtractor()
  }
}

class CopyingInputExtractor(private val maxSize: Int) : InputExtractor {
  private val buffer = CharArray(maxSize)
  override fun extract(input: Reader): ExtractionResult {
    val sz1 = input.read(buffer)
    if (sz1 == maxSize) {
      val data = String(buffer, 0, sz1)
      return ExtractionResult(data, remaining = true)
    }
    val sz2 = input.read(buffer, sz1, maxSize - sz1)
    if (sz2 == -1) {
      val data = String(buffer, 0, sz1)
      return ExtractionResult(data, remaining = false)
    }
    val data = String(buffer, 0, sz1 + sz2)
    return ExtractionResult(data, true)
  }

  override fun canExtract(input: Reader): Boolean = true
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
}
