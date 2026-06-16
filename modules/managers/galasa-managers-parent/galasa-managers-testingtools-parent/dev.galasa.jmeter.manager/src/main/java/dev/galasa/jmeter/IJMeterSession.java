/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.jmeter;

import java.io.InputStream;
import java.util.Map;

/**
 * Interface for creation, management, deletion of JMeter sessions
 */
public interface IJMeterSession {
    
    /**
     * Apply properties to the JMeter session from a properties file stream.
     * This method should be called before starting the JMeter test.
     *
     * @param propStream An InputStream containing the properties file content
     * @throws JMeterManagerException if properties cannot be applied
     */
    public void applyProperties(InputStream propStream) throws JMeterManagerException;

    /**
     * Apply properties to the JMeter session from both a properties file stream and additional dynamic properties.
     * This method should be called before starting the JMeter test.
     * The dynamic properties map will be merged with properties from the stream, with dynamic properties taking precedence.
     *
     * @param propStream An InputStream containing the properties file content
     * @param properties A Map of additional dynamic properties to apply
     * @throws JMeterManagerException if properties cannot be applied
     */
    public void applyProperties(InputStream propStream, Map<String,Object> properties) throws JMeterManagerException;

    /**
     * Start the JMeter test execution with the default timeout of 60 seconds.
     * All results are automatically stored in the Result Archive Store (RAS).
     *
     * @throws JMeterManagerException if the test fails to start or execute
     */
    public void startJmeter() throws JMeterManagerException;

    /**
     * Start the JMeter test execution with a specified timeout.
     * All results are automatically stored in the Result Archive Store (RAS).
     *
     * @param timeout The maximum time in seconds to wait for test completion
     * @throws JMeterManagerException if the test fails to start, execute, or times out
     */
    public void startJmeter(int timeout) throws JMeterManagerException;

    /**
     * Set the JMX test plan file for this session using a static (non-parameterized) JMX file.
     * Use this method when your JMX file does not require dynamic parameter substitution.
     *
     * @param jmxStream An InputStream containing the JMX test plan content
     * @throws JMeterManagerException if the JMX file cannot be stored or is invalid
     */
    public void setDefaultGeneratedJmxFile(InputStream jmxStream) throws JMeterManagerException;

    /**
     * Set the JMX test plan file for this session using a parameterized JMX template.
     * This method processes the JMX template using Velocity, replacing variables with provided values.
     *
     * <p>To prepare your JMX file for parameterization:
     * <ol>
     *   <li>Replace JMeter's ${__P(VARIABLE)} notation with Velocity's $VARIABLE notation</li>
     *   <li>Provide parameter values in a Map</li>
     * </ol>
     *
     * <p>Example:
     * <pre>
     * Map<String,Object> params = new HashMap<>();
     * params.put("HOST", "galasa.dev");
     * params.put("PORT", 8080);
     * session.setChangedParametersJmxFile(jmxStream, params);
     * </pre>
     *
     * @param jmxStream An InputStream containing the JMX template content
     * @param parameters A Map of parameter names to values for template substitution
     * @throws JMeterManagerException if the JMX template cannot be processed or stored
     */
    public void setChangedParametersJmxFile(InputStream jmxStream, Map<String,Object> parameters) throws JMeterManagerException;
    
    /**
     * Retrieve the JMX test plan file content as a UTF-8 encoded string.
     *
     * @return The JMX file content
     * @throws JMeterManagerException if the file cannot be retrieved
     */
    public String getJmxFile() throws JMeterManagerException;

    /**
     * Retrieve the JMeter log file content as a UTF-8 encoded string.
     * The log file contains JMeter's execution logs and diagnostic information.
     *
     * @return The log file content
     * @throws JMeterManagerException if the file cannot be retrieved
     */
    public String getLogFile() throws JMeterManagerException;

    /**
     * Retrieve the console output from the JMeter execution.
     *
     * @return The console output as a string
     * @throws JMeterManagerException if the console output cannot be retrieved
     */
    public String getConsoleOutput() throws JMeterManagerException;

    /**
     * Retrieve a specific listener output file from the JMeter execution.
     * Listener files contain test results in various formats (e.g., .jtl files).
     *
     * @param fileName The name of the listener file to retrieve
     * @return The listener file content as a string
     * @throws JMeterManagerException if the file cannot be retrieved or fileName is invalid
     */
    public String getListenerFile(String fileName) throws JMeterManagerException;

    /**
     * Check whether the JMeter test executed successfully.
     *
     * <p>For LOCAL mode: Returns true if exit code is 0
     * <p>For DOCKER mode: Returns true if log contains expected completion markers
     *
     * @return true if the test completed successfully, false otherwise
     * @throws JMeterManagerException if the test status cannot be determined or the test failed
     */
    public boolean isTestSuccessful() throws JMeterManagerException;

    /**
     * Stop the JMeter test execution and clean up resources.
     *
     * <p>For LOCAL mode: This is a no-op as binary execution completes automatically
     * <p>For DOCKER mode: Stops the container and removes it from active sessions
     *
     * @throws JMeterManagerException if the test cannot be stopped or cleanup fails
     */
    public void stopTest() throws JMeterManagerException;

    /**
     * Get the exit code from the JMeter process execution.
     *
     * @return The exit code (0 indicates success, non-zero indicates failure)
     * @throws JMeterManagerException if the exit code cannot be retrieved
     */
    public long getExitCode() throws JMeterManagerException;

    /**
     * Get the unique session identifier for this JMeter session.
     *
     * @return The session ID
     */
    public int getSessionID();
       
}