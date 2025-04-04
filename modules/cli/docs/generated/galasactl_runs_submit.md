## galasactl runs submit

submit a list of tests to the ecosystem

### Synopsis

Submit a list of tests to the ecosystem, monitor them and wait for them to complete

```
galasactl runs submit [flags]
```

### Options

```
      --bundle strings             bundles of which tests will be selected from, bundles are selected if the name contains this string, or if --regex is specified then matches the regex
      --class strings              test class names to run from the specified stream or portfolio. The format of each entry is {osgi-bundle-name}/{java-class-name}.  Multiple values can be supplied using a comma-separated list of values, or by using multiple instances of the --class flag. Java class names are fully qualified. No .class suffix is needed.
      --gherkin strings            Gherkin feature file URL. Should start with 'file://'. 
  -g, --group string               the group name to assign the test runs to, if not provided, a psuedo unique id will be generated
  -h, --help                       Displays the options for the 'runs submit' command.
      --noexitcodeontestfailures   set to true if you don't want an exit code to be returned from galasactl if a test fails
      --override strings           overrides to be sent with the tests (overrides in the portfolio will take precedence). Each override is of the form 'name=value'. Multiple instances of this flag can be used. For example --override=prop1=val1 --override=prop2=val2
      --overridefile strings       path to a properties file containing override properties. Defaults to overrides.properties in galasa home folder if that file exists. Overrides from --override options will take precedence over properties in this property file. A file path of '-' disables reading any properties file. To use multiple override files, either repeat the overridefile flag for each file, or list the path (absolute or relative) of each override file, separated by commas. For example --overridefile file.properties --overridefile /Users/dummyUser/code/test.properties or --overridefile file.properties,/Users/dummyUser/code/test.properties. The files are processed in the order given. When a property is be defined in multiple files, the last occurrence processed will have its value used.
      --package strings            packages of which tests will be selected from, packages are selected if the name contains this string, or if --regex is specified then matches the regex
      --poll int                   Optional. The interval time in seconds between successive polls of the test runs status. Defaults to 30 seconds. If less than 1, then default value is used. (default 30)
  -p, --portfolio string           portfolio containing the tests to run
      --progress int               in minutes, how often the cli will report the overall progress of the test runs. A value of 0 or less disables progress reporting. (default 5)
      --regex                      Test selection is performed by using regex
      --reportjson string          json file to record the final results in
      --reportjunit string         junit xml file to record the final results in
      --reportyaml string          yaml file to record the final results in
      --requesttype string         the type of request, used to allocate a run name. Defaults to CLI. (default "CLI")
  -s, --stream string              test stream to extract the tests from
      --tag strings                tags of which tests will be selected from, tags are selected if the name contains this string, or if --regex is specified then matches the regex
      --test strings               test names which will be selected if the name contains this string, or if --regex is specified then matches the regex
      --throttle int               how many test runs can be submitted in parallel, 0 or less will disable throttling. 1 causes tests to be run sequentially. (default 3)
      --throttlefile string        a file where the current throttle is stored. Periodically the throttle value is read from the file used. Someone with edit access to the file can change it which dynamically takes effect. Long-running large portfolios can be throttled back to nothing (paused) using this mechanism (if throttle is set to 0). And they can be resumed (un-paused) if the value is set back. This facility can allow the tests to not show a failure when the system under test is taken out of service for maintainence.Optional. If not specified, no throttle file is used.
      --trace                      Trace to be enabled on the test runs
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
* [galasactl runs submit local](galasactl_runs_submit_local.md)	 - submit a list of tests to be run on a local java virtual machine (JVM)

