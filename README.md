[![Galasa](https://img.shields.io/github/actions/workflow/status/galasa-dev/galasa/pushes.yaml?label="Galasa")](https://github.com/galasa-dev/galasa/actions/workflows/pushes.yaml) [![WebUI](https://img.shields.io/github/actions/workflow/status/galasa-dev/webui/build.yaml?label="WebUI")](https://github.com/galasa-dev/webui/actions/workflows/build.yaml) [![Helm](https://img.shields.io/github/actions/workflow/status/galasa-dev/helm/build.yaml?label="Helm")](https://github.com/galasa-dev/helm/actions/workflows/build.yaml)

# Galasa 
This is the main source code repository for the Galasa open source project.

## Code structure
- [`modules`](./modules/) - The code
- [`tools`](./tools/) - Build tools and useful scripts
- [`docs`](./docs/) - The source code for our [preview](https://vnext.galasa.dev) and [live](https://galasa.dev) documentation sites



## Building locally

### Pre-reqs
Some tools will need to be installed in order to build this code locally.
See our install instructions [here](./developer-docs/install-pre-req-tools.md)


### To build...
Use the `./tools/build-locally.sh` script. `--help` shows you the options.

Basic usage to build everything: `build-locally.sh`

## setting the source code version

The `set-version.sh` script allows you to set the version of the Galasa throughout this repository.

Use the `--help` flag to see what options are supported.

Basic usage: `set-version.sh --version 0.44.0`

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

## How to contribute to the Galasa project
See our [contribution guidelines](./CONTRIBUTING.md) on how to contribute to the Galasa project.

## Notes about the codebase and Galasa system for contributors
We have some notes [here](./developer-docs/README.md) which may help you understand the codebase,
should you wish to contribute to the project.

## Legal
Galasa is developed under this [license](./LICENSE)

All contributions are governed by the [Developer Certificate of Origin](./CONTRIBUTIONS.md)
