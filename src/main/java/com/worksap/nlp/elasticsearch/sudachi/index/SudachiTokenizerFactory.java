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

package com.worksap.nlp.elasticsearch.sudachi.index;

import java.io.IOException;

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;

import com.worksap.nlp.lucene.sudachi.ja.SudachiTokenizer;
import com.worksap.nlp.sudachi.Tokenizer.SplitMode;

public class SudachiTokenizerFactory extends AbstractTokenizerFactory {
    private static final String SPLIT_MODE_PARAM = "split_mode";
    private static final String MODE_PARAM = "mode";
    private static final String ADDITIONAL_SETTINGS_PARAM = "additional_settings";

    private final SplitMode mode;
    private final boolean discardPunctuation;
    private final String resourcesPath;
    private final String settingsJSON;
    private final boolean mergeSettings;

    public SudachiTokenizerFactory(IndexSettings indexSettings,
            Environment env, String name, Settings settings) throws IOException {
        super(indexSettings, settings);
        mode = getMode(settings);
        discardPunctuation = settings.getAsBoolean("discard_punctuation", true);
        resourcesPath = getResourcesPath(env, settings);

        String[] settingsStrings = getSettingsJSON(env, settings);
        settingsJSON = settingsStrings[0];
        mergeSettings = settingsStrings[1].equals("true");
    }

    public static SplitMode getMode(Settings settings) {
        SplitMode mode = SudachiTokenizer.DEFAULT_MODE;
        String modeSetting = settings.get(SPLIT_MODE_PARAM, null);
        if (modeSetting != null) {
            if ("a".equalsIgnoreCase(modeSetting)) {
                mode = SplitMode.A;
            } else if ("b".equalsIgnoreCase(modeSetting)) {
                mode = SplitMode.B;
            } else if ("c".equalsIgnoreCase(modeSetting)) {
                mode = SplitMode.C;
            }
        }

        if (settings.hasValue(MODE_PARAM)) {
            throw new IllegalArgumentException(MODE_PARAM + " is deprecated, use SudachiSplitFilter");
        }

        return mode;
    }

    public static String getResourcesPath(Environment env, Settings settings) {
        return new SudachiPathResolver(env.configFile().toString(), settings.get("resources_path", "sudachi")).resolvePathForDirectory();
    }

    public static String[] getSettingsJSON(Environment env, Settings settings) {
        String[] ret = new String[2];
        if (settings.hasValue(ADDITIONAL_SETTINGS_PARAM)) {
            ret[0] = settings.get(ADDITIONAL_SETTINGS_PARAM);
            ret[1] = "true";
        } else {
            ret[0] = new SudachiSettingsReader(env.configFile().toString(), settings.get("settings_path")).read();
            ret[1] = "false";
        }
        return ret;
    }

    @Override
    public Tokenizer create() {
        SudachiTokenizer t = null;
        try {
            t = new SudachiTokenizer(discardPunctuation, mode, resourcesPath, settingsJSON, mergeSettings);
        } catch (IOException e) {
            throw new ElasticsearchException("fail to make SudachiTokenizer", e);
        }
        return t;
    }

}
