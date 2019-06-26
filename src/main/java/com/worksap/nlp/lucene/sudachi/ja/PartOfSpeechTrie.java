/*
 *  Copyright (c) 2018 Works Applications Co., Ltd.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartOfSpeechTrie {

    static final String EMPTY_SYMBOL = "*";
    static final String LEAF = "";

    Map<String, Object> root = new HashMap<>();

    public void add(String... items) {
        Map<String, Object> node = root;
        for (String item : items) {
            if (EMPTY_SYMBOL.equals(item)) {
                break;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> newNode =
                (Map<String, Object>)node.computeIfAbsent(item, k -> new HashMap<>());
            node = newNode;
        }
        node.put(LEAF, LEAF);
    }

    public boolean isPrefixOf(List<String> items, int begin, int end) {
        if (root.isEmpty()) {
            return false;
        }
        Map<String, Object> node = root;
        for (int i = begin; i < end; i++) {
            String item = items.get(i);
            if (EMPTY_SYMBOL.equals(item)) {
                return node.containsKey(LEAF);
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> newNode = (Map<String, Object>)node.get(item);
            node = newNode;
            if (node == null) {
                return false;
            } else if (node.containsKey(LEAF)) {
                return true;
            }
        }
        return node.containsKey(LEAF);
    }
}
