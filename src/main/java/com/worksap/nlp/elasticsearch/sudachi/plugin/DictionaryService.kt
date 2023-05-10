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

import com.worksap.nlp.lucene.sudachi.ja.CurrentDictionary
import com.worksap.nlp.lucene.sudachi.ja.CurrentTokenizer
import com.worksap.nlp.lucene.sudachi.ja.ReloadAware
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
class ReloadableDictionary(private val config: Config) : CurrentDictionary {
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

  override fun newTokenizer(): CurrentTokenizer = ReloadableTokenizer(this)

  override fun <T> reloadable(fn: Function<Dictionary, T>): ReloadAware<T> {
    return ReloadAwareImpl(this, fn)
  }

  override fun maybeReload(newDictionary: CurrentDictionary?): Dictionary {
    if (newDictionary is ReloadableDictionary) {
      val holder = newDictionary.current
      current = Holder(holder.version + 1, holder.dictionary)
    }
    return get()
  }

  override fun get(): Dictionary {
    return current.dictionary
  }

  override fun dictionary(): ReloadableDictionary {
    return this
  }
}

/** Smart reference for objects which need to be aware of reloadable dictionary */
class ReloadAwareImpl<T>(private val factory: Function<Dictionary, T>) : ReloadAware<T> {
  private var version: Long = Long.MIN_VALUE
  private var instance: T? = null
  private var lastDic: ReloadableDictionary? = null

  internal constructor(
      dic: ReloadableDictionary,
      factory: Function<Dictionary, T>
  ) : this(factory) {
    maybeReload(dic)
  }

  override fun maybeReload(dic: CurrentDictionary?): T {
    val dicInstance = ((dic as? ReloadableDictionary) ?: lastDic)!!
    val cur = dicInstance.holder()
    if (cur.version != version) {
      instance = factory.apply(cur.dictionary)!!
      version = cur.version
      lastDic = dicInstance
    }
    return get()
  }

  override fun get(): T {
    return instance ?: throw NullPointerException("maybeReload function was not called")
  }

  override fun dictionary(): ReloadableDictionary {
    return lastDic!!
  }
}

/**
 */
class ReloadableTokenizer(private val dictionary: ReloadableDictionary) : CurrentTokenizer {
  private var version = 0L
  private var tokenizer = run {
    val pair = dictionary.newTokenizerInternal()
    version = pair.second
    SoftReference(pair.first)
  }

  /** Instances returned from this function should not be cached */
  override fun get(): Tokenizer {
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

  override fun dictionary(): ReloadableDictionary = dictionary

  override fun maybeReload(newDictionary: CurrentDictionary?): Tokenizer {
    dictionary.maybeReload(newDictionary)
    return get()
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
