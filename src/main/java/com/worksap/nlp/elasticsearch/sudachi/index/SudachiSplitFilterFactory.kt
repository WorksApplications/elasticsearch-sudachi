/*
 * Copyright (c) 2020-2023 Works Applications Co., Ltd.
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

package com.worksap.nlp.elasticsearch.sudachi.index

import com.worksap.nlp.lucene.sudachi.ja.SudachiSplitFilter
import com.worksap.nlp.search.aliases.AbstractTokenFilterFactory
import com.worksap.nlp.search.aliases.Environment
import com.worksap.nlp.search.aliases.IndexSettings
import com.worksap.nlp.search.aliases.Settings
import com.worksap.nlp.sudachi.Tokenizer
import com.worksap.nlp.tools.EnumFlag
import org.apache.lucene.analysis.TokenStream

class SudachiSplitFilterFactory(
    indexSettings: IndexSettings?,
    env: Environment?,
    name: String?,
    settings: Settings
) : AbstractTokenFilterFactory(indexSettings, env, name, settings) {

  private val mode = Mode.get(settings)
  private val splitMode = SplitMode.get(settings)

  override fun create(tokenStream: TokenStream): TokenStream {
    return SudachiSplitFilter(tokenStream, mode, splitMode)
  }

  companion object {
    private object Mode :
        EnumFlag<SudachiSplitFilter.Mode>("mode", SudachiSplitFilter.DEFAULT_MODE)
    private object SplitMode : EnumFlag<Tokenizer.SplitMode>("split_mode", Tokenizer.SplitMode.A)
  }
}
