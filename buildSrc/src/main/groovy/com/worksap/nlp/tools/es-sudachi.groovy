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

    boolean isEs() {
        return kind.get().engine == EngineType.ElasticSearch
    }

    String version() {
        return kind.get().version
    }

    /**
     * Returns the required JVM version for the current engine.
     * OpenSearch 3.0+ requires JVM 21, others use JVM 17.
     */
    int jvmVersion() {
        def k = kind.get()
        if (k.engine == EngineType.OpenSearch) {
            def ver = k.parsedVersion()
            if (ver.ge(3, 0)) {
                return 21
            }
        }
        return 17
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

        def parsed = kind.parsedVersion()
        def allTags = kind.engine == EngineType.OpenSearch ? kind.engine.allTags(parsed) : kind.engine.allTags()

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
                // OpenSearch 3.0+ requires the new Java agent security framework
                def parsedVersion = Version.fromRaw(verString)
                if (parsedVersion.ge(3, 0)) {
                    testImplementation("org.opensearch:opensearch-agent-bootstrap:$verString")
                    testRuntimeOnly("org.opensearch:opensearch-agent:$verString")
                }
            }
        }
    }

    static final Logger logger = LoggerFactory.getLogger(EsSudachiPlugin.class)
}