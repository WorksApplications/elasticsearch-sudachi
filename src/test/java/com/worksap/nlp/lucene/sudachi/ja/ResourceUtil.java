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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ResourceUtil {
    static final String RESOURCE_NAME_SYSTEM_DIC = "system_core.dic";

    private ResourceUtil() {
    }

    public static String getSudachiSetting(InputStream is) throws IOException {
        String settings;
        try (InputStreamReader ir = new InputStreamReader(is, StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(ir)) {
            String sudachiSettingsLine = "";
            StringBuilder sb = new StringBuilder();
            while ((sudachiSettingsLine = br.readLine()) != null) {
                sb.append(sudachiSettingsLine);
            }
            settings = sb.toString();
        }
        return settings;
    }

    public static void copy(File destDir) throws IOException {
        copyResource(RESOURCE_NAME_SYSTEM_DIC, destDir, false);
    }

    public static void copyResource(String filename, File destDir, boolean fromRoot) throws IOException {
        String src = (fromRoot) ? "/" + filename : filename;
        try (InputStream stream = ResourceUtil.class.getResourceAsStream(src)) {
            Files.copy(stream, destDir.toPath().resolve(filename));
        }
    }

    public static URL resource(String name) {
        return ResourceUtil.class.getResource(name);
    }
}
