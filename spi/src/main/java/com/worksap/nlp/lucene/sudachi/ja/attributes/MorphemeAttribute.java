/*
 * Copyright (c) 2023 Works Applications Co., Ltd.
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

package com.worksap.nlp.lucene.sudachi.ja.attributes;

import com.worksap.nlp.sudachi.Morpheme;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Attribute;

/** Access the current Sudachi morpheme within the token stream. */
public interface MorphemeAttribute extends Attribute {
    /**
     *
     * @return current {@link Morpheme} object, will be null if called before
     *         {@link TokenStream#incrementToken()}
     */
    Morpheme getMorpheme();

    /**
     * Replace the current {@link Morpheme} object with the provided one
     * 
     * @param morpheme
     *            new object
     */
    void setMorpheme(Morpheme morpheme);
}
