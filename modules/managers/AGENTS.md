# AGENTS.md — managers module

## What this module produces

OSGi bundles implementing Galasa Managers — the test framework components that give test code access to system resources (z/OS, CICS, HTTP, Kubernetes, Docker, databases, etc.). Each manager ships as one or more OSGi bundles.

## Directory layout

Managers are grouped by domain under `galasa-managers-parent/`:

```
galasa-managers-core-parent/     - Core and artifact managers
galasa-managers-cicsts-parent/   - CICS TS managers
galasa-managers-zos-parent/      - z/OS managers (console, batch, file, etc.)
galasa-managers-cloud-parent/    - Docker and Kubernetes managers
galasa-managers-comms-parent/    - HTTP, IPNETWORK managers
galasa-managers-database-parent/ - Database managers
galasa-managers-logging-parent/  - Logging managers
```

Within each group, a manager typically has:
- `dev.galasa.<manager>/` — public SPI (annotations + interfaces used by test authors)
- `dev.galasa.<manager>.manager/` — framework implementation (registered as OSGi service)
- `dev.galasa.<manager>.manager.ivt/` — integration verification test bundle (lives in `ivts` module)

## Build and test

```bash
./build-locally.sh --clean    # full clean build including tests
./build-locally.sh --delta    # incremental build
```

## Coding conventions

- Each manager implementation registers itself as an OSGi `@Component` service.
- The SPI bundle (`dev.galasa.<manager>/`) must not depend on the implementation bundle.
- Dependency versions are managed by the `dev.galasa.platform` BOM — do not hardcode versions.
- JUnit tests live in `src/test/java/` within the implementation bundle.
- Use manual mocks for mocking the framework and other manager services. If this is not feasible, use Mockito.
- GPG signing is CI-only; skip locally with `-PisMainOrRelease=false`.
- Do not add mainframe-specific tests to unit tests — those belong in `modules/ivts/`.
