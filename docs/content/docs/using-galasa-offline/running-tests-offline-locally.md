---
title: "Running tests locally in an offline environment"
---

# Running tests locally in an offline environment

In restricted network environments — such as air-gapped systems or corporate networks that block access to Maven Central — you can run Galasa tests locally without any outbound network connectivity by combining two commands:

1. **`galasactl runs prepare local`** — run once, while online, to pre-fetch all required bundle dependencies to your local Maven cache.
2. **`galasactl runs submit local --offline`** — run repeatedly, without a network connection, to execute tests using only the locally cached bundles.

## Step 1: Pre-fetch dependencies

Use the `galasactl runs prepare local` command to download all OSGi bundle dependencies for your test OBRs to the local Maven cache (typically `~/.m2/repository`). No tests are executed by this command.

Run this command on a machine that has access to Maven Central (or your internal Maven mirror), before moving to the restricted environment.

=== "Linux or macOS"

    ```shell
    galasactl runs prepare local --log - \
    --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
    --galasaVersion 1.0.0
    ```

=== "Windows (PowerShell)"

    ```powershell
    galasactl runs prepare local --log - `
    --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr `
    --galasaVersion 1.0.0
    ```

**Parameters explained:**

- `--obr`: The Maven coordinates of the OBR that references your test bundles. Repeat the flag for each OBR you need.
- `--galasaVersion`: The version of Galasa to download. This must match the version your test bundles were built against.
- `--log -`: Sends progress information to the console.

When the command completes, all required Galasa framework bundles and test bundles are cached locally. You can then copy or transfer the local Maven repository to any offline machine.

## Step 2: Run tests offline

Once dependencies are cached, use `galasactl runs submit local` with the `--offline` flag. This prevents the test JVM from contacting any remote Maven repository during bundle resolution.

=== "Linux or macOS"

    ```shell
    galasactl runs submit local --log - \
    --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
    --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
    --offline
    ```

=== "Windows (PowerShell)"

    ```powershell
    galasactl runs submit local --log - `
    --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr `
    --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount `
    --offline
    ```

!!! note
    The `--offline` and `--remoteMaven` flags are mutually exclusive. If you need to specify a non-default remote Maven URL, do not use `--offline`.

## Pointing to a non-default local Maven cache

If your local Maven cache is in a non-standard location (for example, because you copied it from another machine), use the `--localMaven` flag to tell Galasa where to find it:

=== "Linux or macOS"

    ```shell
    galasactl runs submit local --log - \
    --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
    --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
    --localMaven file:///home/myuser/my-offline-cache \
    --offline
    ```

=== "Windows (PowerShell)"

    ```powershell
    galasactl runs submit local --log - `
    --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr `
    --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount `
    --localMaven file:///C:/Users/myuser/my-offline-cache `
    --offline
    ```

## Reference

- [`galasactl runs prepare local`](../reference/cli-syntax/galasactl_runs_prepare_local.md) — full list of flags
- [`galasactl runs submit local`](../reference/cli-syntax/galasactl_runs_submit_local.md) — full list of flags
- [Running tests locally](../cli-command-reference/runs-submit-local.md) — general guide to local test runs
