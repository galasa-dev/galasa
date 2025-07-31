---
title: "Managing credentials in an Ecosystem"
---

It is likely that a test will need to pass credentials to the application being tested. For example, as HTTP credentials or as username and password values entered onto a 3270 screen. In a Galasa Ecosystem the credentials store, which is hosted in the etcd server, securely provides the credentials (for example, password, username, and personal access token) that are required for a test to run in automation.  

You can set a Username, UsernamePassword, Token, or UsernameToken secret in the credentials store by using the Galasa CLI tool's `secrets` commands. The ability to set these properties means that you can supply test cases with the credentials and tokens that they need to run.

The following examples show how you can update the credentials store, and will refer to credentials as secrets. The example commands that are provided in the following sections assume that the `GALASA_BOOTSTRAP` environment variable is set, so the `--bootstrap` flag is not required in the command.

## Creating and updating secrets

The `galasactl secrets set` command can be used to create or update secrets in the credentials store. The command's `--username`, `--password`, and `--token` flags can be used in different combinations to create different types of secret.

Optionally, a description can be provided when setting secrets using the `--description` flag to provide useful information as to what the secrets are used for.

For example, a UsernamePassword secret can be created by supplying `--username` and `--password`:

```shell
galasactl secrets set --name SYSTEM1 --username "my-username" --password "my-password" --description "an example secret"
```

A UsernameToken secret can be created by supplying `--username` and `--token`:

```shell
galasactl secrets set --name SYSTEM1 --username "my-username" --token "my-token"
```

A Token secret can be created by supplying `--token` on its own:

```shell
galasactl secrets set --name SYSTEM1 --token "my-token"
```

A Username secret can be created by supplying `--username` on its own:

```shell
galasactl secrets set --name SYSTEM1 --username "my-username"
```

Base64-encoded values can be supplied using the `--base64-username`, `--base64-password`, and `--base64-token` flags. These base64 flags can be used with the non-encoded flags described above to supply values in different encoding schemes. However, you cannot provide the same value in multiple encoding schemes (e.g. using `--base64-username` and `--username` in the same command).

For example, to create a UsernamePassword secret where both the username and password are base64-encoded:

```shell
galasactl secrets set --name SYSTEM1 --base64-username "my-base64-username" --base64-password "my-base64-password"
```

To create a UsernameToken secret where only the token is base64-encoded:

```shell
galasactl secrets set --name SYSTEM1 --username "my-base64-username" --base64-token "my-base64-token"
```

Once a secret has been created, you can change the type of the secret by supplying your desired secret type using the `--type` flag. When supplying the `--type` flag, all values for the new secret type must be provided. To find out what secret types are supported, run `galasactl secrets set --help`.

For example, to create a UsernamePassword secret and then change it to a Token secret:

```shell
galasactl secrets set --name SYSTEM1 --username "my-username" --password "my-password"
galasactl secrets set --name SYSTEM1 --token "my-token" --type Token
```

Updated credentials are now available for a test to run in automation on a Galasa Ecosystem.

For a complete list of supported parameters, see the [galasactl secrets set](../reference/cli-syntax/galasactl_secrets_set.md) command reference.


## Getting secrets

You can use the `galasactl secrets get` command to get secrets stored in the credentials store to verify that the secrets exist and are populated correctly. You can also filter results to retrieve a specific secret by providing its name in `galasactl secrets get` commands.

The following table shows the available output formats that can be provided as part of the `galasactl secrets get` command:

| Name |  Description  |
| :---- | :-------- | 
| `--format summary` | The default format is _summary_. Summary format is useful if you need a quick, high-level overview. If you omit the `--format` flag in the command, results are returned in summary format. You can set the summary format explicitly by setting the `--format summary` flag in the `galasactl secrets get` command.   | 
| `--format yaml` |  The results from `galasactl secrets get` are returned as GalasaSecret resources in YAML format. This YAML content can then be used in `galasactl resources` commands to create, update, and delete secrets using a YAML file. See [Configuring an Ecosystem using resource files](./ecosystem-manage-resources.md) for more details.|

For example, you can use the following command to retrieve all secrets in `yaml` format:

```shell
galasactl secrets get --format yaml
```

For a complete list of supported parameters, see the [galasactl secrets get](../reference/cli-syntax/galasactl_secrets_get.md) command reference.


### Getting all secrets

To retrieve all secrets stored in a Galasa Ecosystem, run the following command:

```shell
galasactl secrets get
```

This will display the retrieved secrets in a summary format:

```
name    type             last-updated(UTC)   last-updated-by description
SIMBANK UsernamePassword 2024-11-01 10:43:06 galasa-user     credentials for simbank
SYSTEM2 Token            2024-11-01 10:43:41 galasa-user2    example access token
SYSTEM1 Username         2024-11-01 10:42:46 galasa-user3    example secret

Total:3
```

### Get a secret by name

To retrieve a secret with a specific name, use the following example command:

```shell
galasactl secrets get --name {mysecret}
```

where `{mysecret}` is the name of the secret that you want to get.

For example, to view a secret named `SYSTEM1`, run the following command:

```shell
galasactl secrets get --name SYSTEM1
```

The following example shows the returned secret:

```
name    type             description       last-updated(UTC)   last-updated-by
SYSTEM1 UsernamePassword an example secret 2024-10-30 16:23:49 galasa-user

Total:1
```

## Deleting secrets

When a secret is no longer used or required by tests, it can be deleted from the Galasa Ecosystem using the `galasactl secrets delete` command, supplying the name of the secret as part of the command.

To delete a specific secret, use the following command:

```shell
galasactl secrets delete --name {mysecret}
```

where:
`{mysecret}` is the name of the secret that you want to delete.


For example, to delete a secret called `SIMBANK`, run the following command:

```shell
galasactl secrets delete --name SIMBANK
```

If the secret does not exist, an error will be displayed to indicate that the secret could not be found.

For a complete list of supported parameters, see the [galasactl secrets delete](../reference/cli-syntax/galasactl_secrets_delete.md) command reference.
