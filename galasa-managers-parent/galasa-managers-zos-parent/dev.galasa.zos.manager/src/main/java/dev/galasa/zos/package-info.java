/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
/**
 * z/OS Manager
 * 
 * @galasa.manager zOS
 * 
 * @galasa.release.state BETA - This Manager is feature complete but may contain known or unknown bugs.
 * 
 * @galasa.description
 *
 * This Manager provides tests and Managers with access to and configuration information about z/OS images and Sysplexes. It offers services such as APF, DUMP, SMF and Log access. <br><br>
 * Additionally, the z/OS Manager provides tests with interfaces to the following z/OS functions which are implemented by other Managers: <br><br>
 * - <code>z/OS Batch</code> which enables tests and Managers to submit, monitor and retrieve the output of z/OS batch jobs. See <a href="/docs/running-simbank-tests/batch-accounts-open-test">BatchAccountsOpenTest</a> for a walkthrough of a test that employs this Manager.<br><br>
 * - <code>z/OS Console</code> which allows tests and Managers to issue and retrieve the responses from z/OS console commands.<br><br>
 * - <code>z/OS File</code> which provides tests and Managers with the ability to manage and transfer files to and from z/OS. Supported file types include Sequential, PDS, PDSE, KSDS, ESDS or RRDS and zOS UNIX files.<br><br>
 * - <code>z/OS TSO Command</code> which enables tests and Managers to issue and retrieve the responses from z/OS TSO commands. <br><br>
 * - <code>z/OS UNIX Command</code> which enables tests and Managers to issue and retrieve the responses from z/OS UNIX commands.<br><br>

 * <br><br>
 *
 * You can view the <a href="https://javadoc.galasa.dev/dev/galasa/zos/package-summary.html">Javadoc documentation for the Manager here</a>.
 * <br><br>
 * 
 */
package dev.galasa.zos;