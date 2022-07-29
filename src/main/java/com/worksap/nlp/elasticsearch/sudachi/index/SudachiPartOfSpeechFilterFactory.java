/*
 * Copyright (c) 2017-2022 Works Applications Co., Ltd.
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

import com.worksap.nlp.elasticsearch.sudachi.plugin.ReloadAware;
import com.worksap.nlp.elasticsearch.sudachi.plugin.ReloadableDictionary;
import com.worksap.nlp.lucene.sudachi.ja.SudachiPartOfSpeechStopFilter;
import com.worksap.nlp.lucene.sudachi.ja.attributes.SudachiAttribute;
import com.worksap.nlp.lucene.sudachi.ja.util.Stoptags;
import com.worksap.nlp.sudachi.PartialPOS;
import com.worksap.nlp.sudachi.PosMatcher;
import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.elasticsearch.index.analysis.Analysis;

import java.util.ArrayList;
import java.util.List;

public class SudachiPartOfSpeechFilterFactory extends AbstractTokenFilterFactory {

    private final List<PartialPOS> stopTags = new ArrayList<>();

    public SudachiPartOfSpeechFilterFactory(IndexSettings indexSettings, Environment env, String name,
            Settings settings) {
        super(indexSettings, name, settings);
        List<String> tagList = Analysis.getWordList(env, settings, "stoptags");
        if (tagList != null) {
            for (String tag : tagList) {
                stopTags.add(Stoptags.parse(tag));
            }
        }
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        if (stopTags.isEmpty()) {
            return tokenStream;
        } else {
            SudachiAttribute sudachi = tokenStream.getAttribute(SudachiAttribute.class);
            if (sudachi == null) {
                throw new IllegalStateException("Sudachi-based tokenizer was not present in the filter chain");
            }
            ReloadableDictionary dic = sudachi.getDictionary();
            ReloadAware<PosMatcher> matcher = new ReloadAware<>(dic, d -> d.posMatcher(stopTags));
            return new SudachiPartOfSpeechStopFilter(tokenStream, matcher);
        }
    }

}
