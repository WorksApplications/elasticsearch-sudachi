package com.worksap.nlp.tools

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Adds version-specific ext source directories to main/test source sets.
 */
class EsSourcesPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        var kind = EsPluginSupport.requireProjectKind(project)
        var verString = kind.versionString
        var support = kind.supportVersion()
        var tags = support.getTags()
        logger.warn("Compatibility for version $verString is $support, using additional directories src/{test,main}/ext/{${tags.join(",")}}")

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
