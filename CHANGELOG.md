# Change log

## [Unreleased](https://github.com/WorksApplications/elasticsearch-sudachi/releases/)

### Added

- Support OpenSearch 3.5.0

## [3.5.0](https://github.com/WorksApplications/elasticsearch-sudachi/releases/tag/v3.5.0) - 2026-04-06

### Added

- Add multi-engine code management policy documents for branch-based ES/OS version management ([#183](https://github.com/WorksApplications/elasticsearch-sudachi/pull/183))

### Changed

- Update Gradle and build tooling, including Gradle 8.14, Kotlin 2.3.0, Spotless 8.1.0, Kover 0.9.7, SonarQube 7.2.2, and Nexus Publish 2.0.0, and switch packaging and coverage aggregation to subproject-aware tasks ([#180](https://github.com/WorksApplications/elasticsearch-sudachi/pull/180))
- Reorganize codebase to prepare for branch-based management:
  - Separate Lucene and search-engine sources under `lucene/` and `search/` ([#179](https://github.com/WorksApplications/elasticsearch-sudachi/pull/179))
  - Separate es-sudachi plugins into version-parsing/dependency management/source code management ([#181](https://github.com/WorksApplications/elasticsearch-sudachi/pull/179))
  - Merge search-engine-specific plugin code previously managed in `ext` ([#184](https://github.com/WorksApplications/elasticsearch-sudachi/pull/184), [#185](https://github.com/WorksApplications/elasticsearch-sudachi/pull/185), [#186](https://github.com/WorksApplications/elasticsearch-sudachi/pull/186))
  - Replace the release workflow with branch support matrix based CI, release orchestration, release-by-series workflows, and automated backport handling for engine-specific branches ([#183](https://github.com/WorksApplications/elasticsearch-sudachi/pull/183), [#188](https://github.com/WorksApplications/elasticsearch-sudachi/pull/188))

## [3.4.0](https://github.com/WorksApplications/elasticsearch-sudachi/releases/tag/v3.4.0) - 2026-01-27

### Added

- Support OpenSearch 2.18.0
- Support OpenSearch 2.19.\* ([#170](https://github.com/WorksApplications/elasticsearch-sudachi/pull/170))
- Support Elasticsearch 8.16.6 and 8.17.10 ([#172](https://github.com/WorksApplications/elasticsearch-sudachi/pull/172))
- Support OpenSearch 3 ([#176](https://github.com/WorksApplications/elasticsearch-sudachi/pull/176))

### Changed

- Update Java version from 11 to 17 ([#172](https://github.com/WorksApplications/elasticsearch-sudachi/pull/172))

### Removed

- Elasticsearch 7.x and versions earlier than 8.10 will no longer be supported. ([#172](https://github.com/WorksApplications/elasticsearch-sudachi/pull/172))

## [3.3.0](https://github.com/WorksApplications/elasticsearch-sudachi/releases/tag/v3.3.0) - 2024-11-13

### Added

- `allow_empty_morpheme` is added to the `sudachi_tokenizer` settings (#151)
  - This allows morphemes to have an empty span (bool, default `false`)

### Changed

- spi changed to implement #149
  - New methods are added to `MorphemeAttribute`

### Fixed

- Offset correction of `SudachiSplitFilter` now works properly with char filters (#149)

## [3.2.3] - 2024-10-16

### Added

- Sopport latest elasticsearch / opensearch (#144)
  - es: 8.14.3, 8.15.2, 7.17.24
  - os: 2.15.0, 2.16.0, 2.17.1

## [3.2.2] - 2024-07-02

### Fixed

- Use `lazyTokenizeSentences` for the analysis (#137)
  - This fixes the problem of input chunking (#131).

## [3.2.1] - 2024-06-14

### Fixed

- Fix OOM error with a huge document (#132)
  - Plugin now handles huge documents splitting into relatively small (1M char) chunks.
  - Analysis may be broken around the edge of chunks (open issue, see #131)

### Added

- Add tutorial to use [Sudachi synonym dictionary](https://github.com/WorksApplications/SudachiDict/blob/develop/docs/synonyms.md) (#65)

## [3.2.0] - 2024-05-30

### Added

- Update documents including tutrial (#125, #126)

### Fixed

- Explain with morpheme attribute (#121)
- Synonym filter and Sudachi filters can be used in any order (#122)
- Update deprecated codes (#125)

### Removed

- MorphemeConsumerAttribute is removed (#127)
  - This changes the interface of SPI. You can just remove MorphemeConsumerAttribute related code to migrate.
  - Also see #123 and #124.

## [3.1.1] - 2024-05-17

### Added

- Support ElasticSearch -8.13.4 and OpenSearch -2.14.0. (#114, #118)
  - Integration tests (`:integration`) for es:8.9.0+ are moved to Github Actions.

### Fixed

- Fix dictionary caching problem (#112)

## [3.1.0]

- support OpenSearch 2.6.0+ in addition to ElasticSearch
- analysis-sudachi plugin is now can be extended by other plugins. Loading sudachi plugins from extending plugins is supported as well

## [3.0.0]

- Plugin is now implemented in Kotlin

## [2.1.0]

- Added a new property `additional_settings` to write Sudachi settings directly in config
- Added support for specifying Elasticsearch version at build time

## [2.0.3]

- Fix duplicated tokens for OOVs with `sudachi_split` filter's `extended mode`

## [2.0.2]

- Upgrade Sudachi to 0.4.3
  - Fix overrun with surrogate pairs

## [2.0.1]

- Upgrade Sudachi to 0.4.2
  - Fix buffer overrun with character normalization

## [2.0.0]

- New mode `split_mode` was added
- New filter `sudachi_split` was added instead of `mode`
- `mode` was deperecated
- Upgrade Sudachi morphological analyzer to 0.4.1
- Words containing periods are no longer split
- Fix a bug causing wrong offsets with `icu_normalizer`

## [1.3.2]

- Upgrade Sudachi morphological analyzer to 0.3.1

## [1.3.1]

- Upgrade Sudachi morphological analyzer to 0.3.0
- Minor bug fix

## [1.3.0]

- Upgrade Sudachi morphological analyzer to 0.2.0
- Import Sudachi from maven central repository
- Minor bug fix

## [1.2.0]

- Upgrading Sudachi morphological analyzer to 0.2.0-SNAPSHOT
- New filter `sudachi_normalizedform` was added; see [sudachi_normalizedform](#sudachi_normalizedform)
- Default normalization behavior was changed; neather baseform filter and normalziedform filter not applied
- `sudachi_readingform` filter was changed with new romaji mappings based on MS-IME

## [1.1.0]

- `part-of-speech forward matching` is available on `stoptags`; see [sudachi_part_of_speech](#sudachi_part_of_speech)

## [1.0.0]

- first release
