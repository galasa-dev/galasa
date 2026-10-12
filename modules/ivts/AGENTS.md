# AGENTS.md — ivts module

## What this module produces

Integration Verification Tests (IVTs) — Galasa test classes that validate Galasa managers against a live Galasa ecosystem. These are not unit tests; they require a running Galasa service and (for z/OS IVTs) mainframe access.

## Directory layout

```
galasa-ivts-parent/
  dev.galasa.ivts/       - Non-mainframe IVTs (HTTP, Core, etc.)
  dev.galasa.zos.ivts/   - z/OS IVTs (require mainframe access)
compilation-tests/       - Bundles that verify test code compiles against manager SPIs
dockerfiles/             - Docker images used in IVT execution environment
```

## Build and test

```bash
./build-locally.sh           # compile and package IVT bundles
```

Do not add standalone unit tests here — logic under test belongs in `modules/managers/` or `modules/framework/`.

## Coding conventions

- IVT test classes extend or use `@Test`-annotated methods following Galasa test conventions.
- Separate mainframe-dependent tests (`dev.galasa.zos.ivts/`) from non-mainframe tests (`dev.galasa.ivts/`).
- Compilation tests verify that the manager SPI is consumable — keep them minimal and focused on the public API surface.
