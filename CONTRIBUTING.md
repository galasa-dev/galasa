# Contributing

Welcome to Galasa! To learn more about contributing to the [galasa-dev repositories](https://github.com/galasa-dev), please read this Contributor's Guide.

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

## Making your first contribution

### How to set up a fork of a repository

1. Fork the repository using the GitHub UI
1. You will need to enable the GitHub Actions workflows by going to the Actions tab and clicking 'I understand my workflows, go ahead and enable them'
1. You will need to trigger an initial workflow run of the 'Main Build Orchestrator' before making any Pull Requests from a branch to your main branch on your fork. You can do this by going to the Actions tab, clicking 'Main Build Orchestrator', and submitting 'Run workflow'. (todo: expand on this, explain about how the artifact caching works)

### How to configure repository secrets and variables

To run certain workflows on your fork, you will need to configure some repository secrets and variables so that the workflows can access them. (todo: expand...)

#### Repository secrets and variables that required configuring

##### Repository variables

To set a repository variable:
1. Navigate to your repository settings.
1. Select 'Secrets and variables', then 'Actions' from the menu.
1. Click the 'Variables' tab.
1. Select 'New repository variable'.
1. Enter the variable name and value, then click 'Add variable'.

**Required repository variables:**

1. WRITE_GITHUB_PACKAGES_USERNAME: This requires the GitHub username you want to use to log into GitHub Container Registry.

##### Repository secrets

To set a repository variable:
1. Navigate to your repository settings.
1. Select 'Secrets and variables', then 'Actions' from the menu.
1. Click the 'Secrets' tab.
1. Select 'New repository secret'.
1. Enter the secret name and value, then click 'Add secret'. Once you have done this, you will not be able to view the secret value again.

**Required repository secrets:**

1. GPG_KEY: This requires your Base 64 encoded GPG key payload.
1. GPG_KEYID: This requires the ID of your GPG key in plain text.
1. GPG_PASSPHRASE: This requires your passphrase for your GPG key in plain text.
1. WRITE_GITHUB_PACKAGES_TOKEN: This requires your GitHub Personal Access Token with write:packages scope that you want to use to log into GitHub Container Registry.

For the GPG related secrets, you can either use an existing key, or create a new key.

**How to create a new GPG key**:
1. Generate a new GPG key with `gpg --full-generate-key`
2. Select the type of key using the following options:
    * RSA and RSA (default)
    * 4096 bits long
    * Select an expiration date
3. Enter your User ID for the key:
    * Name: Your full name
    * Email: Your email address
    * Comment: N/A
4. Enter a password, and ensure to remember it or note it down, as you will need it later.

**How to set the GPG_KEYID repository secret**: 

1. Get your GPG key information with `gpg --list-secret-keys --keyid-format=short`. The output will contain something like the block below. In this example, your GPG key ID is `XXXXXXXX`. 
```
sec   rsa4096/XXXXXXXX 2023-05-22 [SC] [expires: 2025-05-21]
      123456789101112131415161718192021222324252627282930
```
2. Create a repository secret called `GPG_KEYID` and add the value of the GPG key ID in plain text.


**How to set the GPG_KEY repository secret**: 

1. Use your GPG key ID from above to get your GPG key payload in a Base 64 encoded format with `gpg --export-secret-keys XXXXXXXX | base64`. Ensure it is on one line.
1. Add the output to your repository secrets for GPG_KEY.

**How to set the GPG_PASSPHRASE repository secret**: 

1. Get the passphrase for your GPG key in plain text.
1. If you have forgotten the passphrase for your GPG key, (todo: explain how to find lost password or explain how to create new key...)
1. Add it to your repository secrets for GPG_PASSPHRASE.

**How to set the WRITE_GITHUB_PACKAGES_TOKEN repository secret**: 

You will first need to create a new GitHub Personal access token (classic) or Fine-grained personal access token.

To create a GitHub Personal access token (classic):
1. Go to the Settings for your GitHub account > Developer settings > Personal access tokens > Tokens (classic). 
1. Select Generate new token (classic).
1. Select write:packages access and give the token a name.
1. Copy the token as it will disappear, and add this to your repository secrets for WRITE_GITHUB_PACKAGES_TOKEN.

To create a Fine-grained personal access token:
1. TO DO
1. ... add this to your repository secrets for WRITE_GITHUB_PACKAGES_TOKEN.

### How to contribute code back to the project
1. Clone your fork of the repository locally (todo: does this need expanding on?)
1. Add `upstream` as a remote, and ensure you cannot push to it:
```
# replace <upstream git repo> with the upstream repo URL
# example:
#  https://github.com/galasa-dev/galasa.git
#  git@github.com/galasa-dev/galasa.git

git remote add upstream <upstream git repo>
git remote set-url --push upstream no_push
```
1. Verify this step by listing your configured remote repositories:
```
git remote -v
```
1. Create a new branch for your contribution:
```
git checkout -b issue-number/contribution-description
```
1. Make your changes and commit them, ensuring to DCO and GPG sign your commits:
```
git commit -s -S -m "Add a meaningful commit message"
```
1. Push your changes to your fork:
```
git push origin issue-number/contribution-description
```
1. Open a pull request and explain your changes.
