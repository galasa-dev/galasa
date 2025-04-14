## galasactl resources

Manages resources in an ecosystem

### Synopsis

Allows interaction with the Resources endpoint to create and maintain resources in the Galasa Ecosystem

### Options

```
  -b, --bootstrap string                      Bootstrap URL. Should start with 'http://' or 'file://'. If it starts with neither, it is assumed to be a fully-qualified path. If missing, it defaults to use the 'bootstrap.properties' file in your GALASA_HOME. Example: http://example.com/bootstrap, file:///user/myuserid/.galasa/bootstrap.properties , file://C:/Users/myuserid/.galasa/bootstrap.properties
  -f, --file string                           The file containing yaml definitions of resources to be applied manipulated by this command. This can be a fully-qualified path or path relative to the current directory.Example: my_resources.yaml
  -h, --help                                  Displays the options for the 'resources' command.
      --rate-limit-retries int                The maximum number of retries that should be made when requests to the Galasa Service fail due to rate limits being exceeded. Must be a whole number. Defaults to 3 retries (default 3)
      --rate-limit-retry-backoff-secs float   The amount of time in seconds to wait before retrying a command if it failed due to rate limits being exceeded. Defaults to 1 second. (default 1)
```

### Options inherited from parent commands

```
      --galasahome string   Path to a folder where Galasa will read and write files and configuration settings. The default is '${HOME}/.galasa'. This overrides the GALASA_HOME environment variable which may be set instead.
  -l, --log string          File to which log information will be sent. Any folder referred to must exist. An existing file will be overwritten. Specify "-" to log to stderr. Defaults to not logging.
```

### SEE ALSO

* [galasactl](galasactl.md)	 - CLI for Galasa
* [galasactl resources apply](galasactl_resources_apply.md)	 - Apply file contents to the ecosystem.
* [galasactl resources create](galasactl_resources_create.md)	 - Update Galasa Ecosystem resources.
* [galasactl resources delete](galasactl_resources_delete.md)	 - Delete Galasa Ecosystem resources.
* [galasactl resources update](galasactl_resources_update.md)	 - Update Galasa Ecosystem resources.

