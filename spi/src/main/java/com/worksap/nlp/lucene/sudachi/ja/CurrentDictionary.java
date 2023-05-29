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

package com.worksap.nlp.lucene.sudachi.ja;

import com.worksap.nlp.sudachi.Dictionary;

import java.util.function.Function;

/**
 * Sudachi {@link Dictionary} object wrapped in reload-aware facade.
 *
 * @see ReloadAware
 */
public interface CurrentDictionary extends ReloadAware<Dictionary> {
    /**
     * Create a new Tokenizer wrapped in reload-aware facade.
     * 
     * @return new Sudachi Tokenizer object
     */
    CurrentTokenizer newTokenizer();

    /**
     * Create a new reloadable facade for an object which stem from the dictionary.
     * Object will be automatically recreated if needed.
     *
     * @param function
     *            factory function for the object
     * @return created object wrapped in the facade
     * @param <T>
     *            object type
     */
    <T> ReloadAware<T> reloadable(Function<Dictionary, T> function);
}
