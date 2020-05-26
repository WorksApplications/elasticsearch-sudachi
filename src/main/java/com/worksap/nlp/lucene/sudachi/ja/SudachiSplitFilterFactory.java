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

package com.worksap.nlp.lucene.sudachi.ja;

import java.util.Locale;
import java.util.Map;

import com.worksap.nlp.lucene.sudachi.ja.SudachiSplitFilter.Mode;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

public class SudachiSplitFilterFactory extends TokenFilterFactory {
    private static final String MODE_PARAM = "mode";
    private final Mode mode;
    
    public SudachiSplitFilterFactory(Map<String, String> args) {
        super(args);
        mode = Mode.valueOf(get(args, MODE_PARAM, SudachiSplitFilter.DEFAULT_MODE.toString()).toUpperCase(Locale.ROOT));
        if (!args.isEmpty()) {
            throw new IllegalArgumentException("Unknown parameters: " + args);
        }
    }

    @Override
    public TokenStream create(TokenStream input) {
        return new SudachiSplitFilter(input, mode);
    }
}