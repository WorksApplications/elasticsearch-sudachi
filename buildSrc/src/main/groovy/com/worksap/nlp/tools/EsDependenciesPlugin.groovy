package com.worksap.nlp.tools

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Adds compile/test dependencies for the target ElasticSearch/OpenSearch version.
 *
 * Note: The appropriate Lucene version is also provided transitively by these
 * ElasticSearch/OpenSearch dependencies.
 */
class EsDependenciesPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        var kind = EsPluginSupport.requireProjectKind(project)
        var verString = kind.versionString

        project.dependencies {
            if (kind.engine == EngineType.ElasticSearch) {
                compileOnly("org.elasticsearch:elasticsearch:$verString")
                testImplementation("org.elasticsearch:elasticsearch:$verString")
                testImplementation("org.elasticsearch.test:framework:$verString") {
                    exclude(group: 'junit', module: 'junit')
                }
            } else { // Opensearch
                compileOnly("org.opensearch:opensearch:$verString")
                testImplementation("org.opensearch:opensearch:$verString")
                testImplementation("org.opensearch.test:framework:$verString") {
                    exclude(group: 'junit', module: 'junit')
                }
                testImplementation("org.opensearch:opensearch-plugin-classloader:$verString")
                // OpenSearch 3.0+ requires the new Java agent security framework
                var parsedVersion = kind.parsedVersion()
                if (parsedVersion.ge(3, 0)) {
                    testImplementation("org.opensearch:opensearch-agent-bootstrap:$verString")
                    testRuntimeOnly("org.opensearch:opensearch-agent:$verString")
                }
            }
        }
    }
}
