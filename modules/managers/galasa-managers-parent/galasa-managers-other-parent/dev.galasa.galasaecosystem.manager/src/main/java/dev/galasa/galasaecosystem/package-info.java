/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
/**
 * Galasa Ecosystem Manager
 * 
 * @galasa.manager Galasa Ecosystem
 * 
 * @galasa.release.state ALPHA - This Manager is being actively developed. It is subject to change and has not been extensively tested.
 * 
 * @galasa.description
 * 
 * This Manager provides the test with a fully provisioned Galasa Ecosystem on which to test.  When the test starts running
 * the test can be assured that all the services are up and working.
 * <br><br>
 * The Galasa Ecosystem Manager supports Galasa Shared Environments. Shared Environments provide 
 * the ability to create a test environment that can be shared across multiple test runs 
 * so you don't have to provision a test environment for each test.  
 * 
 * @galasa.limitations
 * 
 * The Manager only supports the following platforms:<br>
 * - Kubernetes Namespace<br>
 * <br>
 * In the near future, this Manager will be able to provision ecosystems in Docker and on a plain Linux server.<br><br>
 *
 * You can view the <a href="https://javadoc.galasa.dev/dev/galasa/galasaecosystem/package-summary.html">Javadoc documentation for the Manager here</a>.
 * <br><br>
 * 
 */
package dev.galasa.galasaecosystem;
