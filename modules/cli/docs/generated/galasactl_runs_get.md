## galasactl runs get

Get the details of a test runname which ran or is running.

### Synopsis

Get the details of a test runname which ran or is running, displaying the results to the caller.

```
galasactl runs get [flags]
```

### Options

```
      --active             parameter to retrieve runs that have not finished yet. Cannot be used in conjunction with --name or --result flag.
      --age string         the age of the test run(s) we want information about. Supported formats are: 'FROM' or 'FROM:TO', where FROM and TO are each ages, made up of an integer and a time-unit qualifier. Supported time-units are 'w' (weeks), 'd' (days), 'h' (hours), 'm' (minutes). If missing, the TO part is defaulted to '0h'. Examples: '--age 1d', '--age 6h:1h' (list test runs which happened from 6 hours ago to 1 hour ago). The TO part must be a smaller time-span than the FROM part.
      --format string      output format for the data returned. Supported formats are: 'details', 'raw', 'summary'. (default "summary")
      --group string       the name of the group to return tests under that group. Cannot be used in conjunction with --name
  -h, --help               Displays the options for the 'runs get' command.
      --name string        the name of the test run we want information about. Cannot be used in conjunction with --requestor, --result or --active flags
      --requestor string   the requestor of the test run we want information about. Cannot be used in conjunction with --name flag.
      --result string      A filter on the test runs we want information about. Optional. Default is to display test runs with any result. Case insensitive. Value can be a single value or a comma-separated list. For example "--result Failed,Ignored,EnvFail". Cannot be used in conjunction with --name or --active flag.
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

* [galasactl runs](galasactl_runs.md)	 - Manage test runs in the ecosystem

