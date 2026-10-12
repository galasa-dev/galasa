# AGENTS.md — maven module

## What this module produces

A Maven plugin (`dev.galasa:galasa-maven-plugin`) consumed by Galasa test projects to build test catalogs, deploy them to an ecosystem, and package Gherkin tests.

## Directory layout

```
galasa-maven-plugin/
  src/main/java/   - Plugin Mojo implementations (one class per goal)
  src/test/java/   - Unit tests using maven-plugin-testing-harness
  pom.xml          - Maven configuration
```

## Build and test

```bash
./build-locally.sh           # build and test
```

## Plugin goals

| Goal | Purpose |
|---|---|
| `bundletestcat` | Builds a test catalog from compiled test bundles |
| `deploytestcat` | Deploys a test catalog to a Galasa ecosystem |
| `gherkinzip` | Packages Gherkin feature files into a ZIP |
| `gitcommithash` | Injects the current git commit hash into build metadata |

## Coding conventions

- Each goal is a `@Mojo`-annotated class in `src/main/java/`.
- Tests use `maven-plugin-testing-harness`; mock ecosystem endpoints with WireMock or similar.
- GPG signing is CI-only; skip locally with `-Dgpg.skip=true`.
- Do not hardcode dependency versions — they are managed by the `dev.galasa.platform` POM.
