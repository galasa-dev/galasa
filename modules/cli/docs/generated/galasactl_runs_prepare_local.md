## galasactl runs prepare local

download test bundle dependencies to a local Maven cache

### Synopsis

Downloads all OSGi bundle dependencies referenced by the provided OBRs to the local Maven cache without executing any tests. Run this command from a network-connected environment, then share the populated local Maven cache with restricted test runners. Use 'galasactl runs submit local --offline --localMaven <path>' to execute tests without any remote Maven access.

```
galasactl runs prepare local [flags]
```

### Options

```
      --galasaVersion string   the version of galasa you want to use to prepare dependencies. This should match the version of the galasa obr you built your test bundles against. (default "1.0.0")
  -h, --help                   Displays the options for the 'runs prepare local' command.
      --localMaven string      The url of a local maven repository where galasa bundles will be cached on your local file system. Defaults to your home .m2/repository file. Please note that this should be in a URL form e.g. 'file:///Users/myuserid/.m2/repository', or 'file://C:/Users/myuserid/.m2/repository'
      --obr strings            The maven coordinates of the obr bundle(s) whose dependencies should be downloaded. The format of this parameter is 'mvn:${TEST_OBR_GROUP_ID}/${TEST_OBR_ARTIFACT_ID}/${TEST_OBR_VERSION}/obr' Multiple instances of this flag can be used to describe multiple obr bundles.
      --remoteMaven string     the url of the remote maven where galasa bundles can be downloaded from. Defaults to maven central. (default "https://repo.maven.apache.org/maven2")
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

* [galasactl runs prepare](galasactl_runs_prepare.md)	 - prepares a list of tests

