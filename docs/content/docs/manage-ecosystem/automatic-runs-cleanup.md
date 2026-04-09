---
title: "Configuring automatic cleanup of test runs"
---

Over time, test run results build up and take up space in the Galasa Ecosystem's Result Archive Store (RAS). To help manage storage, the Galasa service includes an automatic cleanup process that deletes old test runs based on configurable criteria.

This automatic cleanup process complements the manual deletion capability provided by the [`galasactl runs delete`](./runs-delete.md) command, allowing you to maintain your test run history without manual intervention.

## How the cleanup process works

The cleanup process runs periodically to identify and delete test runs that meet the configured age criteria. By default:

- Test runs older than 30 days are deleted
- The cleanup process runs once every 24 hours

You can configure both the age threshold and the cleanup frequency to suit your needs.

## Configuring cleanup settings

There are two ways to configure the automatic cleanup process:

1. **Using Helm chart values** - Set values when deploying or upgrading the Galasa service
2. **Using CPS properties** - Configure settings at runtime without redeploying the Galasa service

### Configuring via Helm chart

When deploying or upgrading the Galasa service using Helm, you can set the following values:

- `resourceMonitor.testRunCleanupMaxAgeDays` - The maximum age in days for test runs before they are deleted (default: 30)
- `resourceMonitor.testRunCleanupIntervalHours` - How often the cleanup process runs, in hours (default: 24)

Example values file snippet:

```yaml
resourceMonitor:
  testRunCleanupMaxAgeDays: 60
  testRunCleanupIntervalHours: 12
```

This configuration would delete test runs older than 60 days and run the cleanup process every 12 hours.

### Configuring via CPS properties

You can also configure the cleanup process at runtime by setting Configuration Property Store (CPS) properties. This allows you to adjust settings without redeploying the Galasa service.

Use the [`galasactl properties set`](../reference/cli-syntax/galasactl_properties_set.md) command to configure the following properties in the `framework` namespace:

- `framework.ras.cleanup.test.run.age.max.days` - The maximum age in days for test runs before they are deleted
- `framework.ras.cleanup.test.run.age.interval.hours` - How often the cleanup process runs, in hours

Example commands:

```shell
galasactl properties set --namespace framework \
  --name ras.cleanup.test.run.age.max.days \
  --value 60 \
  --bootstrap https://example.com/api/bootstrap

galasactl properties set --namespace framework \
  --name ras.cleanup.test.run.age.interval.hours \
  --value 12 \
  --bootstrap https://example.com/api/bootstrap
```

**Note:** CPS property settings take precedence over Helm chart values. If both are configured, the CPS property values will be used.

## Excluding test runs from cleanup

You may want to preserve certain test runs indefinitely, such as baseline results or runs from important releases. You can exclude test runs from the automatic cleanup process by configuring exclusion rules based on test run fields.

### Configuring exclusions

Use the `framework.ras.cleanup.test.run.exclude.[FIELD_NAME]` CPS property pattern to exclude test runs based on specific field values, where `[FIELD_NAME]` is the name of the field to filter by.

The property accepts a comma-separated list of values. Test runs matching any of the specified values for that field will be excluded from cleanup.

### Example: Excluding by tags

To exclude test runs that are tagged with `keepMe` or `doNotDelete`:

```shell
galasactl properties set --namespace framework \
  --name ras.cleanup.test.run.exclude.tags \
  --value keepMe,doNotDelete \
  --bootstrap https://example.com/api/bootstrap
```

Any test run with either the `keepMe` or `doNotDelete` tag will be preserved, regardless of its age.

### Example: Excluding by result

To exclude test runs that failed or had environmental failures:

```shell
galasactl properties set --namespace framework \
  --name ras.cleanup.test.run.exclude.result \
  --value Failed,EnvFail \
  --bootstrap https://example.com/api/bootstrap
```

### Example: Excluding by requestor

To exclude test runs submitted by specific requestors:

```shell
galasactl properties set --namespace framework \
  --name ras.cleanup.test.run.exclude.requestor \
  --value alice,bob \
  --bootstrap https://example.com/api/bootstrap
```
