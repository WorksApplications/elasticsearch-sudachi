package com.worksap.nlp.tools

import org.gradle.api.Project

/**
 * Validate and parse `engineVersion` property.
 */
class EsPluginSupport {
    static ProjectKind requireProjectKind(Project project) {
        var rawVersion = project.findProperty("engineVersion")
        if (rawVersion == null || !(rawVersion instanceof String)) {
            throw new IllegalArgumentException("elasticVersion property is not defined")
        }
        return new ProjectKind(rawVersion as String)
    }
}
