---
title: "z/OS Batch z/OS MF Manager"
---

You can view the [Javadoc documentation for the Manager](../../reference/javadoc/dev/galasa/zosbatch/package-summary.html){target="_blank"}.


## Overview

This Manager is an internal implementation of the z/OS Batch Manager using z/OS MF. The z/OS MF Batch Manager is used in conjunction with the z/OS Manager. The z/OS Manager provides the interface for the z/OS batch function and pulls in the z/OS MF Batch Manager to provide the implementation of the interface. If your test needs to submit or monitor a batch job or retrieve output from a batch job, you can call the z/OS Manager in your test code and the z/OS Manager will call the z/OS MF Batch Manager to provide the implementation via the z/OS batch  function. For example, the [BatchAccountsOpenTest](../../running-simbank-tests/batch-accounts-open-test.md) uses the z/OS Manager (which in the background, invokes z/OS MF) to add a set of accounts to the Galasa SimBank  system via a z/OS batch job.

The zOS Batch z/OS MF Manager is enabled by setting the CPS property: `zos.bundle.extra.batch.manager=dev.galasa.zosbatch.zosmf.manager`.
Galasa sets this property by default.

See the [zOS Manager](../zos-managers/zos-manager.md) for details of the z/OS Batch annotations and code snippets.

