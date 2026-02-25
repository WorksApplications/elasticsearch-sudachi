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

package com.worksap.nlp.lucene.sudachi.ja.attributes

import com.worksap.nlp.sudachi.Config
import com.worksap.nlp.sudachi.DictionaryFactory
import com.worksap.nlp.sudachi.Morpheme
import com.worksap.nlp.test.TestDictionary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import org.junit.Before
import org.junit.Rule

class MorphemeAttributeImplTest {
  @JvmField @Rule var testDic = TestDictionary("system")

  private lateinit var config: Config

  fun getFirstMorpheme(text: String): Morpheme? {
    val dict = DictionaryFactory().create(config)
    val tok = dict.create()
    val morphemes = tok.tokenize(text)

    return if (morphemes.size == 0) null else morphemes.get(0)
  }

  @Before
  fun setup() {
    val configDir = testDic.root.toPath().resolve("config/sudachi")
    config = Config.fromFile(configDir.resolve("sudachi.json")).allowEmptyMorpheme(false)
  }

  @Test
  fun setMorpheme() {
    var morphemeAtt = MorphemeAttributeImpl()
    assertNull(morphemeAtt.getMorpheme())

    val morpheme = getFirstMorpheme("東京都")!!
    morphemeAtt.setMorpheme(morpheme)
    assertEquals(morpheme, morphemeAtt.getMorpheme())

    morphemeAtt.setMorpheme(null)
    assertNull(morphemeAtt.getMorpheme())
  }

  @Test
  fun setOffsets() {
    var morphemeAtt = MorphemeAttributeImpl()
    assertTrue(morphemeAtt.getOffsets().isEmpty())

    val intlist = listOf(1, 2, 3)
    morphemeAtt.setOffsets(intlist)
    assertEquals(intlist, morphemeAtt.getOffsets())

    morphemeAtt.setOffsets(listOf())
    assertTrue(morphemeAtt.getOffsets().isEmpty())
  }

  @Test
  fun copyTo() {
    var morphemeAtt1 = MorphemeAttributeImpl()
    var morphemeAtt2 = MorphemeAttributeImpl()
    val morpheme = getFirstMorpheme("東京都")!!

    morphemeAtt1.setMorpheme(morpheme)
    morphemeAtt1.copyTo(morphemeAtt2)
    assertEquals(morpheme, morphemeAtt2.getMorpheme())

    morphemeAtt1.setMorpheme(null)
    morphemeAtt1.copyTo(morphemeAtt2)
    assertNull(morphemeAtt2.getMorpheme())
  }

  @Test
  fun reflectWith() {
    var morphemeAtt = MorphemeAttributeImpl()
    val morpheme = getFirstMorpheme("東京都")!!
    morphemeAtt.setMorpheme(morpheme)
    val offsets = listOf(0, 1, 2, 3)
    morphemeAtt.setOffsets(offsets)

    var map = mutableMapOf<String, JsonElement>()
    morphemeAtt.reflectWith(
        fun(attClass, key, value) {
          assertEquals(MorphemeAttribute::class.java, attClass)
          map[key] = toJsonElement(value)
        }
    )

    val serialized = JsonObject(map).toString()
    val deserialized = Json.decodeFromString<MorphemeHolder>(serialized)

    assertNotNull(deserialized.morpheme)
    assertEquals(morpheme.surface(), deserialized.morpheme.surface)
    assertEquals(morpheme.dictionaryForm(), deserialized.morpheme.dictionaryForm)
    assertEquals(morpheme.normalizedForm(), deserialized.morpheme.normalizedForm)
    assertEquals(morpheme.readingForm(), deserialized.morpheme.readingForm)
    assertEquals(morpheme.partOfSpeech(), deserialized.morpheme.partOfSpeech)
    assertEquals(offsets, deserialized.morpheme.offsetMap)
  }

  @Test
  fun reflectWithNullMorpheme() {
    var morphemeAtt = MorphemeAttributeImpl()

    var map = mutableMapOf<String, JsonElement>()
    morphemeAtt.reflectWith(
        fun(attClass, key, value) {
          assertEquals(MorphemeAttribute::class.java, attClass)
          map[key] = toJsonElement(value)
        }
    )

    val serialized = JsonObject(map).toString()
    val deserialized = Json.decodeFromString<MorphemeHolder>(serialized)
    assertNull(deserialized.morpheme)
  }
}

fun toJsonElement(value: Any?): JsonElement {
  return when (value) {
    null -> JsonNull
    is String -> JsonPrimitive(value)
    is Boolean -> JsonPrimitive(value)
    is Number -> JsonPrimitive(value)
    is Map<*, *> -> JsonObject(value.entries.associate {(k, v) -> k.toString() to toJsonElement(v)})
    is Collection<*> -> JsonArray(value.map { toJsonElement(it) })
    else -> JsonPrimitive(value.toString())
  }
}

@Serializable
data class MorphemeHolder(
    val morpheme: MorphemeAttributeHolder?,
)

@Serializable
data class MorphemeAttributeHolder(
    val surface: String,
    val dictionaryForm: String,
    val normalizedForm: String,
    val readingForm: String,
    val partOfSpeech: List<String>,
    val offsetMap: List<Int>,
)
