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

package com.worksap.nlp.elasticsearch.sudachi.index

import com.worksap.nlp.lucene.sudachi.ja.SudachiBaseFormFilter
import com.worksap.nlp.search.aliases.*
import com.worksap.nlp.search.aliases.AbstractTokenFilterFactory
import org.apache.lucene.analysis.TokenStream

class SudachiBaseFormFilterFactory(
    indexSettings: IndexSettings?,
    environment: Environment?,
    name: String?,
    settings: Settings?
) : AbstractTokenFilterFactory(indexSettings, environment, name, settings) {
  override fun create(tokenStream: TokenStream): TokenStream {
    return SudachiBaseFormFilter(tokenStream)
  }
}
