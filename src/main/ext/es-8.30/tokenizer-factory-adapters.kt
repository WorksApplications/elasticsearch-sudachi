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

@file:Suppress("UNUSED_PARAMETER")

package com.worksap.nlp.elasticsearch.sudachi.aliases

import org.apache.lucene.analysis.Analyzer
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.index.IndexSettings

abstract class AbstractTokenizerFactory(
    indexSettings: IndexSettings?,
    settings: Settings?,
    name: String?
) : org.elasticsearch.index.analysis.AbstractTokenizerFactory(indexSettings, settings, name)

abstract class AbstractTokenFilterFactory(
    indexSettings: IndexSettings?,
    name: String?,
    settings: Settings?,
) : org.elasticsearch.index.analysis.AbstractTokenFilterFactory(indexSettings, name, settings)

abstract class AbstractIndexAnalyzerProvider<T : Analyzer>(
    indexSettings: IndexSettings?,
    name: String?,
    settings: Settings?,
) : org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider<T>(name, settings)
