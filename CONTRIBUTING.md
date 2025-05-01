# Contributing

Welcome to Galasa! To learn more about contributing to this repository, please read this Contributor's Guide.

## How can you contribute?

### Reporting bugs

- Search existing issues to avoid duplicates.
- Include clear and concise steps on how to reproduce the bug.
- Provide relevant details, such as your Galasa version and details about environment.

### Suggesting features

- Open an issue and include a user story, background if relevant, and task list.
- Provide a clear description of the feature.
- Explain why it would be beneficial and how it aligns with the project's goals.

### Contributing code

- Ensure your contribution aligns with the project's roadmap.
- Check out open issues with the label `good first issue`.

### Documentation

- Fix typos, improve examples, or enhance explanations.

## How to make a contribution?

### Set up a fork of a repository

1. On GitHub, navigate to the repository.
1. In the top-right corner of the page, click Fork.
1. Select an owner for the forked repository from the dropdown menu under "Owner".
1. The fork will be named the same as the upstream repository as default. Optionally, to further distinguish your fork, type a name in the "Repository name" field.
1. Optionally, type a description for your fork in the "Description" field.
1. Optionally, select "Copy the `main` branch only".
1. Click "Create fork".

### Understanding the 'galasa' repository workflows 

The 'galasa' repository is divided into several modules that are stored within the `/modules` directory. Certain modules require artifacts from other modules as they have a dependency on them. The "Main Build Orchestrator" and "Pull Request Build Orchestrator" workflows attempt to download artifacts from previous modules either from the same workflow run or from a previously successful completed workflow run. Therefore, before you can open a Pull Request on your forked repository from one branch to another, you will need to run an initial workflow run of the "Main Build Orchestrator", so there is a successful completed workflow run present on your fork.

### Enable GitHub Actions workflows

1. As default, GitHub Actions will be disabled on your fork. You will need to enable the GitHub Actions workflows by going to the Actions tab on your fork and clicking "I understand my workflows, go ahead and enable them".

### Configure repository secrets and variables

To run certain workflows on your fork, you will need to configure some repository secrets and variables so that the workflows can access them. These secrets and variables are used for things such as authenticating to the GitHub Container Registry and supplying Maven and Gradle builds with your GPG key information to sign artifacts.

The table below outlines which secrets/variables are required for the build of the different modules:

| Module | Build tool | Secrets/variables required |
| --- | --- | --- |
| platform | Gradle | `GPG_KEY`, `GPG_KEYID`, `GPG_PASSPHRASE` |
| buildutils | Go and Docker | `WRITE_GITHUB_PACKAGES_USERNAME`, `WRITE_GITHUB_PACKAGES_TOKEN` |
| wrapping | Maven | `GPG_KEY`, `GPG_KEYID`, `GPG_PASSPHRASE` |
| gradle | Gradle | `GPG_KEY`, `GPG_KEYID`, `GPG_PASSPHRASE` |
| maven | Maven | `GPG_KEY`, `GPG_KEYID`, `GPG_PASSPHRASE` |
| framework | Gradle | `GPG_KEY`, `GPG_KEYID`, `GPG_PASSPHRASE`, `WRITE_GITHUB_PACKAGES_USERNAME`, `WRITE_GITHUB_PACKAGES_TOKEN` |
| extensions | Gradle | `GPG_KEY`, `GPG_KEYID`, `GPG_PASSPHRASE` |
| managers | Gradle | `GPG_KEY`, `GPG_KEYID`, `GPG_PASSPHRASE` |
| obr | Maven | `GPG_KEY`, `GPG_KEYID`, `GPG_PASSPHRASE`, `WRITE_GITHUB_PACKAGES_USERNAME`, `WRITE_GITHUB_PACKAGES_TOKEN` |
| ivts | Gradle | `GPG_KEY`, `GPG_KEYID`, `GPG_PASSPHRASE`, `WRITE_GITHUB_PACKAGES_USERNAME`, `WRITE_GITHUB_PACKAGES_TOKEN` |
| cli | Go and Docker | `WRITE_GITHUB_PACKAGES_USERNAME`, `WRITE_GITHUB_PACKAGES_TOKEN` |

#### How to set repository variables:

1. Navigate to your repository settings.
1. Select 'Secrets and variables', then 'Actions' from the menu.
1. Click the 'Variables' tab.
1. Select 'New repository variable'.
1. Enter the variable name and value, then click 'Add variable'.

WRITE_GITHUB_PACKAGES_USERNAME is the only repository variable required.

1. WRITE_GITHUB_PACKAGES_USERNAME: This requires your GitHub username so you can authenticate to GitHub Container Registry and push images to your personal GitHub Packages. Enter your GitHub username in the value for this variable.

#### How to set repository secrets:

1. Navigate to your repository settings.
1. Select 'Secrets and variables', then 'Actions' from the menu.
1. Click the 'Secrets' tab.
1. Select 'New repository secret'.
1. Enter the secret name and value, then click 'Add secret'. Once you have done this, you will not be able to view the secret value again.

##### Required repository secrets

1. GPG_KEY: This requires your Base 64 encoded GPG key payload.
1. GPG_KEYID: This requires the ID of your GPG key in plain text.
1. GPG_PASSPHRASE: This requires your passphrase for your GPG key in plain text.
1. WRITE_GITHUB_PACKAGES_TOKEN: This requires your GitHub Personal Access Token with write:packages scope that you want to use to log into GitHub Container Registry.

**Note:** The GitHub web interface allows the setting of secrets, but you can't read them back, so be very careful what you set them to so you avoid some painful problem diagnosis steps later.

For the GPG related secrets, you can either use an existing key, or create a new key.

