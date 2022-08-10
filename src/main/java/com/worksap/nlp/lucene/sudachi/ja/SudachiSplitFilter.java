/*
 * Copyright (c) 2020-2022 Works Applications Co., Ltd.
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

package com.worksap.nlp.lucene.sudachi.ja;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

import com.worksap.nlp.lucene.sudachi.ja.attributes.*;
import com.worksap.nlp.lucene.sudachi.ja.util.Strings;
import com.worksap.nlp.sudachi.Morpheme;

import com.worksap.nlp.sudachi.Tokenizer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;

public class SudachiSplitFilter extends TokenFilter {

    public enum Mode {
        SEARCH, EXTENDED
    }

    public static final Mode DEFAULT_MODE = Mode.SEARCH;

    static class OovChars {
        private int length;
        private char[] buffer = new char[0];
        private int reserved;
        private int index;
        private int baseOffset;

        public void setOov(int offset, char[] src, int length) {
            baseOffset = offset;
            this.length = length;
            if (reserved < length) {
                buffer = new char[length];
                reserved = length;
            }
            System.arraycopy(src, 0, buffer, 0, length);
            index = 0;
        }

        public boolean hasNext() {
            return index < length;
        }

        public char next() {
            if (index < length) {
                return buffer[index++];
            } else {
                throw new IllegalStateException();
            }
        }

        public int index() {
            return index;
        }

        public int offset() {
            return baseOffset + index;
        }
    }

    private final Mode mode;
    private final Tokenizer.SplitMode splitMode;
    private final CharTermAttribute termAtt;
    private final OffsetAttribute offsetAtt;
    private final PositionIncrementAttribute posIncAtt;
    private final PositionLengthAttribute posLengthAtt;
    private final MorphemeAttribute morphemeAtt;
    private final MorphemeConsumerAttribute consumerAttribute;
    private ListIterator<Morpheme> aUnitIterator;
    private final OovChars oovChars = new OovChars();

    private int aUnitOffset = 0;

    public SudachiSplitFilter(TokenStream input, Mode mode, Tokenizer.SplitMode splitMode) {
        super(input);
        this.mode = mode;
        this.splitMode = splitMode;

        termAtt = addAttribute(CharTermAttribute.class);
        offsetAtt = addAttribute(OffsetAttribute.class);
        posIncAtt = addAttribute(PositionIncrementAttribute.class);
        posLengthAtt = addAttribute(PositionLengthAttribute.class);
        morphemeAtt = addAttribute(MorphemeAttribute.class);
        consumerAttribute = addAttribute(MorphemeConsumerAttribute.class);
        consumerAttribute.setInstance(this);
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (oovChars.hasNext()) {
            clearAttributes();
            setOOVAttribute();
            return true;
        }
        if (aUnitIterator != null && aUnitIterator.hasNext()) {
            clearAttributes();
            setAUnitAttribute(aUnitIterator.next());
            return true;
        }

        if (input.incrementToken()) {
            int length = 0;
            Morpheme m = morphemeAtt.getMorpheme();
            if (m == null) {
                return true;
            }
            if (consumerAttribute.shouldConsume(this)) {
                termAtt.append(m.surface());
            }
            if (mode == Mode.EXTENDED && m.isOOV() && (length = Strings.codepointCount(termAtt)) > 1) {
                oovChars.setOov(offsetAtt.startOffset(), termAtt.buffer(), termAtt.length());
                posLengthAtt.setPositionLength(length);
            } else if (splitMode != Tokenizer.SplitMode.C) {
                List<Morpheme> subUnits = m.split(splitMode);
                if (subUnits.size() > 1) {
                    aUnitIterator = subUnits.listIterator();
                    aUnitOffset = offsetAtt.startOffset();
                    posLengthAtt.setPositionLength(subUnits.size());
                } else {
                    posLengthAtt.setPositionLength(1);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private void setAUnitAttribute(Morpheme morpheme) {
        posLengthAtt.setPositionLength(1);
        if (aUnitIterator.previousIndex() == 0) {
            posIncAtt.setPositionIncrement(0);
        } else {
            posIncAtt.setPositionIncrement(1);
        }
        int length = morpheme.end() - morpheme.begin();
        offsetAtt.setOffset(aUnitOffset, aUnitOffset + length);
        aUnitOffset += length;
        morphemeAtt.setMorpheme(morpheme);
        if (consumerAttribute.shouldConsume(this)) {
            termAtt.append(morpheme.surface());
        }
    }

    private void setOOVAttribute() {
        int offset = oovChars.offset();
        posLengthAtt.setPositionLength(1);
        if (oovChars.index() == 0) {
            posIncAtt.setPositionIncrement(0);
        } else {
            posIncAtt.setPositionIncrement(1);
        }
        char c = oovChars.next();
        termAtt.append(c);
        if (Character.isSurrogate(c) && oovChars.hasNext()) {
            termAtt.append(oovChars.next());
            offsetAtt.setOffset(offset, offset + 2);
        } else {
            offsetAtt.setOffset(offset, offset + 1);
        }
    }
}