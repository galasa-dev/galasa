# AGENTS.md — obr module

Build tooling: Maven (BOM generation), Go (`galasabld`), Docker.

## What this module produces

- **`dev.galasa.uber.obr`**: An OSGi Bundle Repository (OBR) aggregating all Galasa framework, extension, and manager bundles into a single repository index consumed by the Galasa ecosystem.
- **`galasa-bom`**: A Maven Bill of Materials (BOM) POM for version-aligned dependency management in Maven-based test projects.
- **Release manifests**: `release.yaml` files fed into downstream build steps.

## Directory layout

```
galasa-bom/            - Maven BOM POM generation
dev.galasa.uber.obr/   - OBR index generation
obr-generic/           - Generic OBR assembly logic
dependency-download/   - Dependency fetching scripts
javadocs/              - Aggregated Javadoc generation
dockerfiles/           - Docker images for build infrastructure
```

## Build

```bash
./build-locally.sh           # assemble OBR and BOM from local artifact cache
```

This module depends on all upstream modules having published their artifacts first. There are no unit tests.

## Conventions

- Do not add source code or unit tests here — this module is a packaging/aggregation step only.
- To change which bundles are included in the OBR, update the relevant `release.yaml` in the source module (framework, managers, extensions), not here.
- The `galasa-bom` POM version is kept in sync with the overall Galasa release version via `set-version.sh`.
