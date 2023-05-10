package com.worksap.nlp.tools

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.inject.Inject

class EsExtension {
    Provider<ProjectKind> kind

    @Inject
    EsExtension(ProviderFactory providers) {
        var engineVersion = providers.gradleProperty("engineVersion")
        this.kind = engineVersion.map {new ProjectKind(it) }
    }

    boolean hasPluginSpiSupport() {
        def kind = kind.get()
        if (kind.engine == EngineType.ElasticSearch) {
            def ver = kind.parsedVersion()
            return ver.ge(8, 0)
        } else {
            return false
        }
    }
}


class EsSudachiPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        var rawVersion = project.property("engineVersion")
        if (rawVersion == null || !(rawVersion instanceof String)) {
            throw new IllegalArgumentException("elasticVersion property is not defined")
        }

        project.extensions.add("sudachiEs", EsExtension.class)


        var kind = new ProjectKind(rawVersion as String)
        var verString = kind.version


        var version = kind.supportVersion(verString)

        var tags = kind.engine.allTags().collectMany { v ->
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


        project.dependencies {
            if (kind.engine == EngineType.ElasticSearch) {
                compileOnly("org.elasticsearch:elasticsearch:$verString")
                testImplementation("org.elasticsearch:elasticsearch:$verString")
                testImplementation("org.elasticsearch.test:framework:$verString") {
                    exclude(group: 'junit', module: 'junit')
                }
            } else {
                compileOnly("org.opensearch:opensearch:$verString")
                testImplementation("org.opensearch:opensearch:$verString")
                testImplementation("org.opensearch.test:framework:$verString") {
                    exclude(group: 'junit', module: 'junit')
                }
                testImplementation("org.opensearch:opensearch-plugin-classloader:$verString")
            }
        }
    }

    static final Logger logger = LoggerFactory.getLogger(EsSudachiPlugin.class)
}