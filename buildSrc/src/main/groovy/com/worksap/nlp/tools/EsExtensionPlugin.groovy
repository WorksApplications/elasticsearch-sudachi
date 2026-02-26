package com.worksap.nlp.tools

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

import javax.inject.Inject

class EsExtension {
    Provider<ProjectKind> kind

    @Inject
    EsExtension(ProviderFactory providers) {
        var engineVersion = providers.gradleProperty("engineVersion")
        this.kind = engineVersion.map { new ProjectKind(it) }
    }

    boolean hasPluginSpiSupport() {
        var k = kind.get()
        if (k.engine == EngineType.ElasticSearch) {
            var version = k.parsedVersion()
            return version.ge(8, 0)
        } else {
            return false
        }
    }

    boolean isEs() {
        return kind.get().engine == EngineType.ElasticSearch
    }

    String engineKind() {
        return kind.get().engine.getKind()
    }

    String engineVersion() {
        return kind.get().versionString
    }

    /**
     * Returns the required JVM version for the current engine.
     * OpenSearch 3.0+ requires JVM 21, others use JVM 17.
     */
    int jvmVersion() {
        var k = kind.get()
        if (k.engine == EngineType.OpenSearch) {
            var version = k.parsedVersion()
            if (version.ge(3, 0)) {
                return 21
            }
        }
        return 17
    }
}

class EsExtensionPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        EsPluginSupport.requireProjectKind(project)
        project.extensions.add("sudachiEs", EsExtension.class)
    }
}
