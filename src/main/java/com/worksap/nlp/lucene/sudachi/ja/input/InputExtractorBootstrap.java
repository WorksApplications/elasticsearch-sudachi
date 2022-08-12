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

package com.worksap.nlp.lucene.sudachi.ja.input;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class InputExtractorBootstrap {
    private final static Logger logger = LogManager.getLogger(InputExtractorBootstrap.class);

    @NotNull
    public static final InputExtractor ZERO_COPY;

    static {
        InputExtractor zeroCopy;
        try {
            zeroCopy = ReusableReaderVarHandleExtractor.INSTANCE;
            logger.debug("successful instantiation of VarHandle-based input extractor");
        } catch (UnsupportedOperationException | ExceptionInInitializerError e) {
            logger.debug("failed to instantiate VarHandle-based input extractor", e);
            zeroCopy = null;
        }

        ZERO_COPY = Objects.requireNonNullElse(zeroCopy, NoopInputExtractor.INSTANCE);
    }
}
