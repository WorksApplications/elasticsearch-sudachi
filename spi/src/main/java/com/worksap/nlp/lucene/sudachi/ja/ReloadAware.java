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

import org.apache.lucene.analysis.TokenStream;

/**
 * Base interface for reloadable components. <br>
 *
 * Function {@link #maybeReload()} should be called periodically in safe places
 * (e.g. in {@link TokenStream#reset()}). Instances do not get reloaded
 * automatically because that could change something in the middle of analysis
 * and cause inconsistent results.
 *
 * @param <T>
 */
public interface ReloadAware<T> {

    /**
     * Update components if underlying state has changed
     * 
     * @return newly reloaded component if changed, old otherwise
     */
    default T maybeReload() {
        return maybeReload(null);
    }

    /**
     * Replace the current dictionary with the provided one
     * 
     * @param newDictionary
     *            new dictionary. Can be null, in this case resolve changes
     *            internally.
     * @return newly reloaded component if changed, old otherwise.
     */
    T maybeReload(CurrentDictionary newDictionary);

    /**
     * Get current version of the reloadable object. Do not update it.
     * 
     * @return current instance of the object.
     */
    T get();

    /**
     * Access the current dictionary object
     * 
     * @return current dictionary object
     */
    CurrentDictionary dictionary();
}
