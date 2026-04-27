---
title: "Installing Galasa offline"
---

The Galasa _isolated.zip_ file is available from the [https://resources.galasa.dev/](https://resources.galasa.dev){target="_blank"} site and provides a complete offline distribution of Galasa. Download and extract it to a directory of your choice.

## What's included

The zip file contains:

- **galasactl directory** - Binaries for the Galasa CLI tool
- **maven directory** - Dependencies required for running Galasa tests
- **javadoc directory** - Javadoc API documentation for Galasa Managers
- **docs.tar file** - Docker image for running the Galasa website locally (see README.txt for instructions)
- **isolated.tar file** - Optional Docker image for hosting Galasa artifacts on an internal server

## Prerequisites

Before installing, ensure you have the required software installed. See the [Prerequisites](../cli-command-reference/cli-prereqs.md)  documentation for details.

## Installation

### Step 1: Download and extract

1. Download the _isolated.zip_ file from [https://resources.galasa.dev/](https://resources.galasa.dev){target="_blank"}
2. Extract the contents to a directory of your choice

### Step 2: Install the Galasa CLI

The `galasactl` directory contains CLI binaries for different operating systems and architectures:

| Operating system | Architecture | Binary name                  |
| :--------------- | :----------- | :--------------------------- |
| MacOSX           | x86_64       | galasactl-darwin-x86_64      |
| MacOSX           | arm64        | galasactl-darwin-arm64       |
| Linux            | x86_64       | galasactl-linux-x86_64       |
| Linux arm64      | arm64        | galasactl-linux-arm64        |
| zLinux           | s390x        | galasactl-linux-s390x        |
| Windows          | x86_64       | galasactl-windows-x86_64.exe |

Choose the appropriate binary for your system and follow the instructions below:

=== "Linux or macOS"

    1. Find your machine architecture:
       ```bash
       uname -m
       ```
    
    2. Navigate to the `galasactl` directory and rename the appropriate binary to `galasactl`
    
    3. Add the Galasa CLI to your PATH. For example, if you extracted galasactl to `~/tools`, add this line to your shell's initialization file (`~/.bashrc` or `~/.zshrc`):
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

    1. Navigate to the `galasactl` directory and rename the Windows executable to `galasactl.exe`
    
    2. Add the directory containing `galasactl.exe` to your PATH environment variable through System Properties → Environment Variables
    
    3. Open a command prompt and verify:
       ```powershell
       galasactl --version
       ```

You can now run the Galasa CLI from any directory without specifying the full path.

## Optional: Hosting on an internal server

If you want to host Galasa artifacts on an internal server for multiple users, you can use the included Docker image. **Skip this section if you're only installing Galasa on your local machine.**

### Prerequisites for Docker hosting

- Docker installed with appropriate privileges
- A server accessible to your team

### Loading and running the Docker image

1. Navigate to the directory containing `isolated.tar`

2. Load the Docker image:
   ```bash
   docker load -i isolated.tar
   ```
   
   You should see: _Loaded image: ghcr.io/galasa-dev/galasa-isolated:release_

3. Run the container (using port 8080 as an example):
   ```bash
   docker run -d -p 8080:80 --name galasa ghcr.io/galasa-dev/galasa-isolated:release
   ```

4. Access the running container at `http://localhost:8080`

## Next steps

Now that you have installed the Galasa offline distribution, you can use Galasa in the same way as the online version:

1. Learn about available commands in the [Galasa CLI commands](../cli-command-reference/cli-command-reference-about.md) documentation
2. [Initialize your local environment](../cli-command-reference/initialising-home-folder.md) to set up the necessary file structures
3. Start exploring with [Galasa SimBank offline](./simbank-cli-offline.md)
