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

import org.apache.lucene.util.Attribute;

/**
 * This attribute tells Sudachi-based TokenStreams not to produce anything into
 * {@link org.apache.lucene.analysis.tokenattributes.CharTermAttribute} if it is
 * not the current consumer. <br>
 * This is performance optimisation and will not change correctness if resetting
 * {@code CharTermAttribute} before writing into it.
 */
public interface MorphemeConsumerAttribute extends Attribute {
    /**
     * Check whether the object should consume the token stream.
     * 
     * @param consumer
     *            object that will try to consume the token stream
     * @return true if the object is current consumer
     */
    default boolean shouldConsume(Object consumer) {
        return consumer == getCurrentConsumer();
    }

    /**
     * Get the current consumer
     * 
     * @return instance that is current consumer
     */
    Object getCurrentConsumer();

    /**
     * Set the current consumer for the token stream
     * 
     * @param consumer
     *            new consumer instance
     */
    void setCurrentConsumer(Object consumer);
}
