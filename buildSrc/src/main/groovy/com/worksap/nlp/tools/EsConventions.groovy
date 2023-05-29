package com.worksap.nlp.tools

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

class EsConventions implements Plugin<Project> {
    @Override
    void apply(Project target) {
        target.tasks.withType(JavaCompile).configureEach {
            options.release.set(11)
            options.encoding = 'UTF-8'
        }


        target.repositories {
            mavenLocal()
            mavenCentral()
            if (target.version.endsWith("-SNAPSHOT")) {
                maven {
                    url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
                }
            }
        }
    }
}
