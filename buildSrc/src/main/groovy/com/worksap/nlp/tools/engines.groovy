package com.worksap.nlp.tools

trait EngineSupport {
    abstract String getMajorTag()
    abstract String getMinorTag()
    abstract List<String> getTags()
}

enum EsSupport implements EngineSupport {
    Es810("es-8", "es-8.10"),
    Es812("es-8", "es-8.12"),
    Es816("es-8", "es-8.16")

    String majorTag
    String minorTag

    @Override
    String getMajorTag() { return majorTag }

    @Override
    String getMinorTag() { return minorTag }

    @Override
    List<String> getTags() { return List.of(getMajorTag(), getMinorTag()) }

    EsSupport(String majorTag, String minorTag) {
        this.majorTag = majorTag
        this.minorTag = minorTag
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
    Os20("os-2", "os-2.00"),
    Os27("os-2", "os-2.07"),
    Os210("os-2", "os-2.10"),
    Os30("os-3", "os-3.00")

    String majorTag
    String minorTag

    @Override
    String getMajorTag() { return majorTag }

    @Override
    String getMinorTag() { return minorTag }

    @Override
    List<String> getTags() { return List.of(getMajorTag(), getMinorTag()) }

    OsSupport(String majorTag, String minorTag) {
        this.majorTag = majorTag
        this.minorTag = minorTag
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
        EngineSupport supportVersion(Version version) {
            return EsSupport.supportVersion(version)
        }

        String getKind() { return "elasticsearch" }
    },

    OpenSearch{
        EngineSupport supportVersion(Version version) {
            return OsSupport.supportVersion(version)
        }

        String getKind() { return "opensearch" }
    }

    abstract EngineSupport supportVersion(Version version);

    abstract String getKind()
}

class ProjectKind {
    EngineType engine
    String versionString

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
        this.versionString = version
    }

    EngineSupport supportVersion() {
        return engine.supportVersion(parsedVersion())
    }

    Version parsedVersion() {
        return Version.fromRaw(versionString)
    }
}
