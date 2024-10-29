/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
/**
 * Galasa Core Manager
 * 
 * @galasa.manager Core
 * 
 * @galasa.release.state Release
 * 
 * @galasa.description
 * 
 * The Core Manager provides tests with access to some of the core features within the Galasa Framework. The Core Manager is always initialised and included in a test run and contributes the <code>Logger</code>, <code>StoredArtifactRoot</code> and <code>TestProperty</code> annotations.
 * <br><br>
 * The <code>Logger</code> annotation is provided by the Core Manager to create the log which is then automatically stored in the Result Archive Store (RAS) by the Galasa framework. 
 * <br><br>
 * The <code>StoredArtifactRoot</code> annotation lets your test write specific output to the RAS. Whilst the Galasa framework automatically sends test output to be stored in the RAS, this annotation enables you to write code to send output specific to your application to be stored. 
 * <br><br>
 * The Core Manager uses methods including <code>getCredentials</code>, <code>getRunName</code> and <code>ConfidentialText</code> credentials. <code>getCredentials</code> lets you retrieve a user id and password from a file to use in your test and <code>ConfidentialText</code> ensures that the password value is masked. The ability to get credentials from a file means that you do not need to hard code these values in your test and that the test can be run in different environments without the need to change a single line of code. 

 * <br><br>
 *
 * You can view the <a href="https://javadoc.galasa.dev/dev/galasa/core/manager/package-summary.html">Javadoc documentation for this Manager here</a>.
 * <br><br>

 */
package dev.galasa.core.manager;