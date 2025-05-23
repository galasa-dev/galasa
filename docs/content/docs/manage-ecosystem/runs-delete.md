---
title: "Deleting test run results"
---

Over time, test run results build up and take up space in the Galasa Ecosystem. You can delete test runs which have completed in the past by using the `galasactl runs delete` command.

You can view the full command syntax in the <a href="https://github.com/galasa-dev/cli/blob/main/docs/generated/galasactl_runs_delete.md" target="_blank">Galasa cli repository</a>.

## Working with the `galasactl runs delete` command

The following example deletes a test run called _C1234_: 

```
galasactl runs delete --name C1234 --bootstrap http://example.com:30960/bootstrap
```

## Identifying test runs to delete

Use the `galasactl runs get` command to find the names of the test runs to delete. For example, use the `--age` option to specify a time period in which the tests ran. 

```
galasactl runs get --age 6w:1w --bootstrap http://example.com:30960/bootstrap
```

The `--format raw` option is useful if you are writing a script to delete multiple test runs.

You can view the full command syntax for `galasactl runs get` in the <a href="https://github.com/galasa-dev/cli/blob/main/docs/generated/galasactl_runs_get.md" target="_blank">Galasa cli repository</a>.
