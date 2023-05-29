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

import com.worksap.nlp.lucene.sudachi.ja.CurrentDictionary;
import org.apache.lucene.util.Attribute;

/**
 * This attribute is used to get the current Sudachi instance from the
 * Sudachi-based token streams. Sudachi will be wrapped in logic which supports
 * dictionary hot reloading (reload itself is not implemented yet).
 */
public interface SudachiAttribute extends Attribute {
    /**
     * Get current dictionary instance
     * 
     * @return reloadable facade for the current dictionary
     */
    CurrentDictionary getDictionary();

    /**
     * Set the current dictionary for the token stream. Use this method only if you
     * really know what you are doing.
     * 
     * @param dictionary
     *            new instance of the dictionary
     */
    void setDictionary(CurrentDictionary dictionary);
}
