/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.jmeter.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.HashMap;

import org.apache.commons.logging.Log;

import dev.galasa.Test;
import dev.galasa.artifact.BundleResources;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.artifact.TestBundleResourceException;
import dev.galasa.core.manager.Logger;
import dev.galasa.jmeter.IJMeterSession;
import dev.galasa.jmeter.JMeterManagerException;
import dev.galasa.jmeter.JMeterSession;

/**
 * IVT for JMeter Manager testing both Docker and LOCAL modes.
 *
 * To test LOCAL mode: Set CPS property jmeter.mode=LOCAL (default) and jmeter.binary.path
 * To test Docker mode: Set CPS property jmeter.mode=DOCKER and Docker Manager properties
 */
@Test
public class JMeterManagerIVT {

    @Logger
    public Log logger;

    @BundleResources
    public IBundleResources resources;

    // DynamicTest.jmx - Uses Velocity templating ($HOST, $PORT, etc.) for runtime parameter substitution
    @JMeterSession(jmxPath = "DynamicTest.jmx", propPath = "jmeter.properties")
    public IJMeterSession session;

    // ExistingTest.jmx - Uses JMeter's native property syntax ${__P(host,default)} with command-line properties
    @JMeterSession(jmxPath = "ExistingTest.jmx")
    public IJMeterSession session2;
    
    // MinimalTest.jmx - Simplest test with hardcoded values (1 thread, Debug Sampler only) for quick validation
    @JMeterSession(jmxPath = "MinimalTest.jmx")
    public IJMeterSession session3;

    @Test
    public void testSessionsProvisionedNotNull() {
        logger.info("Verifying all sessions are provisioned correctly");
        
        assertThat(logger).as("Logger should be injected").isNotNull();
        assertThat(session).as("Session 1 should be provisioned").isNotNull();
        assertThat(session2).as("Session 2 should be provisioned").isNotNull();
        assertThat(session3).as("Session 3 should be provisioned").isNotNull();
        
        logger.info("All sessions provisioned successfully");
    }
    
    @Test
    public void testSessionIdsAreUnique() {
        logger.info("Verifying unique session IDs");
        
        int id1 = session.getSessionID();
        int id2 = session2.getSessionID();
        int id3 = session3.getSessionID();
        
        assertThat(id1).as("Session 1 ID should be positive").isGreaterThan(0);
        assertThat(id2).as("Session 2 ID should be positive").isGreaterThan(0);
        assertThat(id3).as("Session 3 ID should be positive").isGreaterThan(0);
        
        assertThat(id1).as("Session IDs should be unique").isNotEqualTo(id2);
        assertThat(id2).as("Session IDs should be unique").isNotEqualTo(id3);
        assertThat(id1).as("Session IDs should be unique").isNotEqualTo(id3);
        
        logger.info("Session IDs are unique: " + id1 + ", " + id2 + ", " + id3);
    }

    @Test
    public void testDynamicHostSubstitution()
            throws JMeterManagerException, TestBundleResourceException {
        logger.info("Testing dynamic host substitution with velocity templating and property overrides");
        
        InputStream jmxStream = resources.retrieveFile("/DynamicTest.jmx");
        InputStream propStream = resources.retrieveFile("/jmeter.properties");

        // Substitute variables in JMX file
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("HOST", "galasa.dev");
        parameters.put("PORT", "443");
        parameters.put("PROTOCOL", "https");
        parameters.put("PATH", "/docs");
        parameters.put("THREADS", "1");
        parameters.put("RAMPUP", "1");
        parameters.put("DURATION", "5");

        // Test property overrides (programmatic property setting)
        HashMap<String, Object> propOverrides = new HashMap<>();
        propOverrides.put("jmeter.save.saveservice.output_format", "xml");
        propOverrides.put("jmeter.save.saveservice.response_data", "true");

        session.setChangedParametersJmxFile(jmxStream, parameters);
        session.applyProperties(propStream, propOverrides);
        
        // Verify JMX file was stored correctly
        String jmxContent = session.getJmxFile();
        assertThat(jmxContent).as("JMX should contain substituted host")
            .contains("galasa.dev");
        assertThat(jmxContent).as("JMX should not contain template variable")
            .doesNotContain("$HOST");
        
        logger.info("Dynamic substitution and property overrides verified in JMX file");
        
        // Execute the test
        session.startJmeter(120000); // 2 minute timeout
        
        // Verify execution was successful
        assertThat(session.isTestSuccessful()).as("Test should complete successfully").isTrue();
        
        // Verify output files are accessible
        String logFile = session.getLogFile();
        assertThat(logFile).as("Log file should not be empty").isNotEmpty();
        assertThat(logFile).as("Log should indicate test completion")
            .contains("Notifying test listeners of end of test");
        
        String consoleOutput = session.getConsoleOutput();
        assertThat(consoleOutput).as("Console output should be available").isNotNull();
        
        logger.info("Dynamic test with property overrides executed successfully");
    }

    @Test
    public void testStaticJmxExecution()
            throws JMeterManagerException, TestBundleResourceException {
        logger.info("Testing static JMX execution with no variable substitution");
        
        InputStream jmxStream = resources.retrieveFile("/ExistingTest.jmx");

        session2.setDefaultGeneratedJmxFile(jmxStream);
        session2.startJmeter(120000);

        assertThat(session2.isTestSuccessful()).as("Static test should complete successfully").isTrue();
        
        // Verify log file contains expected content
        String logFile = session2.getLogFile();
        assertThat(logFile).contains("Loading file:");
        assertThat(logFile).contains("ExistingTest.jmx");
        assertThat(logFile).contains("Running test");
        
        // Test listener file retrieval (ExistingTest.jtl - matches JMX filename)
        String jtlContent = session2.getListenerFile("ExistingTest.jtl");
        assertThat(jtlContent).as("JTL file should not be empty").isNotEmpty();
        assertThat(jtlContent).as("JTL should contain test results")
            .containsAnyOf("timeStamp", "elapsed", "label");
        
        logger.info("Static test executed successfully and listener file retrieved");
    }

    @Test
    public void testMinimalConfiguration() 
            throws JMeterManagerException, TestBundleResourceException {
        logger.info("Testing minimal JMeter configuration");
        
        InputStream jmxStream = resources.retrieveFile("/MinimalTest.jmx");
        
        session3.setDefaultGeneratedJmxFile(jmxStream);
        session3.startJmeter(60000); // 1 minute timeout
        
        assertThat(session3.isTestSuccessful()).as("Minimal test should complete").isTrue();
        
        long exitCode = session3.getExitCode();
        assertThat(exitCode).as("Exit code should be 0 for success").isEqualTo(0L);
        
        logger.info("Minimal configuration test passed");
    }
    
    @Test
    public void testSessionCleanup() throws JMeterManagerException {
        logger.info("Testing session cleanup");
        
        // Get session ID before cleanup
        int sessionId = session3.getSessionID();
        assertThat(sessionId).isGreaterThan(0);
        
        // Stop the session
        session3.stopTest();
        
        logger.info("Session " + sessionId + " cleaned up successfully");
    }

}