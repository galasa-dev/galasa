/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
/**
 * JMeter Manager
 * 
 * @galasa.manager JMeter
 * 
 * @galasa.release.state BETA - This Manager is almost ready.  It has been tested and the TPI is stable, but there may be minor changes to come.
 * 
 * @galasa.description
 * 
 * This Manager enables a JMeter session to run inside a Docker Container. The JMeter Manager requests a container from the Docker Manager inside which the JMeter scripts, or JMX files can run.  
 * The test can access all JMeter-generated files inside the container without worrying about how the container is provisioned, maintained or shut down at the end of test.
 * By using a containerized environment, the test can benefit from the associated standards of scalability and uniformity.
 * <br><br>
 * 
 * <br><br> 
 * The logfiles and generated CSV files can be accessed once the JMeter tests are complete and the container becomes available for interaction.
 * The JMeter Manager allows as many JMeter sessions as you have available Docker container slots on your machine.
 * 
 * @galasa.limitations
 * 
 * JMeter tests cannot be run remotely on a target host.<br><br>
 *
 * You can view the <a href="https://javadoc.galasa.dev/dev/galasa/jmeter/package-summary.html">Javadoc documentation for the Manager here</a>.
 * <br><br>
 * 
 */
package dev.galasa.jmeter;
