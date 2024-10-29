/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
/**
 * HTTP Client Manager
 * 
 * @galasa.manager HTTP Client
 * 
 * @galasa.release.state Release
 * 
 * @galasa.description
 * 
 *                     This Manager provides a variety of common HTTP client
 *                     operations you can use in your tests. For example, you
 *                     can use this Manager in a test where you want to
 *                     determine if a particular web page contains (or does not
 *                     contain) some specific content. This is exactly how it is
 *                     used in the <a href=
 *                     "https://github.com/galasa-dev/managers/blob/main/galasa-managers-parent/galasa-managers-cloud-parent/dev.galasa.docker.manager.ivt/src/main/java/dev/galasa/docker/manager/ivt/DockerManagerIVT.java"
 *                     target="_blank" rel="noopener noreferrer"> Docker Manager
 *                     IVT</a> (Installation Verification Test). As well as
 *                     providing client functionality to people who write tests,
 *                     it may also be used internally by other Managers to
 *                     enrich their range of offered services.
 * 
 *                     This Manager supports outbound HTTP calls, JSON requests,
 *                     HTTP file transfer and Web Services calls. SSL is
 *                     supported.
 *
 *                     You can view the <a href=
 *                     "https://javadoc.galasa.dev/dev/galasa/http/package-summary.html"
 *                     target="_blank" rel="noopener noreferrer">Javadoc
 *                     documentation for the Manager here</a>. <br>
 *                     <br>
 */
package dev.galasa.http;
