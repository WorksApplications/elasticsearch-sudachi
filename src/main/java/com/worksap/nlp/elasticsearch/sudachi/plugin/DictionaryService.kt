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

package com.worksap.nlp.elasticsearch.sudachi.plugin

import com.worksap.nlp.sudachi.Config
import com.worksap.nlp.sudachi.Dictionary
import com.worksap.nlp.sudachi.DictionaryFactory
import com.worksap.nlp.sudachi.Tokenizer
import java.lang.ref.SoftReference
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function
import org.apache.logging.log4j.LogManager

/** Lightweight cache with strong-referenced entries and unbounded size */
class UnboundedCache<K : Any, V>(private val factory: Function<K, V>) {
  private val cache = ConcurrentHashMap<K, V>()

  fun get(key: K): V {
    return cache.computeIfAbsent(key, factory)
  }
}

// Actual reloading logic will be implemented later
class ReloadableDictionary(private val config: Config) {
  class Holder(val version: Long, val dictionary: Dictionary) {
    internal fun create() = Pair(dictionary.create(), version)
  }

  @Volatile private var current = create(0L)

  private fun create(version: Long): Holder {
    val dic = DictionaryFactory().create(config)
    return Holder(version, dic)
  }

  val version: Long
    get() = current.version

  internal fun newTokenizerInternal(): Pair<Tokenizer, Long> {
    return current.create()
  }

  internal fun holder(): Holder = current

  fun newTokenizer(): ReloadableTokenizer = ReloadableTokenizer(this)
}

/** Smart reference for objects which need to be aware of reloadable dictionary */
class ReloadAware<T>(private val factory: Function<Dictionary, T>) {
  private var version: Long = Long.MIN_VALUE
  private var instance: T? = null
  private var lastDic: ReloadableDictionary? = null

  constructor(dic: ReloadableDictionary, factory: Function<Dictionary, T>) : this(factory) {
    maybeReload(dic)
  }

  fun maybeReload(dic: ReloadableDictionary? = null): T {
    val dicInstance = (dic ?: lastDic)!!
    val cur = dicInstance.holder()
    if (cur.version != version) {
      instance = factory.apply(cur.dictionary)!!
      version = cur.version
      lastDic = dicInstance
    }
    return get()
  }

  fun get(): T {
    return instance ?: throw NullPointerException("maybeReload function was not called")
  }
}

/**
 */
class ReloadableTokenizer(val dictionary: ReloadableDictionary) {
  private var version = 0L
  private var tokenizer = run {
    val pair = dictionary.newTokenizerInternal()
    version = pair.second
    SoftReference(pair.first)
  }

  /** Instances returned from this function should not be cached */
  fun get(): Tokenizer {
    val instance = tokenizer.get()
    val dicVersion = dictionary.version
    if (version != dicVersion || instance == null) {
      val pair = dictionary.newTokenizerInternal()
      version = pair.second
      tokenizer = SoftReference(pair.first)
      return pair.first
    }
    return instance
  }
}

class DictionaryService {
  companion object {
    private val logger = LogManager.getLogger(DictionaryService::class.java)
  }

  private val dictionaryCache = UnboundedCache(this::makeDictionary)

  fun forConfig(config: Config): ReloadableDictionary {
    return dictionaryCache.get(config)
  }

  private fun makeDictionary(config: Config): ReloadableDictionary {
    logger.debug("loading dictionary with config={}", config)
    return ReloadableDictionary(config)
  }
}
