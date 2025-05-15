

# Galasa Homepage

[![Build and publish to github pages](https://github.com/techcobweb/galasa.dev/actions/workflows/build.yaml/badge.svg)](https://github.com/techcobweb/galasa.dev/actions/workflows/build.yaml)

This repository contains the source for the Galasa test automation system homepage.

Read the Galasa documentation here:
- [The latest released version](https://techcobweb.github.io/galasa.dev/latest/)
- [under development currently but not released yet](https://techcobweb.github.io/galasa.dev/development/)


## Developing Galasa
If you are interested in the development of Galasa, take a look at the documentation and feel free to post a question in our <a href="https://galasa.slack.com" target="_blank"> Galasa Slack</a> workspace. <a href="https://join.slack.com/t/galasa/shared_invite/zt-ele2ic8x-VepEO1o13t4Jtb3ZuM4RUA" target="_blank"> Register to join</a> first if you're not yet a member. 

## Contacting the Galasa community
We have the following available Slack channels:

- `#galasa-dev` - for developers of Galasa code or Galasa extensions 
- `#galasa-users` - for users comments, and for making announcements 
- `#galasa-tsc` - for technical steering committee discussions on longer term over-arching issues relating to Galasa 

You can raise new ideas / features / bugs etc. as issues on [GitHub](https://github.com/galasa-dev/projectmanagement). 

## Contributing

Take a look at the [contribution guidelines](https://github.com/galasa-dev/projectmanagement/blob/main/contributing.md).

Any changes to the documentation should be contributed as pull requests:

1. Make a fork of this repository (top-right).
1. Make your changes in a branch of your fork.
1. Create a pull request for your changed branch.

## To build and test the documentation locally
- make sure you have the required python packages installed: `pip3 install --user -r requirements.txt`
- make sure your `gh-pages` branch reflects the current state of the version on the server :
`git branch --delete -D gh-pages ; git checkout -t origin/gh-pages ; git checkout {mybranch}`
- tell your `mike` tool that the default branch is the branch you are working on. For example: for the 'development' branch use `mike set-default development`
- run `mike deploy` to build the docs, and push them to your (local) branch of gh-pages.
- run `mike serve` to host the documentation on a local server
- browse your documentation by visiting http://localhost:8000/development/


## Release the documentation
- Create a release branch in `gh-pages` by using the git web user interface to create a release. Call it vx.yy.z. This will build and push the new content for the release into the gh-pages site.
- If you want that release to be marked as 'latest' then manually trigger the `releases-set-latest` workflow from the Github Actions dashbaord for this repository.

## License

This code is under the [Eclipse Public License 2.0](./LICENSE).


## Code structure
- [`modules`](./modules/) - The code
- [`tools`](./tools/) - Build tools and useful scripts

## building locally

Use the `./tools/build-locally.sh` script. `--help` shows you the options.

Basic usage to build everything: `build-locally.sh`

## setting the source code version

The `set-version.sh` script allows you to set the version of the Galasa throughout this repository.

Use the `--help` flag to see what options are supported.

Basic usage: `set-version.sh --version 0.42.0`

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