/*
 *  Copyright (c) 2017 Works Applications Co., Ltd.
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
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;
import org.apache.lucene.util.AttributeFactory;

import com.worksap.nlp.lucene.sudachi.ja.tokenattribute.BaseFormAttribute;
import com.worksap.nlp.lucene.sudachi.ja.tokenattribute.NormalizedFormAttribute;
import com.worksap.nlp.lucene.sudachi.ja.tokenattribute.PartOfSpeechAttribute;
import com.worksap.nlp.lucene.sudachi.ja.tokenattribute.ReadingAttribute;
import com.worksap.nlp.sudachi.Dictionary;
import com.worksap.nlp.sudachi.DictionaryFactory;
import com.worksap.nlp.sudachi.Morpheme;
import com.worksap.nlp.sudachi.Tokenizer;

public final class SudachiTokenizer extends
        org.apache.lucene.analysis.Tokenizer {
    public static final Mode DEFAULT_MODE = Mode.SEARCH;

    public static enum Mode {
        NORMAL, SEARCH, EXTENDED
    }

    private final boolean discardPunctuation;
    private final Mode mode;
    private final Dictionary dictionary;
    private final Tokenizer tokenizer;
    private final CharTermAttribute termAtt;
    private final BaseFormAttribute basicFormAtt;
    private final NormalizedFormAttribute normFormAtt;
    private final PartOfSpeechAttribute posAtt;
    private final ReadingAttribute readingAtt;
    private final OffsetAttribute offsetAtt;
    private final PositionIncrementAttribute posIncAtt;
    private final PositionLengthAttribute posLengthAtt;

    private Iterator<List<Morpheme>> sentenceIterator;
    private Iterator<Morpheme> morphemeIterator;
    private ListIterator<Morpheme> aUnitIterator;
    private ListIterator<String> oovIterator;

    private int baseOffset = 0;
    private int sentenceLength = 0;
    private int oovBegin = 0;
    private int aUnitSize = 0;
    private int oovSize = 0;

    public SudachiTokenizer(boolean discardPunctuation, Mode mode,
            String resourcesPath, String settings) throws IOException {
        this(DEFAULT_TOKEN_ATTRIBUTE_FACTORY, discardPunctuation, mode,
                resourcesPath, settings);
    }

    public SudachiTokenizer(AttributeFactory factory,
            boolean discardPunctuation, Mode mode, String path, String settings)
            throws IOException {
        super(factory);
        this.discardPunctuation = discardPunctuation;
        this.mode = mode;
        dictionary = new DictionaryFactory().create(path, settings);
        tokenizer = dictionary.create();

        termAtt = addAttribute(CharTermAttribute.class);
        basicFormAtt = addAttribute(BaseFormAttribute.class);
        normFormAtt = addAttribute(NormalizedFormAttribute.class);
        posAtt = addAttribute(PartOfSpeechAttribute.class);
        readingAtt = addAttribute(ReadingAttribute.class);
        offsetAtt = addAttribute(OffsetAttribute.class);
        posIncAtt = addAttribute(PositionIncrementAttribute.class);
        posLengthAtt = addAttribute(PositionLengthAttribute.class);
    }

    @Override
    public final boolean incrementToken() throws IOException {
        clearAttributes();
        if (oovIterator != null && oovIterator.hasNext()) {
            setOOVAttribute(oovIterator.next());
            return true;
        }
        if (aUnitIterator != null && aUnitIterator.hasNext()) {
            setAUnitAttribute(aUnitIterator.next());
            return true;
        }
        Morpheme morpheme = getNextMorpheme();
        if (morpheme == null) {
            return false;
        }
        if (discardPunctuation) {
            for (; morpheme != null && isPunctuation(morpheme.normalizedForm()); morpheme = getNextMorpheme()) {
                // do nothing
            }
            if (morpheme == null) {
                return false;
            }
        }

        if (mode == Mode.EXTENDED && morpheme.isOOV()) {
            oovBegin = morpheme.begin();
            oovSize = morpheme.surface().length();
            oovIterator = Arrays.asList(morpheme.surface().split(""))
                    .listIterator();
        } else if (mode != Mode.NORMAL) {
            List<Morpheme> aUnits = morpheme
                    .split(com.worksap.nlp.sudachi.Tokenizer.SplitMode.A);
            if (aUnits.size() != 1) {
                aUnitSize = aUnits.size();
                aUnitIterator = aUnits.listIterator();
            }
        }
        setAttribute(morpheme);
        return true;
    }

    private Morpheme getNextMorpheme() throws IOException {
        if (morphemeIterator != null && morphemeIterator.hasNext()) {
            Morpheme morpheme = morphemeIterator.next();
            sentenceLength = morpheme.end();
            return morpheme;
        }
        if (sentenceIterator != null && sentenceIterator.hasNext()) {
            morphemeIterator = sentenceIterator.next().iterator();
            baseOffset += sentenceLength;
            sentenceLength = 0;
            return getNextMorpheme();
        }
        sentenceIterator = tokenizer.tokenizeSentences(input).iterator();
        if (sentenceIterator.hasNext()) {
            return getNextMorpheme();
        }
        return null;
    }

    private void setAttribute(Morpheme morpheme) throws IOException {
        if (aUnitSize != 0) {
            posLengthAtt.setPositionLength(aUnitSize);
            aUnitSize = 0;
        } else if (oovSize != 0) {
            posLengthAtt.setPositionLength(oovSize);
            oovSize = 0;
        } else {
            posLengthAtt.setPositionLength(1);
        }
        posIncAtt.setPositionIncrement(1);
        setMorphemeAttributes(morpheme);
    }

    private void setAUnitAttribute(Morpheme morpheme) throws IOException {
        posLengthAtt.setPositionLength(1);
        if (aUnitIterator.previousIndex() == 0) {
            posIncAtt.setPositionIncrement(0);
        } else {
            posIncAtt.setPositionIncrement(1);
        }
        setMorphemeAttributes(morpheme);
    }

    private void setMorphemeAttributes(Morpheme morpheme) throws IOException {
        basicFormAtt.setMorpheme(morpheme);
        normFormAtt.setMorpheme(morpheme);
        posAtt.setMorpheme(morpheme);
        readingAtt.setMorpheme(morpheme);
        offsetAtt.setOffset(baseOffset + morpheme.begin(),
                            baseOffset + morpheme.end());
        setTermAttribute(morpheme.surface());
    }

    private void setOOVAttribute(String str) throws IOException {
        offsetAtt.setOffset(baseOffset + oovBegin, baseOffset + oovBegin + 1);
        oovBegin += 1;
        posLengthAtt.setPositionLength(1);
        if (oovIterator.previousIndex() == 0) {
            posIncAtt.setPositionIncrement(0);
        } else {
            posIncAtt.setPositionIncrement(1);
        }
        setTermAttribute(str);
    }

    private void setTermAttribute(String str) throws IOException {
        int upto = 0;
        char[] termAttrBuffer = termAtt.buffer();
        try (Reader inputSudachi = new StringReader(str)) {
            while (true) {
                final int length = inputSudachi.read(termAttrBuffer, upto, termAttrBuffer.length - upto);
                if (length == -1) {
                    break;
                }
                upto += length;
                if (upto == termAttrBuffer.length) {
                    termAttrBuffer = termAtt.resizeBuffer(1 + termAttrBuffer.length);
                }
            }
            termAtt.setLength(upto);
        }
    }
    private boolean isPunctuation(String str) {
        if (str.length() == 0) {
            return false;
        }
        return str.codePoints().allMatch(c -> {
                switch (Character.getType(c)) {
                case Character.SPACE_SEPARATOR:
                case Character.LINE_SEPARATOR:
                case Character.PARAGRAPH_SEPARATOR:
                case Character.CONTROL:
                case Character.FORMAT:
                case Character.DASH_PUNCTUATION:
                case Character.START_PUNCTUATION:
                case Character.END_PUNCTUATION:
                case Character.CONNECTOR_PUNCTUATION:
                case Character.OTHER_PUNCTUATION:
                case Character.MATH_SYMBOL:
                case Character.CURRENCY_SYMBOL:
                case Character.MODIFIER_SYMBOL:
                case Character.OTHER_SYMBOL:
                case Character.INITIAL_QUOTE_PUNCTUATION:
                case Character.FINAL_QUOTE_PUNCTUATION:
                    return true;
                default:
                    return false;
                }
            });
    }

    @Override
    public final void end() throws IOException {
        super.end();
        int lastOffset = baseOffset + sentenceLength;
        offsetAtt.setOffset(lastOffset , lastOffset);
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        baseOffset = 0;
        sentenceLength = 0;
        oovBegin = 0;
        morphemeIterator = null;
        aUnitIterator = null;
        oovIterator = null;
        aUnitSize = 0;
        oovSize = 0;
    }
}
