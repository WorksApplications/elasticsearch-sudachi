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

import com.worksap.nlp.lucene.sudachi.aliases.ResourceLoaderArgument
import com.worksap.nlp.lucene.sudachi.aliases.ResourceLoaderAware
import com.worksap.nlp.lucene.sudachi.aliases.TokenFilterFactory
import java.io.IOException
import org.apache.lucene.analysis.CharArraySet
import org.apache.lucene.analysis.TokenStream

class SudachiPartOfSpeechStopFilterFactory(args: Map<String?, String?>) :
    TokenFilterFactory(args), ResourceLoaderAware {
  private val stopTagFiles: String
  private var stopTags: PartOfSpeechTrie? = null

  init {
    val stopTagFiles = get(args, "tags")
    require(args.isEmpty()) { "Unknown parameters: $args" }
    this.stopTagFiles = stopTagFiles
  }

  @Throws(IOException::class)
  override fun inform(loader: ResourceLoaderArgument?) {
    stopTags = null
    val cas: CharArraySet? = getWordSet(loader, stopTagFiles, false)
    if (cas != null) {
      val stopTags = PartOfSpeechTrie()
      for (element in cas) {
        val chars = element as CharArray
        val elements = java.lang.String(chars).split(",")
        stopTags.add(*elements)
      }
      this.stopTags = stopTags
    }
  }

  override fun create(stream: TokenStream): TokenStream {
    // if stoptags is null, it means the file is empty
    return if (stopTags != null) {
      SudachiPartOfSpeechStopFilter(stream, stopTags)
    } else {
      stream
    }
  }
}
