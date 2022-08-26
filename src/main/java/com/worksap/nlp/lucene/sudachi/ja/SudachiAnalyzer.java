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

package com.worksap.nlp.lucene.sudachi.ja;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.worksap.nlp.elasticsearch.sudachi.plugin.ReloadAware;
import com.worksap.nlp.elasticsearch.sudachi.plugin.ReloadableDictionary;
import com.worksap.nlp.lucene.sudachi.ja.util.AnalysisCache;
import com.worksap.nlp.lucene.sudachi.ja.util.Stoptags;
import com.worksap.nlp.sudachi.PartialPOS;
import com.worksap.nlp.sudachi.PosMatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.StopFilter;

import com.worksap.nlp.sudachi.Tokenizer.SplitMode;
import org.apache.lucene.util.AttributeFactory;

/**
 * Analyzer which uses Sudachi as internal tokenizer. It also applies
 * BaseFormFilter and stop word/stop POS filtering.
 *
 * @see SudachiTokenizer
 */
public class SudachiAnalyzer extends StopwordAnalyzerBase {

    private static final Logger logger = LogManager.getLogger(SudachiAnalyzer.class);
    private final SplitMode mode;
    private final List<PartialPOS> stoptags;

    private final ReloadableDictionary dictionary;

    private final AnalysisCache cache;

    private final boolean discardPunctuation;

    public SudachiAnalyzer(ReloadableDictionary dictionary, AnalysisCache cache, boolean discardPunctuation,
            SplitMode mode, CharArraySet stopwords, List<PartialPOS> stoptags) {
        super(stopwords);
        this.mode = mode;
        this.stoptags = stoptags;
        this.dictionary = dictionary;
        this.cache = cache;
        this.discardPunctuation = discardPunctuation;
    }

    public static CharArraySet getDefaultStopSet() {
        return Defaults.STOP_WORDS;
    }

    public static List<PartialPOS> getDefaultStopTags() {
        return Defaults.STOP_TAGS;
    }

    private static class Defaults {
        static final CharArraySet STOP_WORDS;
        static final List<PartialPOS> STOP_TAGS;

        static {
            try {
                STOP_WORDS = loadStopwordSet(true, SudachiAnalyzer.class, "stopwords.txt", "#");
                final CharArraySet tagset = loadStopwordSet(false, SudachiAnalyzer.class, "stoptags.txt", "#");
                List<PartialPOS> tags = new ArrayList<>();
                for (Object element : tagset) {
                    char[] chars = (char[]) element;
                    tags.add(Stoptags.parse(new String(chars)));
                }
                STOP_TAGS = Collections.unmodifiableList(tags);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        private Defaults() {
        }
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        logger.debug("creating Sudachi token stream with mode={} for field={}", mode, fieldName);
        CachingTokenizer it = new CachingTokenizer(dictionary.newTokenizer(), mode, cache);
        Tokenizer tokenizer = new SudachiTokenizer(it, discardPunctuation, AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY);
        TokenStream stream = tokenizer;
        stream = new SudachiBaseFormFilter(stream);
        if (!stoptags.isEmpty()) {
            ReloadAware<PosMatcher> matcher = new ReloadAware<>(dictionary, dic -> dic.posMatcher(stoptags));
            stream = new SudachiPartOfSpeechStopFilter(stream, matcher);
        }
        stream = new StopFilter(stream, stopwords);
        return new TokenStreamComponents(tokenizer, stream);
    }
}
