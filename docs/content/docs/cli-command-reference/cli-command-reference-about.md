---
title: "Galasa CLI commands"
---

Galasa CLI commands start with `galasactl`. Example commands are provided for running on Mac or Unix, and Windows Powershell. The Windows Powershell uses the backtick (`) for line continuation characters. If you are using Windows command-shell, the line continuation character is the caret (^). 

You can view the Galasa CLI command syntax, including parameter descriptions, in the [CLI reference](../reference/cli-syntax/galasactl.md).


## Getting help

Once you have installed the Galasa CLI, you can use the following command to get more information about the command and command options, including default values.

```shell
galasactl --help
```

Use the `--log` option to send logging information to a file. Any folder that is referenced must exist. Existing files are overwritten. Specify `-` to log to `stderr`. The default is no logging.

```shell
galasactl --log <logFilePath>  
```  

## Errors

You can view a list of error messages that can be generated by the galasactl tool in the [CLI reference](../reference/cli-syntax/errors-list.md).


## Known limitations

Go programs can sometimes struggle to resolve DNS names, especially when a working over a virtual private network (VPN). In such situations, you might notice that a bootstrap file cannot be found with galasactl, but can be found by a desktop browser, or curl command. In such situations you can manually add the host detail to the `/etc/hosts` file, to avoid DNS being involved in the resolution mechanism.


