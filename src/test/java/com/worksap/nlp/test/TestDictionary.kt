/*
 * Copyright (c) 2022-2024 Works Applications Co., Ltd.
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

import com.worksap.nlp.elasticsearch.sudachi.plugin.AnalysisCache
import com.worksap.nlp.elasticsearch.sudachi.plugin.ReloadableDictionary
import com.worksap.nlp.lucene.sudachi.ja.CachingTokenizer
import com.worksap.nlp.lucene.sudachi.ja.ResourceUtil
import com.worksap.nlp.lucene.sudachi.ja.SudachiTokenizer
import com.worksap.nlp.lucene.sudachi.ja.input.NoopInputExtractor
import com.worksap.nlp.sudachi.Config
import com.worksap.nlp.sudachi.PathAnchor
import com.worksap.nlp.sudachi.Tokenizer.SplitMode
import com.worksap.nlp.sudachi.dictionary.BinaryDictionary
import com.worksap.nlp.sudachi.dictionary.build.DicBuilder
import java.io.File
import java.io.StringReader
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.SeekableByteChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption as OO
import org.junit.rules.TemporaryFolder

fun resource(name: String): URL? {
  return TestDictionary::class.java.classLoader.getResource(name)
}

class InMemoryByteChannel(size: Int = 4096) : SeekableByteChannel {
  private var data = ByteArray(size)
  private var pointer: Int = 0
  private var end: Int = 0
  override fun close() {}

  override fun isOpen(): Boolean = true

  override fun read(dst: ByteBuffer?): Int {
    if (dst == null) {
      throw NullPointerException("dst")
    }
    val rem = dst.remaining()
    dst.put(data, pointer, end - pointer)
    return rem - dst.remaining()
  }

  override fun write(src: ByteBuffer?): Int {
    if (src == null) {
      throw NullPointerException("src")
    }
    val rem = src.remaining()
    maybeResize(rem)
    src.get(data, pointer, rem)
    pointer += rem
    end = end.coerceAtLeast(pointer)
    return rem
  }

  private fun maybeResize(additional: Int) {
    val rem = data.size - pointer
    if (rem < additional) {
      val newSize = additional.coerceAtLeast(data.size) + data.size
      data = data.copyOf(newSize)
    }
  }

  override fun position(): Long = pointer.toLong()

  override fun position(newPosition: Long): SeekableByteChannel {
    pointer = newPosition.toInt()
    return this
  }

  override fun size(): Long {
    return end.toLong()
  }

  override fun truncate(size: Long): SeekableByteChannel {
    end = size.toInt()
    return this
  }

  fun view(): ByteBuffer {
    val bb = ByteBuffer.wrap(data)
    bb.order(ByteOrder.LITTLE_ENDIAN)
    bb.position(pointer)
    bb.limit(end)
    return bb
  }
}

class TestDictionary
@JvmOverloads
constructor(
    vararg components: String,
    parent: File? = null,
) : TemporaryFolder(parent) {

  private val resources = run {
    val parts = components.toMutableSet()
    if (parts.contains("system")) {
      parts.add("system_core.dic")
      parts.add("sudachi.json")
      parts.add("unk.def")
    }
    parts
  }

  override fun before() {
    super.before()
    copyResources()
  }

  private fun copyResources() {
    if (resources.isEmpty()) {
      return
    }
    val sudachiFolder = newFolder("config", "sudachi")
    if (resources.contains("sudachi.json")) {
      ResourceUtil.copyResource("sudachi.json", sudachiFolder, false)
    }
    if (resources.contains("unk.def")) {
      ResourceUtil.copyResource("unk.def", sudachiFolder, false)
    }
    if (resources.contains("system_core.dic")) {
      writeSystemDic(sudachiFolder.toPath().resolve("system_core.dic"))
    }
    if (resources.contains("user0.dic")) {
      writeUser0Dic(sudachiFolder.toPath().resolve("user0.dic"))
    }
  }

  private fun writeSystemDic(path: Path) {
    Files.newByteChannel(path, OO.WRITE, OO.TRUNCATE_EXISTING, OO.CREATE).use {
      it.write(inMemorySystemData.duplicate())
    }
  }

  private fun writeUser0Dic(path: Path) {
    Files.newByteChannel(path, OO.WRITE, OO.TRUNCATE_EXISTING, OO.CREATE).use {
      it.write(inMemoryUser0Data.duplicate())
    }
  }

  companion object {
    val inMemorySystemData by lazy {
      val buf = InMemoryByteChannel()
      DicBuilder.system()
          .matrix(resource("dict/matrix.def"))
          .lexicon(resource("dict/lex.csv"))
          .build(buf)
      val view = buf.view()
      view.flip()
      view
    }
  }

  val inMemoryUser0Data by lazy {
    val buf = InMemoryByteChannel()
    DicBuilder.user(BinaryDictionary(inMemorySystemData.duplicate().order(ByteOrder.LITTLE_ENDIAN)))
        .lexicon(resource("dict/user0.csv"))
        .build(buf)
    val view = buf.view()
    view.flip()
    view
  }
}

class InMemoryDictionary {
  val config = run {
    val anchor = PathAnchor.classpath(ResourceUtil::class.java)
    val base = Config.fromClasspath(ResourceUtil.resource("sudachi.json"), anchor)
    val dic = TestDictionary.inMemorySystemData.duplicate()
    dic.order(ByteOrder.LITTLE_ENDIAN)
    base.systemDictionary(BinaryDictionary(dic))
  }

  val dic = newDictionary()

  fun newDictionary() = ReloadableDictionary(config)

  @JvmOverloads
  fun tokenizer(
      data: String? = null,
      discardPunctuation: Boolean = true,
      mode: SplitMode = SplitMode.C
  ): SudachiTokenizer {
    val cache = AnalysisCache(0, NoopInputExtractor.INSTANCE)
    val it = CachingTokenizer(dic.newTokenizer(), mode, cache)
    val tokenizer = SudachiTokenizer(it, discardPunctuation)
    if (data != null) {
      tokenizer.setReader(StringReader(data))
    }
    return tokenizer
  }
}
