---
title: "Installing the Galasa CLI offline"
---

The Galasa _isolated.zip_ file is available from the [https://resources.galasa.dev/](https://resources.galasa.dev){target="_blank"} site and can be downloaded and extracted to a directory of your choice. The zip file contains three directories (galasactl, maven, and javadoc), an `isolated.tar` file and a `docs.jar` file. 

The `galasactl` directory contains the binaries that are required to run the Galasa command line interface tool (Galasa CLI). The `maven` directory contains dependencies that are required for building Galasa tests. The `javadoc` directory contains the Javadoc API documentation for the Galasa Managers.

The `isolated.tar` file is an optional Docker image that hosts all the artifacts. You might want to use the Docker image if you want to host Galasa on an internal server that can be accessed by other users. If you want to host Galasa on your local machine only, you do not need to use the Docker image. 

The `docs.jar` file enables you to run the Galasa website locally on your machine or on an internal server. Instructions on how to do this are available in the `README.txt` that is provided in the Galasa zip file. 


## Getting started

The Galasa plug-in is accompanied by Galasa SimBank - a demonstration application - which sits on top of a very small middleware layer called SimPlatform (you may see its name in some console messages, but you will otherwise not need to interact with SimPlatform).

This section describes using the Galasa command line tool (galasactl) to install the Galasa plug-in - together with SimPlatform/SimBank - on your local machine and preparing it to run an initial set of provided tests against a simulated mainframe application.


## Unpacking the zip file

Extract the contents of the zip file into a directory of your choice.

If you are using the zipped distribution hosted in Docker, ensure that you have the appropriate privileges to run Docker commands on the server on which you are hosting the Galasa artifacts and complete the following steps to load and run the Docker image: 

Note: The example uses port `8080` but you can use a different port.

1. Within the directory that contains the Docker image (`isolated.tar`), run the following command:

    ```shell
    docker load -i isolated.tar
    ``` 

    The following confirmation message is received: _Loaded image: ghcr.io/galasa-dev/galasa-isolated:main_.

2. Run the container by using the following command: 

    ```shell
    docker run -d -p 8080:80 --name galasa ghcr.io/galasa-dev/galasa-isolated:main
    ```

3. Go to `http:\\localhost:8080` to view the running container. 


## Configuring the Galasa CLI for offline use

To install Galasa for using in the command-line you first need to select the appropriate binary file for the Galasa CLI from the `galasactl` directory that is provided in the zip file you downloaded.

The following versions of the Galasa CLI are available to download for different operating systems and machine architectures:

| Operating system  |  Architecture  | Download  |
| :---- | :---- | :-------- | 
| MacOSX | x86_64 | galasactl-darwin-x86_64 |
| MacOSX | arm64 | galasactl-darwin-arm64 |
| Linux | x86_64 | galasactl-linux-x86_64 | 
| Linux arm64 | arm64 | galasactl-linux-arm64 | 
| zLinux  | s390x| galasactl-linux-s390x | 
| Windows | x86_64| galasactl-windows-x86_64.exe | 


Complete the following steps to install Galasa for using the command line:

On Mac or Unix:

1. Find out the architecture of your machine by typing the command `uname -m` into your terminal. You can use the information that is returned to understand which binary of the Galasa CLI you need to use for your particular machine architecture.

1. Open the `galasactl` directory that is provided in the zip file and re-name the appropriate binary to `galasactl`. 

1. Add the Galasa CLI to your PATH to enable you to run CLI commands from anywhere on your file system without having to specify the absolute path. To set the path permanently, you need add the Galasa CLI path to your shell's initialization file. For example, if you downloaded the `galasactl` executable to a folder called `~/tools` in your home directory, you need to add `~/tools` to the list of directories that your shell searches through when you enter a command. You can do this by adding the line `export PATH=$PATH:$HOME/tools` to your shell’s initialization file (for example `~/.bashrc` or `~/.zshrc`).

1. Set execute permission on the binary by running the `chmod +x galasactl` command in the directory containing `galasactl`. If you are using a Mac, you can set permission to open the Galasa CLI tool by running the `xattr -dr com.apple.quarantine galasactl` command in the directory containing `galasactl`.


On Windows (Powershell):

1. Open the `galasactl` directory that is provided in the zip file and re-name the appropriate to `galasactl`.
2. Add the `galasactl` executable to your PATH to enable the tool to be called from the command line without having to specify the path to the directory in which it is stored. You can edit the PATH variable in your System environment variables to add the path to the directory in which you downloaded the `galasactl` executable.
3. Open a command prompt and type `start galasactl.exe`.

You can now run the Galasa CLI tool from any directory in your file system without having to specify the absolute path.


## Next steps

Find out more about the Galasa CLI commands by reading the [Galasa CLI commands](./cli-command-reference-about.md) documentation.

Move on to the [Initialising your local environment](./initialising-home-folder.md) documentation to help you to set up some basic file structures and files in your home folder so that you can start using Galasa.


