/*
 * Copyright (c) 2020-2026 Works Applications Co., Ltd.
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

import com.worksap.nlp.lucene.sudachi.ja.attributes.*
import com.worksap.nlp.lucene.sudachi.ja.util.Romanizer
import com.worksap.nlp.sudachi.Morpheme
import java.io.IOException
import org.apache.lucene.analysis.TokenFilter
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.tokenattributes.*

public class SudachiCompletionFilter(input: TokenStream, mode: Mode = DEFAULT_MODE) :
    TokenFilter(input) {

  public enum class Mode {
    INDEX,
    QUERY,
  }

  companion object {
    public val DEFAULT_MODE: Mode = Mode.INDEX
  }

  private val tokenGenerator: CompletionTokenGenerator

  private val termAtt: CharTermAttribute = addAttribute(CharTermAttribute::class.java)
  private val offsetAtt: OffsetAttribute = addAttribute(OffsetAttribute::class.java)
  private val posIncAtt: PositionIncrementAttribute =
      addAttribute(PositionIncrementAttribute::class.java)
  private val morphemeAtt: MorphemeAttribute = addAttribute(MorphemeAttribute::class.java)

  private var inputStreamConsumed: Boolean = false

  private data class CompletionToken(
      val term: String,
      val isFirst: Boolean,
      val offsetMap: List<Int>,
  )

  init {
    this.tokenGenerator = CompletionTokenGenerator(mode)
  }

  @Throws(IOException::class)
  override fun reset() {
    super.reset()
    tokenGenerator.reset()
    inputStreamConsumed = false
  }

  @Throws(IOException::class)
  override fun incrementToken(): Boolean {
    while (!tokenGenerator.hasNext()) {
      if (!inputStreamConsumed && input.incrementToken()) {
        val m: Morpheme = morphemeAtt.getMorpheme()
        val surface: String = m.surface()
        val reading: String? = m.readingForm()
        val offsetMap: List<Int> = listOf(offsetAtt.startOffset(), offsetAtt.endOffset())

        val effectiveReading: String? =
            if (reading.isNullOrEmpty() && tokenGenerator.isKanaOrMixed(surface)) {
              tokenGenerator.convertHiraganaToKatakana(surface)
            } else {
              reading?.ifEmpty { null }
            }

        tokenGenerator.addToken(surface, effectiveReading, offsetMap)
      } else {
        inputStreamConsumed = true
        if (tokenGenerator.hasPendingToken()) tokenGenerator.finish() else break
      }
    }

    if (tokenGenerator.hasNext()) {
      clearAttributes()
      val token: CompletionToken = tokenGenerator.next()

      termAtt.setEmpty().append(token.term)
      if (token.isFirst) posIncAtt.setPositionIncrement(1) else posIncAtt.setPositionIncrement(0)
      offsetAtt.setOffset(token.offsetMap[0], token.offsetMap[1])
      return true
    } else return false
  }

  private class CompletionTokenGenerator(mode: Mode) : Iterator<CompletionToken> {
    private val mode: Mode

    private val outputs: ArrayList<CompletionToken> = ArrayList()

    private var pdgSurface = StringBuilder()
    private var pdgReading = StringBuilder()
    private var pdgOffsetMap = mutableListOf(0, 0)

    private var hasPdgToken = false

    init {
      this.mode = mode
    }

    public fun reset() {
      clearPendingToken()
      outputs.clear()
    }

    override fun hasNext(): Boolean {
      return outputs.size > 0
    }

    override fun next(): CompletionToken {
      return outputs.removeAt(0)
    }

    public fun addToken(surface: CharSequence, reading: CharSequence?, offsetMap: List<Int>) {
      if (hasPendingToken()) {
        if (mode == Mode.QUERY) {
          when {
            !isAllLowercaseAlphabet(pdgSurface.toString()) &&
                isAllLowercaseAlphabet(surface.toString()) -> {
              pdgSurface.append(surface)
              pdgReading.append(surface)
              pdgOffsetMap[1] = offsetMap[1]
              generateOutputs()
              clearPendingToken()
            }
            isKanaOnly(pdgSurface.toString()) && isKanaOnly(surface.toString()) -> {
              pdgSurface.append(surface)
              if (reading != null) pdgReading.append(reading)
              pdgOffsetMap[1] = offsetMap[1]
            }
            isKanaOnly(pdgSurface.toString()) && isAllLowercaseAlphabet(surface.toString()) -> {
              pdgSurface.append(surface)
              pdgReading.append(surface)
              pdgOffsetMap[1] = offsetMap[1]
            }
            else -> {
              generateOutputs()
              resetPendingToken(surface.toString(), reading?.toString(), offsetMap)
            }
          }
        } else {
          generateOutputs()
          resetPendingToken(surface.toString(), reading?.toString(), offsetMap)
        }
      } else {
        resetPendingToken(surface.toString(), reading?.toString(), offsetMap)
      }
    }

    public fun finish() {
      generateOutputs()
      clearPendingToken()
    }

    private fun generateOutputs() {
      outputs.add(CompletionToken(pdgSurface.toString(), true, pdgOffsetMap.toList()))
      val reading = pdgReading.toString()
      if (reading.isEmpty() || !isRomanizableReading(reading)) return

      val romaji: List<String> = romanize(reading)
      romaji.forEach { outputs.add(CompletionToken(it, false, pdgOffsetMap.toList())) }
    }

    public fun hasPendingToken(): Boolean {
      return hasPdgToken
    }

    private fun resetPendingToken(surface: String, reading: String?, offsetMap: List<Int>) {
      this.hasPdgToken = true
      this.pdgSurface.clear()
      this.pdgReading.clear()
      this.pdgSurface.append(surface)
      if (reading != null) {
        this.pdgReading.append(reading)
      }
      this.pdgOffsetMap = offsetMap.toMutableList()
    }

    private fun clearPendingToken() {
      this.hasPdgToken = false
      this.pdgSurface.clear()
      this.pdgReading.clear()
      this.pdgOffsetMap = mutableListOf(0, 0)
    }

    private fun romanize(text: String): List<String> {
      val output = mutableListOf<String>()
      val romaji: String = convertFullWidthAlphabetToHalfWidth(Romanizer.romanize(text))
      output.add(romaji)
      return output
    }

    private fun Char.isHalfWidthLowercase(): Boolean = this in 'a'..'z'

    private fun Char.isFullWidthLowercase(): Boolean = this in 'ａ'..'ｚ'

    private fun Char.isAnyLowercaseAlphabet(): Boolean =
        this.isHalfWidthLowercase() || this.isFullWidthLowercase()

    public fun isAllLowercaseAlphabet(input: String): Boolean {
      return input.all { it.isAnyLowercaseAlphabet() }
    }

    private fun Char.isHiragana(): Boolean = this.code in 0x3040..0x309f

    private fun Char.isKatakana(): Boolean = this.code in 0x30a0..0x30ff

    public fun isKanaOnly(input: String): Boolean {
      return input.isNotEmpty() && input.all { it.isHiragana() || it.isKatakana() }
    }

    public fun isKanaOrMixed(input: String): Boolean {
      val hasKana = input.any { it.isHiragana() || it.isKatakana() }
      val allValid = input.all { it.isHiragana() || it.isKatakana() || it.isAnyLowercaseAlphabet() }
      return hasKana && allValid
    }

    private fun Char.isFullWidthKatakana(): Boolean = this.code in 0x30a0..0x30ff

    private fun isRomanizableReading(s: String): Boolean {
      return s.all { ch ->
        ch.isFullWidthKatakana() || ch.isHalfWidthLowercase() || ch.isFullWidthLowercase()
      }
    }

    public fun convertHiraganaToKatakana(input: String): String {
      return input
          .map { ch ->
            if (
                ch.isHiragana() && ch != '\u309f'
            ) { // 0x309f is Hiragana Digraph Yori, typically not mapped or mapped separately
              (ch.code + 0x60).toChar()
            } else {
              ch
            }
          }
          .joinToString("")
    }

    public fun convertFullWidthAlphabetToHalfWidth(input: String): String {
      return input
          .map { char ->
            when (char) {
              in 'Ａ'..'Ｚ' -> (char.code - 0xFEE0).toChar() // 全角大文字 A〜Z
              in 'ａ'..'ｚ' -> (char.code - 0xFEE0).toChar() // 全角小文字 a〜z
              else -> char
            }
          }
          .joinToString("")
    }
  }
}
