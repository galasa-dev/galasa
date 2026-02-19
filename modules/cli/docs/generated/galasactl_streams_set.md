## galasactl streams set

Creates or updates a test stream in the Galasa service

### Synopsis

Creates or updates a test stream in the Galasa service

```
galasactl streams set [flags]
```

### Options

```
      --description string       the description to associate with the test stream being created or updated
  -h, --help                     Displays the options for the 'streams set' command.
      --maven-repo-url string    the URL to the Maven repository containing test material for the test stream to use. For example: https://my-maven-repository
      --name string              A mandatory field indicating the name of a test stream.
      --obr strings              The maven coordinates of the OBR bundle(s) which refer to your test bundles. The format of this parameter is 'mvn:{OBR_GROUP_ID}/{OBR_ARTIFACT_ID}/{OBR_VERSION}/obr'. Multiple instances of this flag can be used to describe multiple OBR bundles.
      --testcatalog-url string   the URL to the test catalog for the test stream being created or updated. For example: https://my-maven-repository/path/to/testcatalog.json
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

* [galasactl streams](galasactl_streams.md)	 - Manages test streams in a Galasa service

