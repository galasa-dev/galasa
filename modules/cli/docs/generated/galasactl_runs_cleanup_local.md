## galasactl runs cleanup local

Runs resource cleanup providers once for resources provisioned by Galasa

### Synopsis

Runs resource cleanup providers loaded from provided OBRs for resources provisioned by Galasa as part of local test runs. The loaded providers are only run once and do not run as a daemon process. The providers that are loaded are determined by the patterns provided in the '--includes-pattern' and '--excludes-pattern' flags. By default, all cleanup providers in the provided OBRs will be loaded and none will be excluded.
Supported glob patterns include the following special characters:
'*' (wildcard) Matches zero or more characters.
'?' matches exactly one character
For example, the pattern 'dev.galasa*' will match any monitor that includes 'dev.galasa' as its prefix, so a provider like 'dev.galasa.core.CoreResourceMonitorClass' will be matched.

```
galasactl runs cleanup local [flags]
```

### Options

```
      --excludes-pattern strings   The glob pattern(s) representing the resource cleanup providers that should not be loaded. Supported glob patterns include the following special characters:
                                   '*' (wildcard) Matches zero or more characters.
                                   '?' matches exactly one character
                                   For example, the pattern '*MyResourceCleanupClass' will match any provider that ends with 'MyResourceCleanupClass' such as 'my.company.resources.MyResourceCleanupClass' and so that provider will not be loaded.
  -h, --help                       Displays the options for the 'runs cleanup local' command.
      --includes-pattern strings   The glob pattern(s) representing the resource cleanup providers that should be loaded. Supported glob patterns include the following special characters:
                                   '*' (wildcard) Matches zero or more characters.
                                   '?' matches exactly one character
                                   For example, the pattern 'dev.galasa*' will match any provider that includes 'dev.galasa' as its prefix, so a provider like 'dev.galasa.core.CoreResourceCleanupClass' will be matched. (default [*])
      --localMaven string          The url of a local maven repository are where galasa bundles can be loaded from on your local file system. Defaults to your home .m2/repository file. Please note that this should be in a URL form e.g. 'file:///Users/myuserid/.m2/repository', or 'file://C:/Users/myuserid/.m2/repository'
      --obr strings                The maven coordinates of the obr bundle(s) which refer to your resource cleanup bundles. The format of this parameter is 'mvn:${OBR_GROUP_ID}/${OBR_ARTIFACT_ID}/${OBR_VERSION}/obr' Multiple instances of this flag can be used to describe multiple obr bundles.
      --remoteMaven strings        the urls of the remote maven repositories where galasa bundles can be loaded from. Defaults to maven central. (default [https://repo.maven.apache.org/maven2])
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

* [galasactl runs cleanup](galasactl_runs_cleanup.md)	 - Run resource cleanup jobs for resources provisioned by Galasa

