package com.worksap.nlp.tools

import org.gradle.api.Plugin
import org.gradle.api.Project

enum EsSupport {
    Es56("es-5.06"),
    Es68("es-6.08"),
    Es70("es-7.00"),
    Es74("es-7.04"),
    Es710("es-7.10"),
    Es80("es-8.00")

    List<String> keys

    EsSupport(String... keys) {
        this.keys = Arrays.asList(keys)
    }
}

class Version {
    Integer major
    Integer minor

    Version(Integer major, Integer minor) {
        this.major = major
        this.minor = minor
    }

    boolean ge(Integer major, Integer minor) {
        if (major == this.major) {
            return this.minor >= minor
        }
        return this.major >= major
    }

    boolean lt(Integer major, Integer minor) {
        if (major == this.major) {
            return this.minor < minor
        }
        return this.major < major
    }

    static EsSupport supportVersion(String version) {
        var fields = version.split("\\.")
        var major = fields[0].toInteger()
        var minor = fields[1].toInteger()
        var vers = new Version(major, minor)

        if (vers.ge(5, 6) && vers.lt(6, 8)) {
            return EsSupport.Es56
        } else if (vers.ge(6, 8) && vers.lt(7, 0)) {
            return EsSupport.Es68
        } else if (vers.ge(7, 0) && vers.lt(7, 4)) {
            return EsSupport.Es70
        } else if (vers.ge(7, 4) && vers.lt(7, 10)) {
            return EsSupport.Es74
        } else if (vers.ge(7, 10) && vers.lt(8, 0)) {
            return EsSupport.Es710
        } else if (vers.ge(8, 0)) {
            return EsSupport.Es80
        } else {
            throw new IllegalArgumentException("unsupported ElasticSearch version: " + version)
        }
    }
}



class EsSudachiPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        var version = project.properties.get("elasticsearchVersion")
        if (version == null || !(version instanceof String)) {
            throw new IllegalArgumentException("elasticVersion property is not defined")
        }

        var versRange = Version.supportVersion(version)
        println("ES support kind for version $version is $versRange, using additional directories src/{test,main}/ext/{${versRange.keys.join(",")}}")

        project.sourceSets {
            main.kotlin.srcDirs += versRange.keys.collect {"src/main/ext/$it" }
            test.kotlin.srcDirs += versRange.keys.collect {"src/test/ext/$it" }
            main.java.srcDirs += versRange.keys.collect {"src/main/ext/$it" }
            test.java.srcDirs += versRange.keys.collect {"src/test/ext/$it" }
        }
    }
}