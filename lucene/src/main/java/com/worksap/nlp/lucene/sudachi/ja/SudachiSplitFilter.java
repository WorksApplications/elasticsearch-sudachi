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

package com.worksap.nlp.lucene.sudachi.ja;

import java.io.IOException;
import java.util.List;

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

    private final Mode mode;
    private final Tokenizer.SplitMode splitMode;

    private final CharTermAttribute termAtt;
    private final OffsetAttribute offsetAtt;
    private final PositionIncrementAttribute posIncAtt;
    private final PositionLengthAttribute posLengthAtt;
    private final MorphemeAttribute morphemeAtt;

    private final MorphemeSubunits subunits = new MorphemeSubunits();
    private final OovChars oovChars = new OovChars();
    private List<Integer> offsetMap;

    public SudachiSplitFilter(TokenStream input, Mode mode, Tokenizer.SplitMode splitMode) {
        super(input);
        this.mode = mode;
        this.splitMode = splitMode;

        termAtt = addAttribute(CharTermAttribute.class);
        offsetAtt = addAttribute(OffsetAttribute.class);
        posIncAtt = addAttribute(PositionIncrementAttribute.class);
        posLengthAtt = addAttribute(PositionLengthAttribute.class);
        morphemeAtt = addAttribute(MorphemeAttribute.class);
    }

    @Override
    public final boolean incrementToken() throws IOException {
        // continue to write current split
        if (oovChars.hasNext()) {
            clearAttributes();
            setOOVAttribute();
            return true;
        }
        if (subunits.hasNext()) {
            clearAttributes();
            setAUnitAttribute();
            return true;
        }

        // move to next morpheme
        if (!input.incrementToken()) {
            return false;
        }

        Morpheme m = morphemeAtt.getMorpheme();
        this.offsetMap = morphemeAtt.getOffsets();
        if (m == null) {
            return true;
        }

        // oov does not have splits
        // split into characters in extended mode
        if (m.isOOV()) {
            int length = 0;
            if (mode == Mode.EXTENDED && (length = Strings.codepointCount(termAtt)) > 1) {
                // OovChars requires character length
                oovChars.setOov(termAtt.buffer(), termAtt.length());
                // Position length should be codepoint length
                posLengthAtt.setPositionLength(length);
            }
            return true;
        }

        // C split is the longest split
        if (splitMode == Tokenizer.SplitMode.C) {
            return true;
        }

        // split into A/B units
        List<Morpheme> subsplits = m.split(splitMode);
        if (subsplits.size() > 1) {
            subunits.setUnits(subsplits);
            posLengthAtt.setPositionLength(subunits.size());
        }

        return true;
    }

    private int correctOffset(int currectOff) {
        return this.offsetMap.get(currectOff);
    }

    private void setAUnitAttribute() {
        posLengthAtt.setPositionLength(1);
        if (subunits.index() == 0) {
            posIncAtt.setPositionIncrement(0);
        } else {
            posIncAtt.setPositionIncrement(1);
        }

        MorphemeSubunits.Subunit su = subunits.next();
        termAtt.setEmpty().append(su.morpheme.surface());
        morphemeAtt.setMorpheme(su.morpheme);
        morphemeAtt.setOffsets(offsetMap.subList(su.begin, su.end + 1));
        offsetAtt.setOffset(correctOffset(su.begin), correctOffset(su.end));
    }

    private void setOOVAttribute() {
        posLengthAtt.setPositionLength(1);
        if (oovChars.index() == 0) {
            posIncAtt.setPositionIncrement(0);
        } else {
            posIncAtt.setPositionIncrement(1);
        }

        int startOffset = oovChars.offset();
        char c = oovChars.next();
        termAtt.setEmpty().append(c);
        if (Character.isSurrogate(c) && oovChars.hasNext()) {
            termAtt.append(oovChars.next());
        }
        int endOffset = oovChars.offset();
        offsetAtt.setOffset(correctOffset(startOffset), correctOffset(endOffset));
    }

    static class OovChars {
        private int reserved;
        private char[] buffer = new char[0];
        private int length;
        private int index;

        public void setOov(char[] src, int length) {
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
            }
            throw new IllegalStateException();
        }

        public int index() {
            return index;
        }

        public int offset() {
            return index;
        }
    }

    static class MorphemeSubunits {
        static class Subunit {
            final Morpheme morpheme;
            final int begin;
            final int end;

            public Subunit(Morpheme morpheme, int begin, int end) {
                this.morpheme = morpheme;
                this.begin = begin;
                this.end = end;
            }
        }

        private List<Morpheme> morphemes;
        private int size;
        private int index;
        private int baseOffset;

        public void setUnits(List<Morpheme> morphemes) {
            this.morphemes = morphemes;
            size = morphemes.size();
            index = 0;
            baseOffset = morphemes.get(0).begin();
        }

        public boolean hasNext() {
            return index < size;
        }

        public Subunit next() {
            if (!hasNext()) {
                throw new IllegalStateException();
            }
            Morpheme m = morphemes.get(index++);
            return new Subunit(m, m.begin() - baseOffset, m.end() - baseOffset);
        }

        public int size() {
            return size;
        }

        public int index() {
            return index;
        }
    }
}