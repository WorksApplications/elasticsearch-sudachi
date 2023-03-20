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

@file:Suppress("PackageDirectoryMismatch")

package com.worksap.nlp.search.aliases

/**
 * This file must not contain any imports to ElasticSearch/OpenSearch classes. For clarity, all
 * ElasticSearch/OpenSearch files must be fully-qualified on every use.
 *
 * Lucene classes can be imported.
 */
import org.apache.lucene.analysis.CharArraySet

typealias Settings = org.elasticsearch.common.settings.Settings

typealias Environment = org.elasticsearch.env.Environment

typealias IndexSettings = org.elasticsearch.index.IndexSettings

typealias AnalyzerProvider<T> = org.elasticsearch.index.analysis.AnalyzerProvider<T>

typealias TokenFilterFactory = org.elasticsearch.index.analysis.TokenFilterFactory

typealias TokenizerFactory = org.elasticsearch.index.analysis.TokenizerFactory

typealias AnalysisProvider<T> =
    org.elasticsearch.indices.analysis.AnalysisModule.AnalysisProvider<T>

typealias AnalysisPlugin = org.elasticsearch.plugins.AnalysisPlugin

typealias Plugin = org.elasticsearch.plugins.Plugin

typealias CacheBuilder<K, V> = org.elasticsearch.common.cache.CacheBuilder<K, V>

fun parseStopWords(
    environment: Environment?,
    settings: Settings?,
    defaultStopWords: CharArraySet,
    ignoreCase: Boolean
): CharArraySet {
  return org.elasticsearch.index.analysis.Analysis.parseStopWords(
      environment,
      settings,
      defaultStopWords,
      ignoreCase,
  )
}

fun getWordList(environment: Environment?, settings: Settings?, prefix: String): List<String>? {
  return org.elasticsearch.index.analysis.Analysis.getWordList(environment, settings, prefix)
}

fun parseWords(
    environment: Environment?,
    settings: Settings?,
    name: String,
    defaultWords: CharArraySet,
    namedStopWords: Map<String, Set<*>>,
    ignoreCase: Boolean
): CharArraySet {
  return org.elasticsearch.index.analysis.Analysis.parseWords(
      environment, settings, name, defaultWords, namedStopWords, ignoreCase)
}
