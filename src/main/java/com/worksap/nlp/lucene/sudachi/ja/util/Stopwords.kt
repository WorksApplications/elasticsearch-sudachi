/*
 * Copyright (c) 2023 Works Applications Co., Ltd.
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

package com.worksap.nlp.lucene.sudachi.ja.util

import com.worksap.nlp.lucene.sudachi.ja.SudachiAnalyzer
import java.io.IOException
import org.apache.lucene.analysis.CharArraySet
import org.apache.lucene.analysis.WordlistLoader

object Stopwords {
  @JvmStatic
  @Throws(IOException::class)
  fun load(ignoreCase: Boolean, filename: String, comment: String = "#"): CharArraySet {
    SudachiAnalyzer::class.java.getResourceAsStream(filename).use {
      if (it == null) {
        throw IllegalArgumentException("resource $filename was not found")
      }
      val result = CharArraySet(16, ignoreCase)
      return WordlistLoader.getWordSet(it.bufferedReader(), comment, result)
    }
  }
}
