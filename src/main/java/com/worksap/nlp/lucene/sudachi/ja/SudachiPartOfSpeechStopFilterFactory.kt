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

import com.worksap.nlp.elasticsearch.sudachi.plugin.ReloadAware
import com.worksap.nlp.lucene.aliases.ResourceLoaderArgument
import com.worksap.nlp.lucene.aliases.ResourceLoaderAware
import com.worksap.nlp.lucene.aliases.TokenFilterFactory
import com.worksap.nlp.lucene.sudachi.ja.attributes.SudachiAttribute
import com.worksap.nlp.lucene.sudachi.ja.util.Stoptags
import com.worksap.nlp.sudachi.PartialPOS
import org.apache.lucene.analysis.CharArraySet
import org.apache.lucene.analysis.TokenStream

class SudachiPartOfSpeechStopFilterFactory(args: MutableMap<String, String>) :
    TokenFilterFactory(args), ResourceLoaderAware {
  private val stopTagFiles: String
  private lateinit var stopTags: List<PartialPOS>

  init {
    val stopTagFiles = get(args, "tags")
    require(args.isEmpty()) { "Unknown parameters: $args" }
    this.stopTagFiles = stopTagFiles
  }

  override fun inform(loader: ResourceLoaderArgument?) {
    val cas: CharArraySet? = getWordSet(loader, stopTagFiles, false)
    if (cas != null) {
      val stopTags = ArrayList<PartialPOS>()
      for (element in cas) {
        val chars = element as CharArray
        stopTags.add(Stoptags.parse(String(chars)))
      }
      this.stopTags = stopTags
    } else {
      stopTags = emptyList()
    }
  }

  override fun create(stream: TokenStream): TokenStream {
    return if (stopTags.isNotEmpty()) {
      val sudachi =
          stream.getAttribute<SudachiAttribute>()
              ?: throw IllegalArgumentException(
                  "Sudachi Tokenizer does not present in the filter chain")
      val matcher = ReloadAware { it.posMatcher(stopTags) }
      matcher.maybeReload(sudachi.dictionary)
      SudachiPartOfSpeechStopFilter(stream, matcher)
    } else {
      stream
    }
  }
}
