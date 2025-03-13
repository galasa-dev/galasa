# Galasa
This is a home for all of the Galasa Java code.

## Code structure
- [`modules`](./modules/) - The code
- [`tools`](./tools/) - Build tools and useful scripts

## building locally

Use the `./tools/build-locally.sh` script. `--help` shows you the options.

Basic usage to build everything: `build-locally.sh`

## setting the source code version

The `set-version.sh` script allows you to set the version of the Galasa throughout this repository.

Use the `--help` flag to see what options are supported.

Basic usage: `set-version.sh --version 0.41.0`

## Using vscode
When using vscode to develop this code, we recommend the following settings are added to your `settings.json` file:

```
"java.jdt.ls.vmargs": "-Xmx1024m",
"java.import.gradle.arguments" : "-PtargetMaven=~/.m2/repository",

"java.import.gradle.version": "8.9",
"java.configuration.runtimes": [
    {
        "name": "JavaSE-17",
        "path": "/path/to/java/sdk/folder" , // eg: /Users/mcobbett/.sdkman/candidates/java/17.0.12-tem
        "default": true,
    },
],
"java.import.gradle.wrapper.enabled": false,
```