/*
 * Copyright (c) 2026 Works Applications Co., Ltd.
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

package com.worksap.nlp.test

import com.worksap.nlp.lucene.sudachi.ja.plugin.AnalysisCache
import com.worksap.nlp.lucene.sudachi.ja.plugin.AnalysisCacheStats
import com.worksap.nlp.lucene.sudachi.ja.plugin.InnerCache
import com.worksap.nlp.lucene.sudachi.ja.plugin.InnerCacheBuilder
import com.worksap.nlp.sudachi.MorphemeList
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


/*
 * LRU cache with minimum functionality for tests.
 */
class LruCache<K,V>(val maxWeight: Long, val weigher: (K,V) -> Long){
    // container
	private val queue =  ConcurrentLinkedQueue<K>();
	private val map  =  ConcurrentHashMap<K, V>();
    private var currentWeight = 0L

    // lock
	private val lock = ReentrantReadWriteLock();
	private val writeLock = lock.writeLock();
	private val readLock = lock.readLock();

    // stats
    var hits = 0L
    var misses = 0L
    var evictions = 0L

    fun get(key: K): V? {
        readLock.lock()
        try {
            val v = map.get(key)
            if (v == null) {
                misses += 1
            } else {
                queue.remove(key)
                queue.add(key)
                hits += 1
            }
            return v
        } finally {
            readLock.unlock()
        }
    }

    fun put(key: K, value: V) {
        writeLock.lock()
        try {
            var weightDiff = 0L
            val oldV = map.get(key)
            if (oldV != null) {
                queue.remove(key)
                weightDiff -= weigher(key, oldV)
            }

            queue.add(key)
            map.put(key, value)
            weightDiff += weigher(key, value)
            currentWeight += weightDiff

            prune()
        } finally {
            writeLock.unlock()
        }
    }

    fun remove(key: K) {
        writeLock.lock()
        try {
            val v = map.remove(key)
            if (v != null) {
                queue.remove(key)
                currentWeight -= weigher(key, v)
                evictions += 1
            }
        } finally {
            writeLock.unlock()
        }
    }

    fun computeIfAbsent(input: K,  valueFunc: (K) -> V): V {
        val v = get(input) ?: {
            // here we ignore computation cost
            val newV = valueFunc(input)
            put(input, newV)
            newV
        }()
        return v
    }

    private fun prune() {
        while (exceedsWeight()) {
            val key = queue.poll()
            if (key == null) {
                break
            } else {
                val value = map.remove(key)
                if (value != null) {
                    currentWeight -= weigher(key, value)
                    evictions += 1
                }
            }
        }
    }

    private fun exceedsWeight(): Boolean{
        return maxWeight > 0 && currentWeight > maxWeight
    }
}

class InnerCacheBuilderImpl : InnerCacheBuilder {
  override fun build(capacity: Int): InnerCacheImpl {
    val cache = LruCache(
        AnalysisCache.calculateMaxWeight(capacity),
        AnalysisCache.weigher,
    )
    return InnerCacheImpl(cache, capacity)
  }
}

class InnerCacheImpl(
    private val cache: LruCache<String, MorphemeList>,
    override public val capacity: Int,
) : InnerCache {
  override fun computeIfAbsent(
      input: String,
      lazyTokenize: (String) -> MorphemeList
  ): MorphemeList {
    return cache.computeIfAbsent(input, lazyTokenize)
  }

  override fun stats(): AnalysisCacheStats {
    return AnalysisCacheStats(
        hits = cache.hits,
        misses = cache.misses,
        evictions = cache.evictions,
    )
  }
}
