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

import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class ReusableReaderVarHandleExtractor implements InputExtractor {
    private final static Class<? extends Reader> TARGET_CLASS;
    private final static VarHandle S_FIELD;
    public final static ReusableReaderVarHandleExtractor INSTANCE = new ReusableReaderVarHandleExtractor();

    static {
        try {
            Class<?> reusableReader = Class.forName("org.apache.lucene.analysis.ReusableStringReader");
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(reusableReader, MethodHandles.lookup());
            S_FIELD = lookup.findVarHandle(reusableReader, "s", String.class);
            TARGET_CLASS = reusableReader.asSubclass(Reader.class);
        } catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @NotNull
    @Override
    public ExtractionResult extract(@NotNull Reader r) {
        if (TARGET_CLASS.isInstance(r)) {
            String data = (String) S_FIELD.get(r);
            return new ExtractionResult(data, false);
        }
        return ExtractionResult.EMPTY;
    }

    @Override
    public boolean canExtract(@NotNull Reader input) {
        return TARGET_CLASS.isInstance(input);
    }

    private ReusableReaderVarHandleExtractor() {
    }
}
