# AGENTS.md — platform module

## What this module produces

A Gradle platform artifact (`dev.galasa.platform`) that centralises dependency version constraints for the entire Galasa ecosystem. It is the single source of truth for third-party library versions — all other Gradle modules import it as a BOM.

## Directory layout

```
dev.galasa.platform/
  build.gradle       - All dependency version constraints declared here
  gradle.properties
  settings.gradle
```

## Build

```bash
./build-locally.sh           # publish platform artifact to local Maven cache
```

No source code; no tests. Validation is via dependency resolution in downstream modules.

## Conventions

- Add new third-party dependencies here first, then reference them without a version in downstream `build.gradle` files.
- Do not specify version numbers in any other module's `build.gradle` for dependencies already declared here.
- Use the `api` dependency configuration in `build.gradle` so constraints are exported to consumers.
