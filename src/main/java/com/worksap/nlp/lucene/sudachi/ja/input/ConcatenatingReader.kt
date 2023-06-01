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

import java.io.Reader

class ConcatenatingReader(private val data: String, private val remaining: Reader) : Reader() {
  private var offset = 0

  override fun read(cbuf: CharArray, off0: Int, len0: Int): Int {
    var off = off0
    var len = len0
    if (offset < data.length) {
      val toRead = len.coerceAtMost(data.length - offset)
      data.toCharArray(cbuf, off, offset, offset + toRead)
      offset += toRead
      if (len == toRead) {
        return toRead
      }
      len -= toRead
      off += toRead
      val nread = remaining.read(cbuf, off, len)
      return if (nread < 0) { // if the second stream is finished
        toRead
      } else {
        toRead + nread
      }
    }
    return remaining.read(cbuf, off, len)
  }

  override fun close() {
    remaining.close()
  }
}
