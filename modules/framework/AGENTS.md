# AGENTS.md — Framework module

## Bundle structure

Each OSGi bundle is a subdirectory of `galasa-parent/` named `dev.galasa.framework.<area>`:

```
galasa-parent/
  dev.galasa.framework/            - Core framework interfaces and lifecycle
  dev.galasa.framework.api.<area>/ - REST servlet + DTOs for one API area (e.g. .api.tags, .api.runs)
  dev.galasa.framework.api.beans/  - OpenAPI-generated request/response beans — do not edit by hand
  dev.galasa.framework.api.openapi/ - OpenAPI spec (openapi.yaml) — source of truth for the REST API
  dev.galasa.framework.auth.spi/   - Authentication SPI interfaces
  dev.galasa.framework.<area>/     - Non-API bundles (metrics, resource management, controllers, etc.)
```

## Adding a new REST endpoint

1. Add or update the operation in `dev.galasa.framework.api.openapi/src/main/resources/openapi.yaml`.
2. Run the `openapi2beans` code generation step (handled automatically in CI; locally, see `framework.yaml` workflow for the Docker command).
3. Add the servlet class to the appropriate `dev.galasa.framework.api.<area>` bundle under `src/main/java/`.
4. Register the servlet in the bundle's `bnd.bnd` or OSGi component descriptor.
5. Add JUnit tests under `src/test/java/` in the same bundle.

## Build

```bash
./build-locally.sh --clean    # full clean build including tests
./build-locally.sh --delta    # incremental build
```

Publishing to GitHub Packages (done in CI) requires `GITHUB_TOKEN`, `GPG_KEY`, `GPG_KEYID`, and `GPG_PASSPHRASE` — not needed for local development.

## Testing conventions

- Test framework: JUnit (version declared in `buildSrc/`).
- Tests live in `src/test/java/` within the same bundle as the code under test.
- Use manual mocks for mocking (refer to existing mocks in `mocks/` and `testFixtures/` directories).