**Note:** The Galasa build requires a GNU Privacy Guard (GPG) key to sign built artifacts so it can be proven who built them. GitHub also requires a GPG key if you plan on signing commits (see how to set that up on GitHub's documentation). These two GPG keys can be the same if you wish, but they can be separate and different.

Here we explain how to create a new key from scratch.

First you will need to make sure the `gpg` command is installed on your system.

###### How to create a new GPG key:
1. Generate a new GPG key with `gpg --full-generate-key`
2. Select the type of key using the following options:
    * RSA and RSA (default)
    * 4096 bits long
    * Select an expiration date, for example, `1y`
3. Enter your User ID for the key:
    * Name: Your full name
    * Email: Your email address
    * Comment: N/A
4. Enter a passphrase, and ensure to remember it or note it down (in a secure place), as you will need it later.

###### How to set the GPG_PASSPHRASE repository secret: 

1. Get the passphrase for your GPG key in plain text.
1. If you have forgotten the passphrase for your GPG key, unfortunately you will not be able to retrieve the passphrase, so follow the steps to create a new key in [How to create a new GPG key](#how-to-create-a-new-gpg-key).
1. Add it to your repository secrets for GPG_PASSPHRASE.

###### How to set the GPG_KEYID repository secret: 

1. Get your GPG key information with `gpg --list-secret-keys --keyid-format=short`. The output will contain something like the block below. In this example, your GPG key ID is `XXXXXXXX`. 
```
sec   rsa4096/XXXXXXXX 2023-05-22 [SC] [expires: 2025-05-21]
      123456789101112131415161718192021222324252627282930
```
2. Create a repository secret called `GPG_KEYID` and add the value of the GPG key ID in plain text.

###### How to set the GPG_KEY repository secret: 

1. Use your GPG key ID and GPG passphrase from above to get your GPG key payload in a Base 64 encoded format with `gpg --export-secret-keys XXXXXXXX | base64`. You will be prompted for the passphrase that you noted earlier. Ensure the output is on one line.
1. Add the output to your repository secrets for GPG_KEY.

###### How to set the WRITE_GITHUB_PACKAGES_TOKEN repository secret: 

You will first need to create a new GitHub Personal access token (classic) or Fine-grained personal access token.

To create a GitHub Personal access token (classic):
1. Go to the Settings for your GitHub account > Developer settings > Personal access tokens > Tokens (classic). 
1. Select Generate new token (classic).
1. Give the token a name, such as `token to build personal forks of Galasa`.
1. Select an appropriate expiration time. For example `custom` in a years' time.
1. Select the `write:packages` access.
1. Press the "Generate Token" button at the bottom of the dialog.
1. Copy the token as it will disappear, and add this to your repository secrets for WRITE_GITHUB_PACKAGES_TOKEN.

<!-- To create a Fine-grained personal access token:
1. (TO DO)
1. ... add this to your repository secrets for WRITE_GITHUB_PACKAGES_TOKEN. -->

### Manually kick-off the first `Main Build Orchestrator` workflow

To prime build caches with downloaded dependencies, and to check that your variables and secrets are correctly configured, you will need to manually trigger the "Main Build Orchestrator" workflow.

1. Navigate to the Actions tab.
1. Select "Main Build Orchestrator" from the list of workflows on the left (you may need to click "Show more workflows...").
1. Click the "Run workflow" button on the right.

The "Main Build Orchestrator" workflow should take around 15-20 minutes to complete as it builds many modules of Galasa, several of them sequentially as they have dependencies on eachother.

If the workflow run is successful, you should see a Status of "Success" with all the workflow jobs completed successfully.

#### How to deal with `Main Build Orchestrator` failures

If the workflow run is a failure, you will see a Status of "Failure" for the workflow and a failure icon indicating which workflow job(s) and step(s) caused the failure.

##### Secret/variables related failures

If the repository secrets and variables are not configured at all, the "Main Build Orchestrator" will fail in the first job "Check required secrets configured" which checks for the presence of the required secrets. If the secrets and variables are configured, but the values are incorrect, the "Main Build Orchestrator" could fail in any of the subsequent jobs that attempt to use them. See the table in the [Configure repository secrets and variables](#configure-repository-secrets-and-variables) section for details on the secrets each module requires.

1. Follow the steps again in the [Configure repository secrets and variables](#configure-repository-secrets-and-variables) section.
1. Navigate to the failed workflow run of the "Main Build Orchestrator".
1. Click the "Re-run jobs" dropdown.
1. Select "Re-run failed jobs".

##### Other failures

If the workflow run fails for another reason, use the failure icons to identify the failed workflow step, and go to the logs for the step to try to diagnose the cause of the failure.

### Clone the forked repository and make changes
1. Clone your forked repository to your machine:
```
git clone https://github.com/YOUR-USERNAME/galasa.git
```
2. Make your changes and ensure they build locally with the `/tools/build-locally.sh` script and that the unit tests pass.

### Contribute code back to the project
1. Add the original 'galasa' repository, `upstream`, as a remote, and ensure you cannot push to it:
```
# replace <upstream git repo> with the upstream repo URL
# example:
#  https://github.com/galasa-dev/galasa.git
#  git@github.com/galasa-dev/galasa.git

git remote add upstream <upstream git repo>
git remote set-url --push upstream no_push
```
2. Verify this step by listing your configured remote repositories:
```
git remote -v
```
3. Create a new branch for your contribution:
```
git checkout -b issue-number/contribution-description
```
4. Make your changes and commit them, ensuring to DCO and GPG sign your commits:
```
git commit -s -S -m "Add a meaningful commit message"
```
5. Push your changes to your fork:
```
git push origin issue-number/contribution-description
```
6. Open a pull request from your forked repository branch to the main branch of the 'galasa repository', and explain your changes.
