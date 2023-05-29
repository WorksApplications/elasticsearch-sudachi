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

package com.worksap.nlp.sudachi;

import java.util.List;

import com.worksap.nlp.sudachi.dictionary.Grammar;

public class JoinOovPlugin extends PathRewritePlugin {

    short oovPosId;

    @Override
    public void setUp(Grammar grammar) {
        List<String> pos = settings.getStringList("oovPOS");
        if (pos.isEmpty()) {
            throw new IllegalArgumentException("oovPOS is undefined");
        }
        oovPosId = grammar.getPartOfSpeechId(pos);
        if (oovPosId < 0) {
            throw new IllegalArgumentException("oovPOS is invalid");
        }
    }

    @Override
    public void rewrite(InputText text, List<LatticeNode> path, Lattice lattice) {
        boolean isOOVNode = false;
        int begin = 0;

        for (int i = 0; i < path.size(); i++) {
            LatticeNode node = path.get(i);
            if (node.isOOV()) {
                if (!isOOVNode) {
                    begin = i;
                    isOOVNode = true;
                }
            } else {
                if (isOOVNode) {
                    isOOVNode = false;
                    concatenateOov(path, begin, i, oovPosId, lattice);
                    i = begin + 1;
                }
            }
        }
        if (isOOVNode) {
            concatenateOov(path, begin, path.size(), oovPosId, lattice);
        }
    }
}
