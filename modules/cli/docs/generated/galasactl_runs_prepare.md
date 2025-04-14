## galasactl runs prepare

prepares a list of tests

### Synopsis

Prepares a list of tests from a test catalog providing specific overrides if required

```
galasactl runs prepare [flags]
```

### Options

```
      --append             Append tests to existing portfolio
      --bundle strings     bundles of which tests will be selected from, bundles are selected if the name contains this string, or if --regex is specified then matches the regex
      --class strings      test class names to run from the specified stream or portfolio. The format of each entry is {osgi-bundle-name}/{java-class-name}.  Multiple values can be supplied using a comma-separated list of values, or by using multiple instances of the --class flag. Java class names are fully qualified. No .class suffix is needed.
      --gherkin strings    Gherkin feature file URL. Should start with 'file://'. 
  -h, --help               Displays the options for the 'runs prepare' command.
      --override strings   overrides to be sent with the tests (overrides in the portfolio will take precedence)
      --package strings    packages of which tests will be selected from, packages are selected if the name contains this string, or if --regex is specified then matches the regex
  -p, --portfolio string   portfolio to add tests to
      --regex              Test selection is performed by using regex
  -s, --stream string      test stream to extract the tests from
      --tag strings        tags of which tests will be selected from, tags are selected if the name contains this string, or if --regex is specified then matches the regex
      --test strings       test names which will be selected if the name contains this string, or if --regex is specified then matches the regex
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

