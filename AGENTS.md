# AGENTS.md

## Repository Structure & Modules
This repository is a multi-module monorepo powering the Galasa test automation framework:
- **`modules/framework/`**: Core Java framework OSGi bundles (Gradle build).
- **`modules/managers/`**: Official Galasa Managers (e.g. z/OS, Core, HTTP, etc.) built as OSGi bundles (Gradle build).
- **`modules/cli/`**: `galasactl` command-line tool implemented in Go.
- **`modules/buildutils/`**: Build utilities, helper tools, and container images (Go & Docker).
- **`modules/extensions/`**: Galasa extensions and plugins (Gradle build).
- **`modules/gradle/` & `modules/maven/`**: Build plugin integrations for Galasa tests.
- **`modules/platform/`, `modules/obr/`, `modules/wrapping/`**: OSGi bundles, packaging, and dependency resolution.
- **`modules/ivts/`**: Integration and verification test suites.
- **`developer-docs/`**: Architecture diagrams, test lifecycles, and contributor guides.

Each module has its own `AGENTS.md` with module-specific coding conventions, patterns, and testing guidance. Read the module-level `AGENTS.md` before making changes within that module.

## Build and Test Workflows
- **Targeted module testing**: Prefer running narrow, module-specific test suites before full monolithic builds:
  - Java/Gradle modules: `gradle test` within the module directory or targeting specific tasks (use system `gradle` CLI, do not use `gradlew`).
  - Java/Maven modules: `mvn test` in the target module directory.
  - CLI (Go): `go test ./...` in `modules/cli`.
- **Full local build**: Use `tools/build-locally.sh` for complete builds across all modules. Pass `--help` to see options. A full chain build requires GitHub credentials (see `CONTRIBUTING.md`); individual modules can be built without them.
- **Prerequisite toolchain**: See `developer-docs/install-pre-req-tools.md` for exact versions and installation steps.

## Commit & PR Rules
- **Developer Certificate of Origin (DCO)**: All commits must include a sign-off (`git commit -s`).
- **GPG Signing**: Commits should be GPG-signed where feasible (`git commit -S`).
- **Conventional Commits**: Commit messages should follow `type(scope): description` (e.g., `feat(cli): add command`, `fix(framework): resolve lifecycle issue`, `docs: update guides`).
- Pull requests target `main` and should follow [`.github/pull_request_template.md`](.github/pull_request_template.md).
