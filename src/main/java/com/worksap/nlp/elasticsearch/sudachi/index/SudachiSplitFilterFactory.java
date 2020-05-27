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

package com.worksap.nlp.elasticsearch.sudachi.index;

import java.util.Locale;

import com.worksap.nlp.lucene.sudachi.ja.SudachiSplitFilter;
import com.worksap.nlp.lucene.sudachi.ja.SudachiSplitFilter.Mode;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;

public class SudachiSplitFilterFactory extends AbstractTokenFilterFactory {
   
    private static final String MODE_PARAM = "mode";

    private final Mode mode;

    public SudachiSplitFilterFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        super(indexSettings, name, settings);
        mode = Mode.valueOf(settings.get(MODE_PARAM, SudachiSplitFilter.DEFAULT_MODE.toString()).toUpperCase(Locale.ROOT));
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new SudachiSplitFilter(tokenStream, mode);
    }
}