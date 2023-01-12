package com.worksap.nlp.tools

import org.gradle.api.Plugin
import org.gradle.api.Project

enum EsSupport {
    Es74("es-7.04"),
    Es78("es-7.08"),
    Es80("es-8.00"),
    Es83("es-8.30"),
    Es84("es-8.40")

    String tag
    List<String> keys

    EsSupport(String tag, String... keys) {
        this.tag = tag
        this.keys = Arrays.asList(keys)
    }
}

class Version {
    int major
    int minor

    Version(int major, int minor) {
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

        if (vers.lt(7, 4)) {
            throw new IllegalArgumentException("versions below 7.4 are not supported")
        } else if (vers.ge(7, 4) && vers.lt(7, 8)) {
            return EsSupport.Es74
        } else if (vers.ge(7, 8) && vers.lt(8, 0)) {
            return EsSupport.Es78
        } else if (vers.ge(8, 0) && vers.lt(8, 3)) {
            return EsSupport.Es80
        } else if (vers.ge(8, 3) && vers.lt(8, 4)) {
            return EsSupport.Es83
        } else if (vers.ge(8, 4) && vers.lt(9, 0)) {
            return EsSupport.Es84
        } else {
            throw new IllegalArgumentException("unsupported ElasticSearch version: " + version)
        }
    }
}



class EsSudachiPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        var verString = project.properties.get("elasticsearchVersion")
        if (verString == null || !(verString instanceof String)) {
            throw new IllegalArgumentException("elasticVersion property is not defined")
        }

        var version = Version.supportVersion(verString)

        var tags = EsSupport.values().collectMany { v ->
            var comparison = v <=> version
            if (comparison < 0) {
                return List.of("${v.tag}-gt", "${v.tag}-ge")
            } else if (comparison > 0) {
                return List.of("${v.tag}-lt", "${v.tag}-le")
            } else {
                return List.of(v.tag, "${v.tag}-le", "${v.tag}-ge")
            }
        }

        tags.addAll(version.keys)

        println("ES support kind for version $verString is $version, using additional directories src/{test,main}/ext/{${tags.join(",")}}")

        project.sourceSets {
            main.kotlin.srcDirs += tags.collect {"src/main/ext/$it" }
            test.kotlin.srcDirs += tags.collect {"src/test/ext/$it" }
            main.java.srcDirs += tags.collect {"src/main/ext/$it" }
            test.java.srcDirs += tags.collect {"src/test/ext/$it" }
        }
    }
}