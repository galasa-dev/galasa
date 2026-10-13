# AGENTS.md — CLI module (`galasactl`)

Go module path: `github.com/galasa-dev/cli`.

## Package layout

```
pkg/
  cmd/              - Cobra command structs; one file per command (e.g. tagsGet.go + tagsGet_test.go)
  spi/              - Interfaces only (Factory, GalasaCommand, FileSystem, etc.) — no concrete types here
  api/              - REST API client wiring (APICommsClient, bootstrap resolution)
  errors/           - All error message constants (GALASA_ERROR_* format)
  galasaapi/        - Generated OpenAPI client types — do not edit by hand
  <resource>/       - Business logic for a resource (e.g. pkg/tags/, pkg/streams/)
  <resource>formatter/ - Output formatters for a resource (e.g. pkg/tagsformatter/)
  utils/            - Shared utilities and MockFactory for tests
  embedded/         - Embedded files (REST API version string, templates)
```

## Build and test

```bash
./build-locally.sh           # build and test
```

## Adding a new command group

A command group (e.g. `galasactl widgets`) requires changes in five places:

1. **`pkg/cmd/<resource>.go`** — Parent command struct.
   - Define `<Resource>CmdValues` struct for shared flags (e.g. `--name`).
   - Define `<Resource>Command` struct with `cobraCommand *cobra.Command` and `values *<Resource>CmdValues`.
   - Constructor: `func New<Resource>Cmd(rootCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) (spi.GalasaCommand, error)`.
   - Implement `Name() string`, `CobraCommand() *cobra.Command`, `Values() interface{}`.
   - Call `rootCommand.CobraCommand().AddCommand(...)` and `commsFlagSet.Flags()` for persistent flags.

2. **`pkg/cmd/<resource><Verb>.go`** — One file per sub-command (get, set, delete).
   - Constructor receives `factory spi.Factory`, parent `spi.GalasaCommand`, `commsFlagSet GalasaFlagSet`.
   - `RunE` calls an `execute<Resource><Verb>` method that delegates to `pkg/<resource>/`.
   - The execute method must: call `utils.CaptureLog`, resolve `GalasaHome`, create an `api.APICommsClient`, then call the business-logic function.

3. **`pkg/cmd/commandCollection.go`** — Register the new command group.
   - Add `COMMAND_NAME_<RESOURCE>` and `COMMAND_NAME_<RESOURCE>_<VERB>` string constants.
   - Add an `add<Resource>Commands(factory, rootCommand, commsFlagSet)` method following the same flat `if err == nil` chain used by `addTagsCommands`.
   - Call it from `init()` in the `if err == nil` chain.
   - Add all commands to `commands.commandMap` inside the method.

4. **`pkg/<resource>/`** — Business logic package.
   - Public entry-point signature: `func Get<Resource>(name string, format string, commsClient api.APICommsClient, console spi.Console, byteReader spi.ByteReader) error`.
   - Use `commsClient.RunAuthenticatedCommandWithRateLimitRetries(func(apiClient *galasaapi.APIClient) error {...})` for all REST calls.
   - Check `httpResponse` for nil before deferring `Body.Close()`.
   - On API error use `galasaErrors.HttpResponseToGalasaError(...)` with the five resource-specific error constants.

5. **`pkg/<resource>formatter/`** — Output formatters.
   - Define a `<Resource>Formatter` interface: `FormatTags(items []galasaapi.Galasa<Resource>) (string, error)` and `GetName() string`.
   - Implement at minimum a `summary` formatter and a `yaml` formatter.

## Authentication and tokens

Every command that calls the Galasa service requires a valid token. The CLI resolves credentials in this order:

1. `--galasahome <path>` flag (overrides everything)
2. `GALASA_HOME` environment variable
3. `$HOME/.galasa` (default)

The token is then read from:
- `$GALASA_HOME/galasactl.properties` — property key `GALASA_TOKEN`
- `GALASA_TOKEN` environment variable (takes precedence over the properties file)

Token format: `<refresh_token>:<client_id>` (two parts separated by `:`).

The bootstrap URL (pointing at the Galasa service) is supplied via:
- `--bootstrap <url>` flag
- `bootstrap.properties` in `$GALASA_HOME` (fallback)

**When writing or testing code that needs credentials:**
- Use `factory.GetEnvironment()` to read env vars — never call `os.Getenv` directly.
- In tests, set token values via `utils.NewMockFactory()` and its mock `Environment` — do not use real tokens in test code.
- Mock token strings in tests must use the `<value>:<value>` format
- Never add logic that reads `galasactl.properties` outside of `pkg/auth/` — that package owns all token resolution.

## Error handling

- All errors are `*galasaErrors.GalasaError` wrapping a `*MessageType`.
- Constants live in `pkg/errors/errorMessage.go`, prefixed `GALASA_ERROR_`, message prefix `GAL<ordinal>E:`.
- Each REST operation needs its own five error constants: no response content, unreadable body, unparseable content, server-reported error, explanation-not-JSON.
- Use `galasaErrors.NewGalasaError(GALASA_ERROR_<NAME>, params...)` to construct errors.
- Never return a raw `error` from the standard library when a `GalasaError` constant exists for the failure mode.

## Testing conventions

- Test files are co-located with source, same package (e.g. `tagsGet_test.go` in `package cmd`).
- All tests use `utils.NewMockFactory()` to get a test double for `spi.Factory`.
- Use `github.com/stretchr/testify/assert` for assertions.
- Test names: `Test<CommandName><Scenario>` (e.g. `TestTagsGetHelpFlagGetCorrectly`).
- Command-level tests:
  1. Create a mock factory.
  2. Call `Execute(factory, args)` directly, or use `setupTestCommandCollection(COMMAND_NAME_..., factory, t)` for flag tests.
  3. Assert on `err` and call `checkOutput(expectedStdout, expectedStderr, factory, t)`.
- Business-logic tests in `pkg/<resource>/` mock `api.APICommsClient` via `pkg/api/apiCommsClientMock.go`.

## Logging

- Use the standard `log` package.
- Log entry-point calls with `log.Println("Galasa CLI - <description>")`.
- Log significant intermediate results with `log.Printf(...)`.
- Do not use structured logging or third-party logging libraries.
