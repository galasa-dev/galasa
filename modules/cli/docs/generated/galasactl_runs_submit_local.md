## galasactl runs submit local

submit a list of tests to be run on a local java virtual machine (JVM)

### Synopsis

Submit a list of tests to a local JVM, monitor them and wait for them to complete

```
galasactl runs submit local [flags]
```

### Options

```
      --class strings          test class names. The format of each entry is osgi-bundle-name/java-class-name. Java class names are fully qualified. No .class suffix is needed.
      --debug                  When set (or true) the debugger pauses on startup and tries to connect to a Java debugger. The connection is established using the --debugMode and --debugPort values.
      --debugMode string       The mode to use when the --debug option causes the testcase to connect to a Java debugger. Valid values are 'listen' or 'attach'. 'listen' means the testcase JVM will pause on startup, waiting for the Java debugger to connect to the debug port (see the --debugPort option). 'attach' means the testcase JVM will pause on startup, trying to attach to a java debugger which is listening on the debug port. The default value is 'listen' but can be overridden by the 'galasactl.jvm.local.launch.debug.mode' property in the bootstrap file, which in turn can be overridden by this explicit parameter on the galasactl command.
      --debugPort uint32       The port to use when the --debug option causes the testcase to connect to a java debugger. The default value used is 2970 which can be overridden by the 'galasactl.jvm.local.launch.debug.port' property in the bootstrap file, which in turn can be overridden by this explicit parameter on the galasactl command.
      --galasaVersion string   the version of galasa you want to use to run your tests. This should match the version of the galasa obr you built your test bundles against. (default "0.42.0")
      --gherkin strings        Gherkin feature file URL. Should start with 'file://'. 
  -h, --help                   Displays the options for the 'runs submit local' command.
      --localMaven string      The url of a local maven repository are where galasa bundles can be loaded from on your local file system. Defaults to your home .m2/repository file. Please note that this should be in a URL form e.g. 'file:///Users/myuserid/.m2/repository', or 'file://C:/Users/myuserid/.m2/repository'
      --obr strings            The maven coordinates of the obr bundle(s) which refer to your test bundles. The format of this parameter is 'mvn:${TEST_OBR_GROUP_ID}/${TEST_OBR_ARTIFACT_ID}/${TEST_OBR_VERSION}/obr' Multiple instances of this flag can be used to describe multiple obr bundles.
      --remoteMaven string     the url of the remote maven where galasa bundles can be loaded from. Defaults to maven central. (default "https://repo.maven.apache.org/maven2")
```

### Options inherited from parent commands

```
  -b, --bootstrap string                      Bootstrap URL. Should start with 'http://' or 'file://'. If it starts with neither, it is assumed to be a fully-qualified path. If missing, it defaults to use the 'bootstrap.properties' file in your GALASA_HOME. Example: http://example.com/bootstrap, file:///user/myuserid/.galasa/bootstrap.properties , file://C:/Users/myuserid/.galasa/bootstrap.properties
      --galasahome string                     Path to a folder where Galasa will read and write files and configuration settings. The default is '${HOME}/.galasa'. This overrides the GALASA_HOME environment variable which may be set instead.
  -g, --group string                          the group name to assign the test runs to, if not provided, a psuedo unique id will be generated
  -l, --log string                            File to which log information will be sent. Any folder referred to must exist. An existing file will be overwritten. Specify "-" to log to stderr. Defaults to not logging.
      --noexitcodeontestfailures              set to true if you don't want an exit code to be returned from galasactl if a test fails
      --override strings                      overrides to be sent with the tests (overrides in the portfolio will take precedence). Each override is of the form 'name=value'. Multiple instances of this flag can be used. For example --override=prop1=val1 --override=prop2=val2
      --overridefile strings                  path to a properties file containing override properties. Defaults to overrides.properties in galasa home folder if that file exists. Overrides from --override options will take precedence over properties in this property file. A file path of '-' disables reading any properties file. To use multiple override files, either repeat the overridefile flag for each file, or list the path (absolute or relative) of each override file, separated by commas. For example --overridefile file.properties --overridefile /Users/dummyUser/code/test.properties or --overridefile file.properties,/Users/dummyUser/code/test.properties. The files are processed in the order given. When a property is be defined in multiple files, the last occurrence processed will have its value used.
      --poll int                              Optional. The interval time in seconds between successive polls of the test runs status. Defaults to 30 seconds. If less than 1, then default value is used. (default 30)
      --progress int                          in minutes, how often the cli will report the overall progress of the test runs. A value of 0 or less disables progress reporting. (default 5)
      --rate-limit-retries int                The maximum number of retries that should be made when requests to the Galasa Service fail due to rate limits being exceeded. Must be a whole number. Defaults to 3 retries (default 3)
      --rate-limit-retry-backoff-secs float   The amount of time in seconds to wait before retrying a command if it failed due to rate limits being exceeded. Defaults to 1 second. (default 1)
      --reportjson string                     json file to record the final results in
      --reportjunit string                    junit xml file to record the final results in
      --reportyaml string                     yaml file to record the final results in
      --requesttype string                    the type of request, used to allocate a run name. Defaults to CLI. (default "CLI")
      --tags strings                          tagging metadata to be associated with the submitted runs. This can be specified as a comma-separated list of tag strings, or by repeating the --tags parameter, or both.
      --throttle int                          how many test runs can be submitted in parallel, 0 or less will disable throttling. 1 causes tests to be run sequentially. (default 3)
      --throttlefile string                   a file where the current throttle is stored. Periodically the throttle value is read from the file used. Someone with edit access to the file can change it which dynamically takes effect. Long-running large portfolios can be throttled back to nothing (paused) using this mechanism (if throttle is set to 0). And they can be resumed (un-paused) if the value is set back. This facility can allow the tests to not show a failure when the system under test is taken out of service for maintainence.Optional. If not specified, no throttle file is used.
      --trace                                 Trace to be enabled on the test runs
```

### SEE ALSO

* [galasactl runs submit](galasactl_runs_submit.md)	 - submit a list of tests to the ecosystem

