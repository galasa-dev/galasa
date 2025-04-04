## galasactl properties set

Set the details of properties in a namespace.

### Synopsis

Set the details of a property in a namespace. If the property does not exist, a new property is created, otherwise the value for that property will be updated.

```
galasactl properties set [flags]
```

### Options

```
  -h, --help               Displays the options for the 'properties set' command.
  -n, --name string        A mandatory field indicating the name of a property in the namespace.The first character of the name must be in the 'a'-'z' or 'A'-'Z' ranges, and following characters can be 'a'-'z', 'A'-'Z', '0'-'9', '.' (period), '-' (dash) or '_' (underscore)
  -s, --namespace string   A mandatory flag that describes the container for a collection of properties.The first character of the namespace must be in the 'a'-'z' range, and following characters can be 'a'-'z' or '0'-'9'
  -v, --value string       A mandatory flag indicating the value of the property you want to create. Empty values and values with spaces must be put in quotation marks.
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

* [galasactl properties](galasactl_properties.md)	 - Manages properties in an ecosystem

