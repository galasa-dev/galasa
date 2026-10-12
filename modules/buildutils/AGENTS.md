# AGENTS.md — buildutils module

## What this module produces

`galasabld` — a multi-platform CLI binary used in the Galasa build pipeline for version management, release manifest generation, and build orchestration. Compiled for Linux (amd64, arm64, s390x), Windows (amd64), and Darwin (amd64, arm64).

## Directory layout

```
cmd/galasabld/   - main() entry point
pkg/
  cmd/           - Cobra command implementations
  galasayaml/    - YAML structure definitions for Galasa build artifacts
  githubjson/    - GitHub API JSON types
  utils/         - Shared utilities
  versioning/    - Version string handling
bin/             - Compiled binaries (git-ignored)
build/           - Coverage reports
```

## Build and test

```bash
./build-locally.sh           # build and test
```

## Coding conventions

- Cobra CLI framework for command structure — follow the pattern in `pkg/cmd/`.
- Cross-compilation: all code must compile with `CGO_ENABLED=0`.
- Use `github.com/stretchr/testify/assert` for test assertions.
- Do not add third-party dependencies without updating the `go.mod`.
