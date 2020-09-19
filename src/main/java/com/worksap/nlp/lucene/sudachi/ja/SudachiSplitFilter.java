/*
 *  Copyright (c) 2020 Works Applications Co., Ltd.
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

import com.worksap.nlp.lucene.sudachi.ja.tokenattribute.BaseFormAttribute;
import com.worksap.nlp.lucene.sudachi.ja.tokenattribute.NormalizedFormAttribute;
import com.worksap.nlp.lucene.sudachi.ja.tokenattribute.PartOfSpeechAttribute;
import com.worksap.nlp.lucene.sudachi.ja.tokenattribute.ReadingAttribute;
import com.worksap.nlp.lucene.sudachi.ja.tokenattribute.SplitAttribute;
import com.worksap.nlp.sudachi.Morpheme;

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

    class OovChars {
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
            return (index < length) ? buffer[index++] : null;
        }

        public int index() {
            return index;
        }

        public int offset() {
            return baseOffset + index;
        }
    }

    private final Mode mode;

    private final CharTermAttribute termAtt;
    private final BaseFormAttribute basicFormAtt;
    private final NormalizedFormAttribute normFormAtt;
    private final PartOfSpeechAttribute posAtt;
    private final ReadingAttribute readingAtt;
    private final OffsetAttribute offsetAtt;
    private final PositionIncrementAttribute posIncAtt;
    private final PositionLengthAttribute posLengthAtt;
    private final SplitAttribute splitAtt;

    private ListIterator<Morpheme> aUnitIterator;
    private OovChars oovChars = new OovChars();

    private int aUnitOffset = 0;
    
    public SudachiSplitFilter(TokenStream input, Mode mode) {
        super(input);
        this.mode = mode;

        termAtt = addAttribute(CharTermAttribute.class);
        basicFormAtt = addAttribute(BaseFormAttribute.class);
        normFormAtt = addAttribute(NormalizedFormAttribute.class);
        posAtt = addAttribute(PartOfSpeechAttribute.class);
        readingAtt = addAttribute(ReadingAttribute.class);
        offsetAtt = addAttribute(OffsetAttribute.class);
        posIncAtt = addAttribute(PositionIncrementAttribute.class);
        posLengthAtt = addAttribute(PositionLengthAttribute.class);
        splitAtt = addAttribute(SplitAttribute.class);
    }

    public SudachiSplitFilter(TokenStream input) {
        this(input, DEFAULT_MODE);
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
            if (mode == Mode.EXTENDED && splitAtt.isOOV() && (length = codePointCount(termAtt)) > 1) {
                oovChars.setOov(offsetAtt.startOffset(), termAtt.buffer(), termAtt.length());
                posLengthAtt.setPositionLength(length);
            } else {
                List<Morpheme> aUnits = splitAtt.getAUnits();
                if (aUnits.size() > 1) {
                    aUnitIterator = aUnits.listIterator();
                    aUnitOffset = offsetAtt.startOffset();
                    posLengthAtt.setPositionLength(aUnits.size());
                } else {
                    posLengthAtt.setPositionLength(1);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private void setAUnitAttribute(Morpheme morpheme) throws IOException {
        posLengthAtt.setPositionLength(1);
        if (aUnitIterator.previousIndex() == 0) {
            posIncAtt.setPositionIncrement(0);
        } else {
            posIncAtt.setPositionIncrement(1);
        }
        basicFormAtt.setMorpheme(morpheme);
        normFormAtt.setMorpheme(morpheme);
        posAtt.setMorpheme(morpheme);
        readingAtt.setMorpheme(morpheme);
        int length = morpheme.end() - morpheme.begin();
        offsetAtt.setOffset(aUnitOffset, aUnitOffset + length);
        aUnitOffset += length;
        termAtt.append(morpheme.surface());
    }

    private void setOOVAttribute() throws IOException {
        int offset = oovChars.offset();
        offsetAtt.setOffset(offset, offset + 1);
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
        }
    }

    private int codePointCount(CharTermAttribute attr) {
        int count = attr.length();
        for (int i = 0; i < attr.length(); ) {
            if (Character.isHighSurrogate(attr.charAt(i++)) &&
                Character.isLowSurrogate(attr.charAt(i))) {
                    count--;
                    i++;
                }
        }
        return count;
    }
}