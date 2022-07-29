/*
 * Copyright (c) 2022 Works Applications Co., Ltd.
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

package com.worksap.nlp.lucene.sudachi.ja.util;

public class Strings {
    private Strings() {
    }

    private static final long PUNCTUATION_MASK = makeMask(Character.SPACE_SEPARATOR, Character.LINE_SEPARATOR,
            Character.PARAGRAPH_SEPARATOR, Character.CONTROL, Character.FORMAT, Character.DASH_PUNCTUATION,
            Character.START_PUNCTUATION, Character.END_PUNCTUATION, Character.CONNECTOR_PUNCTUATION,
            Character.OTHER_PUNCTUATION, Character.MATH_SYMBOL, Character.CURRENCY_SYMBOL, Character.MODIFIER_SYMBOL,
            Character.OTHER_SYMBOL, Character.INITIAL_QUOTE_PUNCTUATION, Character.FINAL_QUOTE_PUNCTUATION);

    public static boolean isPunctuation(CharSequence str) {
        int length = str.length();
        if (length == 0) {
            return false;
        }

        for (int idx = 0; idx < length;) {
            int codePt = Character.codePointAt(str, idx);
            if (!hasType(codePt, PUNCTUATION_MASK)) {
                return false;
            }
            idx += Character.charCount(codePt);
        }
        return true;
    }

    public static boolean hasType(int codepoint, long typeMask) {
        int type = Character.getType(codepoint);
        long mask = 1L << type;
        return (mask & typeMask) != 0;
    }

    public static long makeMask(Byte... values) {
        long mask = 0;
        for (byte value : values) {
            mask = mask | (1L << value);
        }
        return mask;
    }

    public static int codepointCount(CharSequence seq) {
        return Character.codePointCount(seq, 0, seq.length());
    }
}
