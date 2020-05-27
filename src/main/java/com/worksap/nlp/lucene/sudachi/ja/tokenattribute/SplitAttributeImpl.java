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

package com.worksap.nlp.lucene.sudachi.ja.tokenattribute;

import java.util.Collections;
import java.util.List;

import com.worksap.nlp.sudachi.Morpheme;
import com.worksap.nlp.sudachi.Tokenizer.SplitMode;

import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.AttributeReflector;

public class SplitAttributeImpl extends AttributeImpl implements SplitAttribute {
    private Morpheme morpheme;

    public List<Morpheme> getAUnits() {
        return morpheme == null ? Collections.emptyList() : morpheme.split(SplitMode.A);
    }

    public List<Morpheme> getBUnits() {
        return morpheme == null ? Collections.emptyList() : morpheme.split(SplitMode.B);
    }

    public boolean isOOV() {
        return morpheme == null ? false : morpheme.isOOV();
    }

    public void setMorpheme(Morpheme morpheme) {
        this.morpheme = morpheme;
    }

    @Override
    public void clear() {
        this.morpheme = null;
    }

    @Override
    public void copyTo(AttributeImpl target) {
        SplitAttribute t = (SplitAttribute) target;
        t.setMorpheme(morpheme);
    }

    @Override
    public void reflectWith(AttributeReflector reflector) {
        if (isOOV()) {
            reflector.reflect(SplitAttribute.class, "aUnits", null);
            reflector.reflect(SplitAttribute.class, "bUnits", null);
            reflector.reflect(SplitAttribute.class, "isOOV", true);
        } else {
            reflector.reflect(SplitAttribute.class, "aUnits", null);
            reflector.reflect(SplitAttribute.class, "bUnits", null);
            reflector.reflect(SplitAttribute.class, "isOOV", false);
        }
   }
}