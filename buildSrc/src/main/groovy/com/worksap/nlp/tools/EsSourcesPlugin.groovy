package com.worksap.nlp.tools

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class EsSourcesPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        var kind = EsPluginSupport.requireProjectKind(project)

        var verString = kind.versionString
        var version = kind.supportVersion()
        var parsed = kind.parsedVersion()
        var allTags = kind.engine == EngineType.OpenSearch ? kind.engine.allTags(parsed) : kind.engine.allTags()

        var tags = allTags.collectMany { v ->
            var comparison = v <=> version
            if (comparison < 0) {
                return List.of("${v.tag}-gt", "${v.tag}-ge")
            } else if (comparison > 0) {
                return List.of("${v.tag}-lt", "${v.tag}-le")
            } else {
                return List.of(v.tag, "${v.tag}-le", "${v.tag}-ge")
            }
        }

        logger.warn("Compatibility for version $verString is $version, using additional directories src/{test,main}/ext/{${tags.join(",")}}")

        project.sourceSets {
            if (project.plugins.hasPlugin('org.jetbrains.kotlin.jvm')) {
                main.kotlin.srcDirs += tags.collect {"src/main/ext/$it" }
                test.kotlin.srcDirs += tags.collect {"src/test/ext/$it" }
            }

            main.java.srcDirs += tags.collect {"src/main/ext/$it" }
            test.java.srcDirs += tags.collect {"src/test/ext/$it" }
        }
    }

    static final Logger logger = LoggerFactory.getLogger(EsSourcesPlugin.class)
}
