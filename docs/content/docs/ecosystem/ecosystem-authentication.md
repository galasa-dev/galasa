---
title: "Configuring authentication"
---

Before interacting with a Galasa Ecosystem using the Galasa command line tool (`galasactl`), you must be authenticated with it. Any `galasactl` command which connects to a remote Galasa service needs to know the address of that service. A URL can be configured in the  `GALASA_BOOTSTRAP` environment variable or it can be set on a per-command basis using the `--bootstrap` flag.

Once the `galasactl` tool knows which Galasa service to contact, it needs to be configured to be able to authenticate to that service.
To do that, the Galasa Web UI must be used to allocate a personal access token, which can be passed to the `galasactl` to be used.

A personal access tokens is stored in the `GALASA_TOKEN` property in the `galasactl.properties` file in your Galasa home folder, or in the `GALASA_TOKEN` environment variable. The `galasactl.properties` file is created when you run the `galasa local init` command. Setting the `GALASA_TOKEN` property in this file with a valid token value allows the galasactl tool to access and communicate with an Ecosystem on behalf of the user.

If you have [installed your Galasa Ecosystem](../ecosystem/ecosystem-installing-k8s) by using the Galasa Ecosystem Helm chart that is provided with Galasa, you will have access to the Galasa Web UI. To get a value for the `GALASA_TOKEN` property, log into the Galasa Web UI and request a personal access token (using the 'My Settings' page) which can be copied into the `GALASA_TOKEN` property. The instructions on how to do this are displayed in a dialog box in the Galasa Web UI. You can choose to set the token as an environmental variable but the value would not persist across terminals, so is only valid for that session.

## Authentication architecture

The following diagram shows the architecture for the authentication process:

![Galasa ecosystem architecture:](ecosystem-cluster-auth.svg)


When a user logs into the Galasa Web UI via their browser, the Web UI contacts the Galasa API server which in turn talks to a Dex server, providing it with the user ID. The Dex server talks to an identity provider, for example GitHub or LDAP, to authenticate that user. If the user is successfully authenticated, Dex returns a bearer token to the Galasa API server, which in turn passes the token to the Galasa Web UI. The bearer token is then stored in a cookie by the web browser, to be used in further interactions with the Galasa Web UI until the user logs out of the web application, or the token expires.

Once logged-in to the Web UI, the user can then create a new access token token (using the 'My Settings' page). This provides a secret access token which must be copied from the Web UI panel and made available to the `galasactl` command line tool. The access token can be placed in the `GALASA_TOKEN` property in the `galasactl.properties` file, or the `GALASA_TOKEN` environment variable.

The `galasactl` tool will login implicitly when it needs to contact the remote Galasa service. Or the login state can be explicitly controlled using the `galasactl auth login` and `galasactl auth logout` commands.

On a successful login using the `galasactl` tool, the `GALASA_TOKEN` will be used to create a new temporary bearer token, which is stored in the `bearer-tokens` folder in the Galasa home directory. This file contains a bearer token that galasactl uses to authenticate requests when communicating with a Galasa Ecosystem. If the bearer token expires, galasactl automatically attempts to re-authenticate with the Galasa Ecosystem using the configured `GALASA_TOKEN`.


### Logging in to a Galasa Ecosystem using the auth login command

You can log in to a Galasa Ecosystem explicitly by using the `galasactl auth login` command. You might want to do an explicit log in if you are running galasactl as part of a build pipeline, or if you just want to make sure you can log in.


### Logging out of a Galasa Ecosystem using the auth logout command

To log out of a Galasa Ecosystem using galasactl, you can use the `galasactl auth logout` command. If you run a galasactl command that interacts with an Ecosystem while logged out, galasactl will attempt to automatically log again in using the configured `GALASA_TOKEN`.


### Listing personal access tokens

You can retrieve a list of all active personal access tokens in the Ecosystem by using the `galasactl auth tokens get` command. This information is useful if you need to query active tokens with a view to revoking a particular token, for example, if a user moves to a new role, or loses a laptop with their access token on it.

The token ID, creation date, username, and description information is returned, as shown in the following example:

```console
> galasactl auth tokens get 
tokenid         created(YYYY/MM/DD)  user                description
09823128318238  2024-02-03           m.smith@gmail.com   Ecosystem1 access 
87a6s2y8hqwd27  2024-05-04           s_jones@gmail.com   CLI access from VSCode

Total:2
```

The returned token list is sorted in creation date order, with the earliest creation date first. The description information matches the description that is provided by the user when creating a new access token from the Galasa Web UI.

You can also get tokens for a specific user

```
> galasactl auth tokens get --user m.smith@gmail.com
tokenid         created(YYYY/MM/DD)  user                description
09823128318238  2024-02-03           m.smith@gmail.com   Ecosystem1 access 

Total:1
```

If you are unsure which user id you are currently using, you can find out with this command:
```
> galasactl users get --id me  
id: m.smith@gmail.com
```

### Revoking personal access tokens

If a user moves to a new role, or loses a laptop with their personal access token on it, you can log into the Galasa Ecosystem and revoke their access token by using the `galasactl auth tokens delete` command. This ensures that the Ecosystem and systems-under-test remain secure.

You can retrieve a list of available personal access tokens that have been created, along with their token IDs, by running the `galasactl auth tokens get` command, as described in the previous section. 


Run the following command to revoke a personal access token with the token ID `myId` substituted for the numeric value visible from the `galasactl auth tokens get` command.

```
galasactl auth tokens delete --tokenid {myId}
```

For example:
```
> galasactl auth tokens get --user m.smith@gmail.com
tokenid         created(YYYY/MM/DD)  user                description
09823128318238  2024-02-03           m.smith@gmail.com   Ecosystem1 access 

Total: 1
> galasactl auth tokens delete --tokenid 09823128318238
```

*Note:* The `galasactl auth tokens delete` command revokes personal access tokens that a user creates through the Galasa Web UI. When a user runs a CLI command that talks to the Ecosystem, the CLI uses the personal access token to get a JSON Web Token (JWT). A JWT is a separate, temporary token that identifies a user and is used in galasactl commands to talk to the API server. JWTs cannot be revoked, but they do expire, so a user can continue to run CLI commands after revoking their personal access token until their JWT expires. You can remove the JWT that is stored on a user's machine instead of having to wait for the JWT to expire, by running the `galasactl auth logout` command on that machine.

For more information about JWTs, see the `Configuring Dex` section in the [Installing an Ecosystem using Helm](ecosystem-installing-k8s) documentation.







