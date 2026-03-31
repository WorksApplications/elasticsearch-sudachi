# Policy for Managing Code Differences Across Search Engine Variants (ES/OS)

## Overview

Because the APIs differ across Elasticsearch / OpenSearch and their versions, we need to manage code differences for each target.
In this repository, those differences are managed by splitting them into separate branches.

This document defines the policy for managing those branches.

## Basic Principles

- The default branch is responsible for shared functionality, and branch-based divergence should be kept to a minimum.
- As a rule, maintain one branch for each API series (`engine + major version`).
  - If API differences arise within the same series, further branch splitting is also allowed.
- Changes should originate from the default branch and be cherry-picked/backported to the required series.

## Code Management Policy

### Current Branches

- `develop`: shared-feature branch (default)

In addition, maintain branches for each supported search engine and its major version.

- `es-9`: Elasticsearch v9.\* and later (not yet supported in v3.4.0)
- `es-8.10-plus`: Elasticsearch v8.10.\* and later
- `os-3`: OpenSearch v3.\* and later
- `os-2.6-plus`: OpenSearch v2.6.\* and later

Notes:

- The supported versions for each branch are managed in `.github/branch-support-matrix.json`.
  - When the supported versions are updated, the change must be backported to each relevant branch.
- Branches must be kept even after support has ended.
  - They are kept for history preservation only and are not subject to releases or updates.
  - No explicit maintenance will be provided even for security fixes or buildability.

### Handling Code Divergence via the `ext` Directory

- Do not add new version-specific differences to `src/main/ext`; implement them directly in the target series branch.
- The use of `src/main/ext/` and `src/test/ext/` should be reduced gradually.
  - `src/test/ext/` may still be used to absorb test-only differences within the same series.

### Splitting Branches for Minor-Version Differences

If additional code differences become necessary between minor versions within the same series (`engine + major version`), branch splitting is allowed.

Use the following as guidance when deciding whether to split:

- A significant minor-version-specific code difference occurs in `src/main/`.
  - Differences limited to `src/test` are excluded and should be handled with `src/test/ext`.

Checklist when splitting branches:

- [ ] Split the branch.
  - New branch names must follow `[engine]-[min major.minor]-[max major.minor]`.
  - The original branch must be renamed to `[engine]-[major.minor]-plus`.
  - e.g. splitting out versions up to `9.2` from `es-9`: `es-9.0-9.2`, `es-9.3-plus`
  - e.g. splitting out versions up to `8.15` from `es-8.14-plus`: `es-8.14-8.15`, `es-8.16-plus`
- [ ] Update the documentation.
  - Document the versions supported by each branch (`README.md`).
  - Document the reason for the split (`README.md`).
- [ ] Update GitHub workflows.
  - Update and backport `./github/branch-support-matrix.json`
  - Update or add PR labels for backport workflow.
- [ ] Review existing issues and PRs.
  - Update base branches.
  - Replace backport labels as needed.
  - Address any impact caused by the split.

## PR Operation Policy

### Base Branch

- For changes shared across multiple series, create the PR against `develop`.
  - Changes must be propagated to the required series according to the cherry-pick/backport rules.
  - Examples: shared logic such as filters and their tests, documentation, build configuration
- For changes specific to a single series, create the PR against that series branch.
  - Examples: series-specific bug fixes, dependency updates
- For changes shared within a specific search engine or major version, create the PR against the latest applicable branch among them.
  - Changes must be propagated to the required series according to the cherry-pick/backport rules.
  - However, if the change is not expected to be backportable, create separate PRs as needed.
  - Example: a bug fix related to Elasticsearch

For clarity, the relevant branch and the need for backporting, including target branches, must also be stated in the PR description.

#### Base Branch Selection Guide

Updates to `search/` and related tests should generally target the applicable branch, while updates to other areas such as `spi/`, `lucene/`, and `build.gradle` should generally use `develop` as the base.

- Updates to Gradle, CI, or build configuration: `develop`
- Adding or modifying filters: `develop`
- Bug fixes specific to a certain version: the branch that includes that version
- Bug fixes specific to a certain search engine: the latest branch for that search engine, then backport as needed

### Cherry-pick / Backport Rules

Whether cherry-pick/backport is required, and which target branches are involved, must be determined for each PR.

Specify the backport target by adding the label `backport [target branch]` to the source PR (e.g. `backport es-9`).
If backporting to multiple branches is required, the following shorthand labels may also be used.

- `backport-es`: backport to all Elasticsearch series
- `backport-os`: backport to all OpenSearch series
- `backport-all`: backport to all series

When the PR is merged (or when the label is added if it has already been merged), an action to create the backport PR is triggered, and completion is defined as the merge of that generated backport PR.

- If the auto-generated PR fails tests, apply fixes as appropriate or perform a manual backport.

## CI / Test Operations

- Each series branch must have tests against its supported versions.
  - Coverage should be at the `major.minor` level, with only the latest patch version covered.
  - A version is considered supported when these tests pass.
- Within-series test differences should continue to be managed using `src/test/ext/*` (for test purposes only).
  - When adding code to `src/test/ext/*`, document the applicable version range in code comments or similar.

## Release Operations

Releases are made based on factors such as the development status of `develop`, fixes for critical bugs, and the addition of support for new versions.

- Each release includes builds for all supported versions, even if the changes affect only some of them.
- If only support for new versions is added and there are no functional changes, a release may be made as an extension of the latest existing release.

The release point is the commit on each series branch that has the corresponding release tag, and builds for each supported version in that series are published as assets in the GitHub release.

- This tag must be in the form `[release version]-[branch]` so that the series can be identified in addition to the release version.
  - Examples: `v3.5.0-es-8`, `v3.5.0-es-8.10-8.13`, `v3.5.0-os-3.2-plus`

### Release Procedure

Regular release

- Add a tag in the form `[release version]-[branch]` at the release point on each series branch.
  - Examples: `v3.5.0-es-8.10-plus`, `v3.5.0-os-3`
- Add a tag in the form `[release version]` to the default branch.
  - This triggers builds for each supported version from the `release-orchestrator` workflow.
- If a series branch tag was missed or a build failed, fix the issue and then re-run the workflow manually.
- Review the draft release contents and publish the release.

Adding a new supported version

- If adding support for a version does not require code changes, you may verify that build/test pass on the existing commit tagged with `[release version]-[branch]`, then run the `release-series` workflow manually to add assets only for the newly supported version(s) to the latest existing release.
- In this case, `.github/branch-support-matrix.json` must still be updated and backported.

## History and Background

### 2026-03 - now: Branch-Based Management of Code Differences

Current management approach

- Reasons for the policy change:
  - To move version-specific differences to branch boundaries and simplify the code within each branch.
  - To make the target series of each code change explicit and simplify development and review.
- Accepted drawbacks:
  - Increased synchronization, backport, and release decision costs as the number of branches grows.
  - Increased CI configuration and test matrix management overhead.

### - 2026-03: Managing Differences via the `ext/` Directory

- Difference-specific code was separated into `src/main/ext/*` and `src/test/ext/*`, and switched by the custom `es.sources` Gradle plugin defined in `BuildSrc/`.
  - This enabled code management on a single branch.
- Drawbacks of this approach:
  - Responsibility was split between `ext` and the main implementation.
  - Traceability of differences was reduced.
  - The scope of changes increased when adding support for new versions.
