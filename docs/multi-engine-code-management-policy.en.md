# Policy for Managing Code Differences Across Search Engine Variants (ES/OS)

## Overview

Because the APIs differ across Elasticsearch / OpenSearch and their versions, we need to manage code differences for each target.
In this repository, those differences are managed by splitting them into separate branches.

This document defines the policy for managing those branches.

## Basic Principles

- The default branch is responsible for shared functionality, and branch-based divergence should be kept to a minimum.
- As a rule, maintain one branch for each API lineage (`engine + major version`).
  - If API differences arise within the same lineage, further branch splitting is also allowed.
- Changes should originate from the default branch and be cherry-picked/backported to the required lineages.

## Code Management Policy

### Current Branches

- `develop`: shared-feature branch (default)

In addition, maintain branches for each supported search engine and its major version.

- `es-9`: Elasticsearch v9.\* and later (not yet supported in v3.4.0)
- `es-8.10+`: Elasticsearch v8.10.\* and later
- `os-3`: OpenSearch v3.\* and later
- `os-2.6+`: OpenSearch v2.6.\* and later

Notes:

- The supported versions for each branch must be clearly documented in `README.md` and the CI matrix.
- Branches must be kept even after support has ended.
  - They are kept for history preservation only and are not subject to releases or updates.
  - No explicit maintenance will be provided even for security fixes or buildability.

### Handling Code Divergence via the `ext` Directory

- Do not add new version-specific differences to `src/main/ext`; implement them directly in the target lineage branch.
- The use of `src/main/ext/` and `src/test/ext/` should be reduced gradually.
  - `src/test/ext/` may still be used to absorb test-only differences within the same lineage.

### Splitting Branches for Minor-Version Differences

If additional code differences become necessary between minor versions within the same lineage (`engine + major version`), branch splitting is allowed.

Use the following as guidance when deciding whether to split:

- A significant minor-version-specific code difference occurs in `src/main/`.
  - Differences limited to `src/test` are excluded and should be handled with `src/test/ext`.

Checklist when splitting branches:

- [ ] Split the branch.
  - New branch names must follow `[engine]-[min major.minor]-[max major.minor]`.
  - The original branch must be renamed to `[engine]-[major.minor]+`.
  - e.g. splitting out versions up to `9.2` from `es-9`: `es-9.0-9.2`, `es-9.3+`
  - e.g. splitting out versions up to `8.15` from `es-8.14+`: `es-8.14-8.15`, `es-8.16+`
- [ ] Update the documentation.
  - Document the versions supported by each branch (`README.md`).
  - Document the reason for the split (`README.md`).
- [ ] Update GitHub workflows.
  - Test workflow: make each split branch responsible for its supported versions.
  - Release workflow: make each split branch responsible for its supported versions.
  - Backport workflow: update the post-split backport target branches.
    - Update or add PR labels.
- [ ] Review existing issues and PRs.
  - Update base branches.
  - Replace backport labels as needed.
  - Address any impact caused by the split.

## PR Operation Policy

### Base Branch

- For changes shared across multiple lineages, create the PR against `develop`.
  - Changes must be propagated to the required lineages according to the cherry-pick/backport rules.
  - Examples: shared logic such as filters and their tests, documentation, build configuration
- For changes specific to a single lineage, create the PR against that lineage branch.
  - Examples: lineage-specific bug fixes, dependency updates
- For changes shared within a specific search engine or major version, create the PR against the latest applicable branch among them.
  - Changes must be propagated to the required lineages according to the cherry-pick/backport rules.
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

- `backport-es`: backport to all Elasticsearch lineages
- `backport-os`: backport to all OpenSearch lineages
- `backport-all`: backport to all lineages

When the PR is merged (or when the label is added if it has already been merged), an action to create the backport PR is triggered, and completion is defined as the merge of that generated backport PR.

- If the auto-generated PR fails tests, apply fixes as appropriate or perform a manual backport.

## CI / Test Operations

- Each lineage branch must have tests against its supported versions.
  - Coverage should be at the `major.minor` level, with only the latest patch version covered.
  - A version is considered supported when these tests pass.
- Within-lineage test differences should continue to be managed using `src/test/ext/*` (for test purposes only).
  - When adding code to `src/test/ext/*`, document the applicable version range in code comments or similar.

## Release Operations

- Releases are made based on factors such as the development status of `develop`, fixes for critical bugs, and the addition of support for new versions.
  - In each release, builds for all supported lineages are released even if changes exist only for some of them.
  - If only new support is added and there are no functional changes, a release may be made as an extension of the latest existing release.
- The git tag attached to the commit to be released from each branch is used as the release target.
  - Tags must make the lineage identifiable in addition to the release version, such as `[version]-[engine]-[engine major version]`.
    - Examples: `v3.5.0-es-8`, `v3.5.0-es-8.10-8.13`, `v3.5.0-os-3.2+`
- For each branch, create builds for each version supported by that branch and publish them as assets in the GitHub release.

## History and Background

### 2026-03 - now: Branch-Based Management of Code Differences

Current management approach

- Reasons for the policy change:
  - To move version-specific differences to branch boundaries and simplify the code within each branch.
  - To make the target lineage of each code change explicit and simplify development and review.
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
