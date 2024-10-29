/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
/**
 * z/OS Batch Manager - RSE API Internal Implementation
 * 
 * @galasa.manager z/OS Batch RSE API
 * 
 * @galasa.release.state ALPHA - This Manager is being actively developed. It is subject to change and has not been extensively tested.
 * 
 * @galasa.description
 * 
 * This Manager is an internal implementation of the z/OS Batch Manager using RSE API. The RSE API Batch Manager
 * is used in conjunction with the z/OS Manager. The z/OS Manager provides the interface for the z/OS batch function
 * and pulls in the RSE API Batch Manager to provide the implementation of the interface. If your test needs to
 * submit or monitor a batch job or retrieve output from a batch job, you can call the z/OS Manager in your test
 * code and the z/OS Manager will call the RSE API Batch Manager to provide the implementation via the z/OS batch 
 * function. For example, the <a href="/docs/running-simbank-tests/batch-accounts-open-test">BatchAccountsOpenTest</a> 
 * uses the z/OS Manager (which in the background, invokes RSE API) to add a set of accounts to the Galasa SimBank 
 * system via a z/OS batch job. 
 * <p>
 * The zOS Batch RSE API Manager is enabled by setting the CPS property:
 * <code>zos.bundle.extra.batch.manager=dev.galasa.zosbatch.rseapi.manager</code> 
 * <p>
 * See the <a href="/docs/managers/zos-manager">zOS Manager</a> for details of the z/OS Batch annotations and code snippets. 
 * 
 */
package dev.galasa.zosbatch.rseapi.manager.internal;