/*
 *  Copyright (c) 2017 Works Applications Co., Ltd.
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

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopFilter;

import com.worksap.nlp.sudachi.Tokenizer.SplitMode;

/**
 * Analyzer for Sudachi that uses morphological analysis.
 * 
 * @see SudachiTokenizer
 */
public class SudachiAnalyzer extends StopwordAnalyzerBase {
    private final SplitMode mode;
    private final String resourcesPath;
    private final String settings;
    private final boolean mergeSettings;
    private final PartOfSpeechTrie stoptags;

    public SudachiAnalyzer() {
        this(SudachiTokenizer.DEFAULT_MODE, "", null, false,
                DefaultSetHolder.DEFAULT_STOP_SET,
                DefaultSetHolder.DEFAULT_STOP_TAGS);
    }

    public SudachiAnalyzer(SplitMode mode, String resourcesPath, String settings, boolean mergeSettings,
            CharArraySet stopwords, PartOfSpeechTrie stoptags) {
        super(stopwords);
        this.mode = mode;
        this.resourcesPath = resourcesPath;
        this.settings = settings;
        this.mergeSettings = mergeSettings;
        this.stoptags = stoptags;
    }

    public static CharArraySet getDefaultStopSet() {
        return DefaultSetHolder.DEFAULT_STOP_SET;
    }

    public static PartOfSpeechTrie getDefaultStopTags() {
        return DefaultSetHolder.DEFAULT_STOP_TAGS;
    }

    private static class DefaultSetHolder {
        static final CharArraySet DEFAULT_STOP_SET;
        static final PartOfSpeechTrie DEFAULT_STOP_TAGS;

        static {
            try {
                DEFAULT_STOP_SET = loadStopwordSet(true, SudachiAnalyzer.class,
                        "stopwords.txt", "#");
                final CharArraySet tagset = loadStopwordSet(false,
                        SudachiAnalyzer.class, "stoptags.txt", "#");
                DEFAULT_STOP_TAGS = new PartOfSpeechTrie();
                for (Object element : tagset) {
                    char[] chars = (char[]) element;
                    DEFAULT_STOP_TAGS.add(new String(chars).split(","));
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        private DefaultSetHolder() {}
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = null;
        try {
            tokenizer = new SudachiTokenizer(true, mode, resourcesPath, settings, mergeSettings);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        TokenStream stream = new SudachiBaseFormFilter(tokenizer);
        stream = new SudachiPartOfSpeechStopFilter(stream, stoptags);
        stream = new StopFilter(stream, stopwords);
        return new TokenStreamComponents(tokenizer, stream);
    }
}
