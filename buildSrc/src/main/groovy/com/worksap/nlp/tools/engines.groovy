package com.worksap.nlp.tools

trait EngineSupport {
    abstract String getTag()
}

enum EsSupport implements EngineSupport {
    Es74("es-7.04"),
    Es78("es-7.08"),
    Es715("es-7.15"),
    Es80("es-8.00"),
    Es83("es-8.30"),
    Es84("es-8.40")

    String tag
    List<String> keys

    EsSupport(String tag, String... keys) {
        this.tag = tag
        this.keys = Arrays.asList(keys)
    }

    static EsSupport supportVersion(Version vers) {
        if (vers.lt(7, 4)) {
            throw new IllegalArgumentException("versions below 7.4 are not supported")
        } else if (vers.ge(7, 4) && vers.lt(7, 8)) {
            return Es74
        } else if (vers.ge(7, 8) && vers.lt(7, 15)) {
            return Es78
        } else if (vers.ge(7, 15) && vers.lt(8, 0)) {
            return Es715
        } else if (vers.ge(8, 0) && vers.lt(8, 3)) {
            return Es80
        } else if (vers.ge(8, 3) && vers.lt(8, 4)) {
            return Es83
        } else if (vers.ge(8, 4) && vers.lt(9, 0)) {
            return Es84
        } else {
            throw new IllegalArgumentException("unsupported ElasticSearch version: " + vers.raw)
        }
    }
}

enum OsSupport implements EngineSupport {
    Os20("os-2.00"),
    Os210("os-2.10")

    String tag

    OsSupport(String tag) {
        this.tag = tag
    }


    static OsSupport supportVersion(Version version) {
        if (version.ge(2, 0) && version.lt(2, 10)) {
            return Os20
        } else if (version.ge(2, 10)) {
            return Os210
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
        List<EngineSupport> allTags() {
            return List.of(OsSupport.values())
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