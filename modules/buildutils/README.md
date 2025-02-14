# Build Utils

This repository is built into tools which themselves are used during a Galasa build. 

## To build locally
Use the `./build-locally.sh --help` to get a description of the required parameters and environment variables.

## The `galasabld` utility


### To list the versions of all gradle modules

```
$galasabld versioning list --sourcefolderpath {my-source-folder}
a.b.c. 0.21.0
a.b.d. 0.25.0-SNAPSHOT
```

To find a module version, the code must:
- have a build.gradle file
- have a settings.gradle file
- the build.gradle file must have a line like `version = "0.1.2"` or similar.
- the settings.gradle file must have a line like `rootProject.name = "dev.galasa.examples/module2"`.

### To set a version suffix on all gradle modules
```
$galasabld versioning suffix set --sourcefolderpath {my-source-folder} --suffix "-alpha"
```
This will recursively look for module versions, stripping off any existing suffix, and adding the `-alpha` suffix to everything.

Note: The value of the `--suffix` parameter must start with `-` or `_`

So for example, `0.0.1` will be changed to `0.0.1-alpha` if `-alpha` is the suffix value.

### To remove any suffix on all gradle modules
```
$galasabld versioning suffix remove --sourcefolderpath {my-source-folder}
```
This will recursively look for module versions, stripping off any existing suffix.
So for example, `0.0.1-SNAPSHOT` will be changed to `0.0.1`

### How to contribute to this module

Take a look at the [Contributor's Guide](https://github.com/galasa-dev/galasa/blob/main/CONTRIBUTING.md).