package com.worksap.nlp.elasticsearch.sudachi;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.PluginsService;
import org.elasticsearch.test.ESTestCase;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class StartupCheckTest extends ESTestCase {
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
        PluginsService plugins = new PluginsService(Settings.EMPTY, configPath, null, pluginDir, Collections.emptyList());
        List<AnalysisPlugin> analysisPlugins = plugins.filterPlugins(AnalysisPlugin.class);
        assertEquals(1, analysisPlugins.size());
    }
}
