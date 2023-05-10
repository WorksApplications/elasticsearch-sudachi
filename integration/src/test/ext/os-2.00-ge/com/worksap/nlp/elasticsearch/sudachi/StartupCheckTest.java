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

package com.worksap.nlp.elasticsearch.sudachi;

import com.worksap.nlp.elasticsearch.sudachi.utils.FileUtils;
import org.junit.Test;
import org.opensearch.common.settings.Settings;
import org.opensearch.plugins.AnalysisPlugin;
import org.opensearch.plugins.PluginsService;
import org.opensearch.test.OpenSearchTestCase;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class StartupCheckTest extends OpenSearchTestCase {
    private Path rootPath = null;
    private Path configPath = null;
    private Path pluginDir = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        String bundle = System.getProperty("sudachi.es.bundle");
        Path bundlePath = Path.of(bundle);

        rootPath = Files.createTempDirectory("sudachi-es-integration");
        pluginDir = rootPath.resolve("plugins");
        FileUtils.copyRecursively(bundlePath, pluginDir.resolve("analysis-sudachi"));

        configPath = rootPath.resolve("etc");
        Files.createDirectory(configPath);
    }

    @Override
    protected void afterIfFailed(List<Throwable> errors) {
        super.afterIfFailed(errors);
        logger.warn("sudachi environment: " + rootPath);
    }

    @Override
    protected void afterIfSuccessful() throws Exception {
        super.afterIfSuccessful();
        FileUtils.deleteRecursively(rootPath);
    }

    @Test
    public void checkStarts() {
        PluginsService plugins = new PluginsService(Settings.EMPTY, configPath, null, pluginDir,
                Collections.emptyList());
        List<AnalysisPlugin> analysisPlugins = plugins.filterPlugins(AnalysisPlugin.class);
        assertEquals(1, analysisPlugins.size());
    }

}
