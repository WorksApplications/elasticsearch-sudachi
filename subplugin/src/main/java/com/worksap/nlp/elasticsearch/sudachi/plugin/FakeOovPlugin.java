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

package com.worksap.nlp.elasticsearch.sudachi.plugin;

import com.worksap.nlp.sudachi.InputText;
import com.worksap.nlp.sudachi.LatticeNodeImpl;
import com.worksap.nlp.sudachi.OovProviderPlugin;
import com.worksap.nlp.sudachi.dictionary.WordInfo;

import java.util.List;

public class FakeOovPlugin extends OovProviderPlugin {
    @Override
    public int provideOOV(InputText inputText, int offset, long otherWords, List<LatticeNodeImpl> result) {
        LatticeNodeImpl node = createNode();
        int length = inputText.getCharCategoryContinuousLength(offset);
        node.setParameter((short) 1, (short) 1, (short) -500);
        String rd = inputText.getSubstring(offset, offset + length);
        node.setWordInfo(new WordInfo(rd, (short) length, (short) 1, rd, rd, rd));
        result.add(node);
        return 1;
    }
}
