## galasactl local init

Initialises Galasa home folder

### Synopsis

Initialises Galasa home folder in home directory with all the properties files

```
galasactl local init [flags]
```

### Options

```
      --development   Use bleeding-edge galasa versions and repositories.
  -h, --help          Displays the options for the 'local init' command.
```

### Options inherited from parent commands

```
      --galasahome string   Path to a folder where Galasa will read and write files and configuration settings. The default is '${HOME}/.galasa'. This overrides the GALASA_HOME environment variable which may be set instead.
  -l, --log string          File to which log information will be sent. Any folder referred to must exist. An existing file will be overwritten. Specify "-" to log to stderr. Defaults to not logging.
```

### SEE ALSO

* [galasactl local](galasactl_local.md)	 - Manipulate local system

