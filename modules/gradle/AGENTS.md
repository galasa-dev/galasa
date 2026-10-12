# AGENTS.md — gradle module

## What this module produces

Gradle plugins used by Galasa test projects to build and deploy test bundles. Published as JAR artifacts to a Maven repository.

## Directory layout

```
dev.galasa.gradle.impl/        - Main Gradle plugin implementation
dev.galasa.plugin.common/      - Shared plugin interfaces
dev.galasa.plugin.common.impl/ - Shared plugin implementations
dev.galasa.plugin.common.test/ - Test utilities for plugin tests
```

## Build and test

```bash
./build-locally.sh           # build and test
```

## Coding conventions

- Plugin goals are implemented as Gradle task classes — follow the patterns in `dev.galasa.gradle.impl/`.
- Dependency versions are resolved from `sourceMaven` (local artifact cache) and Maven Central.
- JUnit tests live in `src/test/java/` within the relevant subproject.
- GPG signing is CI-only; skip locally with `-PisMainOrRelease=false`.
