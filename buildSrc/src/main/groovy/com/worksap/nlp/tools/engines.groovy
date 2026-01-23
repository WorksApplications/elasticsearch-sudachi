package com.worksap.nlp.tools

trait EngineSupport {
    abstract String getTag()
}

enum EsSupport implements EngineSupport {
    Es810("es-8.10"),
    Es812("es-8.12"),
    Es816("es-8.16")

    String tag
    List<String> keys

    @Override
    String getTag() { return tag }

    EsSupport(String tag, String... keys) {
        this.tag = tag
        this.keys = Arrays.asList(keys)
    }

    static EsSupport supportVersion(Version vers) {
        if (vers.lt(8, 10)) {
            throw new IllegalArgumentException("versions below 8.10 are not supported")
        } else if (vers.ge(8, 10) && vers.lt(8, 12)) {
            return Es810
        } else if (vers.ge(8, 12) && vers.lt(8, 16)) {
            return Es812
        } else if (vers.ge(8, 16) && vers.lt(9, 0)) {
            return Es816
        } else {
            throw new IllegalArgumentException("unsupported ElasticSearch version: " + vers.raw)
        }
    }
}

enum OsSupport implements EngineSupport {
    Os20("os-2.00"),
    Os27("os-2.07"),
    Os210("os-2.10"),
    Os30("os-3.00")

    String tag

    @Override
    String getTag() { return tag }

    OsSupport(String tag) {
        this.tag = tag
    }


    static OsSupport supportVersion(Version version) {
        if (version.ge(2, 0) && version.lt(2, 7)) {
            return Os20
        } else if (version.ge(2, 7) && version.lt(2, 10)) {
            return Os27
        } else if (version.ge(2, 10) && version.lt(3, 0)) {
            return Os210
        } else if (version.ge(3, 0)) {
            return Os30
        }
        throw new Exception("unsupported version")
    }
}

class Version {
    int major
    int minor
    String raw

    Version(int major, int minor, String raw) {
        this.major = major
        this.minor = minor
        this.raw = raw
    }

    boolean ge(int major, int minor) {
        if (major == this.major) {
            return this.minor >= minor
        }
        return this.major >= major
    }

    boolean lt(int major, int minor) {
        if (major == this.major) {
            return this.minor < minor
        }
        return this.major < major
    }

    static Version fromRaw(String version) {
        var fields = version.split("\\.")
        var major = fields[0].toInteger()
        var minor = fields[1].toInteger()
        return new Version(major, minor, version)
    }
}

enum EngineType {
    ElasticSearch{
        List<EngineSupport> allTags() {
            return List.of(EsSupport.values())
        }

        EngineSupport supportVersion(Version version) {
            return EsSupport.supportVersion(version)
        }

        String getKind() { return "elasticsearch" }
    },
    OpenSearch{
        /**
         * Return the set of compatibility tags we want to include in the build.
         *
         * IMPORTANT:
         * Kotlin top-level functions under src/main/ext/** may share the same package and signatures.
         * If we include multiple major-series directories together (e.g. os-2.* and os-3.*),
         * it can cause "Conflicting overloads" compilation errors.
         */
        List<EngineSupport> allTags() {
            return List.of(OsSupport.values())
        }

        /**
         * Version-aware variant used by the plugin when configuring sourceSets.
         *
         * OpenSearch 3.0+ has breaking API differences from 2.x (e.g. Environment#configDir vs configFile),
         * so we must not compile os-2.* sources together with os-3.* sources.
         */
        List<EngineSupport> allTags(Version targetVersion) {
            if (targetVersion != null && targetVersion.ge(3, 0)) {
                return List.of(OsSupport.Os30)
            }
            return List.of(OsSupport.Os20, OsSupport.Os27, OsSupport.Os210)
        }

        EngineSupport supportVersion(Version version) {
            return OsSupport.supportVersion(version)
        }

        String getKind() { return "opensearch" }
    }

    abstract List<EngineSupport> allTags();

    abstract EngineSupport supportVersion(Version version);

    abstract String getKind()
}

class ProjectKind {
    EngineType engine
    String version

    ProjectKind(String rawVersion) {
        var parts = rawVersion.split(":", 2)
        if (parts.size() != 2) {
            throw new IllegalArgumentException("raw version string should be like es:8.6.0, was $rawVersion")
        }

        var kind = parts[0]
        var version = parts[1]

        switch (kind) {
            case "es":
                this.engine = EngineType.ElasticSearch
                break
            case "os":
                this.engine = EngineType.OpenSearch
                break
            default:
                throw new IllegalArgumentException("unknown engine kind $kind")
        }
        this.version = version
    }

    EngineSupport supportVersion(String rawVersion) {
        Version version = Version.fromRaw(rawVersion)
        return engine.supportVersion(version)
    }

    Version parsedVersion() {
        return Version.fromRaw(version)
    }
}
