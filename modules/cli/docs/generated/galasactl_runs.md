## galasactl runs

Manage test runs in the ecosystem

### Synopsis

Assembles, submits and monitors test runs in Galasa Ecosystem

### Options

```
  -b, --bootstrap string                      Bootstrap URL. Should start with 'http://' or 'file://'. If it starts with neither, it is assumed to be a fully-qualified path. If missing, it defaults to use the 'bootstrap.properties' file in your GALASA_HOME. Example: http://example.com/bootstrap, file:///user/myuserid/.galasa/bootstrap.properties , file://C:/Users/myuserid/.galasa/bootstrap.properties
  -h, --help                                  Displays the options for the 'runs' command.
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
* [galasactl runs cancel](galasactl_runs_cancel.md)	 - cancel an active run in the ecosystem
* [galasactl runs delete](galasactl_runs_delete.md)	 - Delete a named test run.
* [galasactl runs download](galasactl_runs_download.md)	 - Download the artifacts of a test run which ran.
* [galasactl runs get](galasactl_runs_get.md)	 - Get the details of a test runname which ran or is running.
* [galasactl runs prepare](galasactl_runs_prepare.md)	 - prepares a list of tests
* [galasactl runs reset](galasactl_runs_reset.md)	 - reset an active run in the ecosystem
* [galasactl runs submit](galasactl_runs_submit.md)	 - submit a list of tests to the ecosystem

