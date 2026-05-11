---
title: "Installing the Galasa CLI"
---

Download and install the Galasa CLI tool from the [Galasa Releases page](https://github.com/galasa-dev/galasa/releases){target="_blank"} in GitHub.

!!! info "Installing Galasa offline"
    If you would like to install Galasa in environments where internet access is restricted or unavailable, see [Installing Galasa offline](../using-galasa-offline/installing-offline.md) for instructions on using the offline distribution.

## Prerequisites

Before installing, ensure you have the required software installed. See the [Prerequisites](./cli-prereqs.md) documentation for details.

## Available downloads

The following versions of the Galasa CLI are available for different operating systems and architectures:

| Operating system | Architecture | Download                                                                                                                      |
| :--------------- | :----------- | :---------------------------------------------------------------------------------------------------------------------------- |
| MacOSX           | x86_64       | [galasactl-darwin-x86_64](https://github.com/galasa-dev/galasa/releases/latest/download/galasactl-darwin-x86_64){target="_blank"}           |
| MacOSX           | arm64        | [galasactl-darwin-arm64](https://github.com/galasa-dev/galasa/releases/latest/download/galasactl-darwin-arm64){target="_blank"}             |
| Linux            | x86_64       | [galasactl-linux-x86_64](https://github.com/galasa-dev/galasa/releases/latest/download/galasactl-linux-x86_64){target="_blank"}             |
| Linux arm64      | arm64        | [galasactl-linux-arm64](https://github.com/galasa-dev/galasa/releases/latest/download/galasactl-linux-arm64){target="_blank"}               |
| zLinux           | s390x        | [galasactl-linux-s390x](https://github.com/galasa-dev/galasa/releases/latest/download/galasactl-linux-s390x){target="_blank"}               |
| Windows          | x86_64       | [galasactl-windows-x86_64.exe](https://github.com/galasa-dev/galasa/releases/latest/download/galasactl-windows-x86_64.exe){target="_blank"} |

## Installation

Choose your installation method:

=== "macOS (Homebrew)"

    1. Connect Homebrew to the Galasa tap:
       ```bash
       brew tap galasa-dev/tap
       ```
    
    2. Install the latest version:
       ```bash
       brew install galasactl
       ```
       
       Or install a specific version (e.g., 0.48.0):
       ```bash
       brew install galasactl@0.48.0
       ```
    
    3. Check available versions:
       ```bash
       brew tap-info galasa-dev/tap --json
       ```
       Look in the `"cask_tokens"` section of the output.

    4. Remove quarantine attribute:
       ```bash
       xattr -dr com.apple.quarantine galasactl
       ```

=== "Linux or macOS (manual download)"

    1. Find your machine architecture:
       ```bash
       uname -m
       ```
    
    2. Download the appropriate binary from the [Galasa Releases page](https://github.com/galasa-dev/galasa/releases){target="_blank"} and rename it to `galasactl`
    
    3. Add the Galasa CLI to your PATH. For example, if you downloaded galasactl to `~/tools`, add this line to your shell's initialization file (`~/.bashrc` or `~/.zshrc`):
       ```bash
       export PATH=$PATH:$HOME/tools
       ```
    
    4. Set execute permission:
       ```bash
       chmod +x galasactl
       ```
    
    5. **macOS only**: Remove quarantine attribute:
       ```bash
       xattr -dr com.apple.quarantine galasactl
       ```

=== "Windows (PowerShell)"

    1. Download the Windows executable from the [Galasa Releases page](https://github.com/galasa-dev/galasa/releases){target="_blank"} and rename it to `galasactl.exe`
    
    2. Add the directory containing `galasactl.exe` to your PATH environment variable through System Properties → Environment Variables
    
    3. Open a command prompt and verify:
       ```powershell
       galasactl --version
       ```

You can now run the Galasa CLI from any directory without specifying the full path.

## Upgrading the Galasa CLI

=== "macOS (Homebrew)"

    Update to the latest version:
    ```bash
    brew update
    brew upgrade galasactl
    ```
    
    The `brew update` command refreshes Homebrew's catalog, and `brew upgrade galasactl` updates the CLI.

=== "Linux or macOS (manual)"

    Download the latest binary from the [Galasa Releases page](https://github.com/galasa-dev/galasa/releases){target="_blank"} and replace your existing `galasactl` binary.

=== "Windows"

    Download the latest executable from the [Galasa Releases page](https://github.com/galasa-dev/galasa/releases){target="_blank"} and replace your existing `galasactl.exe` file.

## Next steps

1. Learn about available commands in the [Galasa CLI commands](./cli-command-reference-about.md) documentation
2. [Initialize your local environment](./initialising-home-folder.md) to set up the necessary file structures
