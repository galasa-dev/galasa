/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
/**
 * Docker Manager
 * 
 * @galasa.manager Docker
 * 
 * @galasa.release.state Release
 * 
 * @galasa.description
 * 
 * This Manager enables tests to run Docker Containers on a Docker Engine provided by the Galasa infrastructure, making it easy to write tests that consume container-based services.
 * The test does not need to worry about where the Docker infrastructure is, its credentials, or its capacity as this is all handled by the Manager.
 * <br><br>
 * The Docker Manager can be used by other Managers as a base for their own services. 
 * For example, the JMeter Manager can run a JMeter service inside a Docker Container. 
 * Using the Docker Manager in this way means that the test or administration team 
 * do not need to create dedicated JMeter resources.
 * <br><br> 
 * Containers that are provided by the Docker Manager can be used to either drive 
 * workload for the application under test, or to receive workload from the application. 
 * The Docker Manager can also be used to monitor the test or to provide a security context like 
 * OpenLDAP. Docker Containers provide a powerful tool in helping test applications in an integrated environment.
 * <br><br>
 * The Docker Manager supports Galasa Shared Environments.  Shared Environments provide 
 * the ability to create a test environment that can be shared across multiple test runs 
 * so you don't have to provision a test environment for each test.  
 * 
 * @galasa.limitations
 * 
 * The Docker Manager supports only AMD64 platforms. It is planned to expand the capability to S390x.
 * <br><br>
 * The Docker Manager currently supports only a single Docker Engine. 
 * It is planned to allow multiple Docker Engines to be configured.<br><br>
 *
 * You can view the <a href="https://javadoc.galasa.dev/dev/galasa/docker/package-summary.html">Javadoc documentation for the Manager here</a>.
 * <br><br>
 */
package dev.galasa.docker;
