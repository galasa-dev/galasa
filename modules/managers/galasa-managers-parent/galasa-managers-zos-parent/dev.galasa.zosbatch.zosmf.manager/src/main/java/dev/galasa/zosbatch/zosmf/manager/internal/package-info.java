/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
/**
 * z/OS Batch Manager - z/OS MF Internal Implementation
 * 
 * @galasa.manager z/OS Batch z/OS MF
 * 
 * @galasa.release.state BETA - This Manager is feature complete but may contain known or unknown bugs.
 * 
 * @galasa.description
 * 
 * This Manager is an internal implementation of the z/OS Batch Manager using z/OS MF. The z/OS MF Batch Manager
 * is used in conjunction with the z/OS Manager. The z/OS Manager provides the interface for the z/OS batch function
 * and pulls in the z/OS MF Batch Manager to provide the implementation of the interface. If your test needs to
 * submit or monitor a batch job or retrieve output from a batch job, you can call the z/OS Manager in your test
 * code and the z/OS Manager will call the z/OS MF Batch Manager to provide the implementation via the z/OS batch 
 * function. For example, the <a href="/docs/running-simbank-tests/batch-accounts-open-test">BatchAccountsOpenTest</a> 
 * uses the z/OS Manager (which in the background, invokes z/OS MF) to add a set of accounts to the Galasa SimBank 
 * system via a z/OS batch job. 
 * <p>
 * The zOS Batch z/OS MF Manager is enabled by setting the CPS property:<br>
 * <code>zos.bundle.extra.batch.manager=dev.galasa.zosbatch.zosmf.manager</code><br> 
 * Galasa sets this property by default.
 * <p>
 * See the <a href="/docs/managers/zos-manager">zOS Manager</a> for details of the z/OS Batch annotations and code snippets. 
 */
package dev.galasa.zosbatch.zosmf.manager.internal;