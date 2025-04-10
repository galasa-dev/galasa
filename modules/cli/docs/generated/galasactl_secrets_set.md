## galasactl secrets set

Creates or updates a secret in the credentials store

### Synopsis

Creates or updates a secret in the credentials store

```
galasactl secrets set [flags]
```

### Options

```
      --base64-password string   a base64-encoded password to set into a secret
      --base64-token string      a base64-encoded token to set into a secret
      --base64-username string   a base64-encoded username to set into a secret
      --description string       the description to associate with the secret being created or updated
  -h, --help                     Displays the options for the 'secrets set' command.
      --name string              A mandatory flag that identifies the secret to be created or manipulated.
      --password string          a password to set into a secret
      --token string             a token to set into a secret
      --type string              the desired secret type to convert an existing secret into. Supported types are: [UsernamePassword Username UsernameToken Token].
      --username string          a username to set into a secret
```

### Options inherited from parent commands

```
  -b, --bootstrap string                      Bootstrap URL. Should start with 'http://' or 'file://'. If it starts with neither, it is assumed to be a fully-qualified path. If missing, it defaults to use the 'bootstrap.properties' file in your GALASA_HOME. Example: http://example.com/bootstrap, file:///user/myuserid/.galasa/bootstrap.properties , file://C:/Users/myuserid/.galasa/bootstrap.properties
      --galasahome string                     Path to a folder where Galasa will read and write files and configuration settings. The default is '${HOME}/.galasa'. This overrides the GALASA_HOME environment variable which may be set instead.
  -l, --log string                            File to which log information will be sent. Any folder referred to must exist. An existing file will be overwritten. Specify "-" to log to stderr. Defaults to not logging.
      --rate-limit-retries int                The maximum number of retries that should be made when requests to the Galasa Service fail due to rate limits being exceeded. Must be a whole number. Defaults to 3 retries (default 3)
      --rate-limit-retry-backoff-secs float   The amount of time in seconds to wait before retrying a command if it failed due to rate limits being exceeded. Defaults to 1 second. (default 1)
```

### SEE ALSO

* [galasactl secrets](galasactl_secrets.md)	 - Manage secrets stored in the Galasa service's credentials store

