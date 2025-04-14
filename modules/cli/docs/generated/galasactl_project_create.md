## galasactl project create

Creates a new Galasa project

### Synopsis

Creates a new Galasa test project with optional OBR project and build process files

```
galasactl project create [flags]
```

### Options

```
      --development       Use bleeding-edge galasa versions and repositories.
      --features string   A comma-separated list of features you are testing. These must be able to form parts of a java package name. For example: "payee,account" (default "feature1")
      --force             Force-overwrite files which already exist.
      --gradle            Generate gradle build artifacts. Can be used in addition to the --maven flag.
  -h, --help              Displays the options for the 'project create' command.
      --maven             Generate maven build artifacts. Can be used in addition to the --gradle flag. If this flag is not used, and the gradle option is not used, then behaviour of this flag defaults to true.
      --obr               An OSGi Object Bundle Resource (OBR) project is needed.
      --package string    Java package name for tests we create. Forms part of the project name, maven/gradle group/artifact ID, and OSGi bundle name. It may reflect the name of your organisation or company, the department, function or application under test. For example: dev.galasa.banking.example
```

### Options inherited from parent commands

```
      --galasahome string   Path to a folder where Galasa will read and write files and configuration settings. The default is '${HOME}/.galasa'. This overrides the GALASA_HOME environment variable which may be set instead.
  -l, --log string          File to which log information will be sent. Any folder referred to must exist. An existing file will be overwritten. Specify "-" to log to stderr. Defaults to not logging.
```

### SEE ALSO

* [galasactl project](galasactl_project.md)	 - Manipulate local project source code

