/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.jmeter.internal;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.jmeter.IJMeterSession;
import dev.galasa.jmeter.JMeterManagerException;

/**
 * Abstract base class for JMeter session implementations.
 * Contains shared logic for Velocity template processing, file path derivation,
 * and default method implementations.
 */
public abstract class AbstractJMeterSession implements IJMeterSession {
    
    protected static final Log logger = LogFactory.getLog(AbstractJMeterSession.class);
    
    protected final int sessionID;
    protected final IFramework framework;
    protected final VelocityTemplateProcessor velocityProcessor;
    
    /**
     * Constructor for abstract base class
     * 
     * @param sessionID The unique session identifier
     * @param framework The Galasa framework instance
     */
    protected AbstractJMeterSession(int sessionID, IFramework framework) {
        this.sessionID = sessionID;
        this.framework = framework;
        this.velocityProcessor = new VelocityTemplateProcessor();
    }
    
    @Override
    public int getSessionID() {
        return sessionID;
    }
    
    /**
     * Default implementation delegates to applyProperties with null parameters
     */
    @Override
    public void applyProperties(InputStream propStream) throws JMeterManagerException {
        applyProperties(propStream, null);
    }
    
    /**
     * Default implementation delegates to startJmeter with default timeout
     */
    @Override
    public void startJmeter() throws JMeterManagerException {
        startJmeter(JMeterConstants.DEFAULT_TIMEOUT_MILLISECONDS);
    }
    
    /**
     * Validate timeout parameter
     *
     * @param timeout The timeout value to validate
     * @throws JMeterManagerException if timeout is invalid
     */
    protected void validateTimeout(int timeout) throws JMeterManagerException {
        if (timeout <= 0) {
            throw new JMeterManagerException("Timeout must be positive, got: " + timeout);
        }
    }
    
    /**
     * Validate filename parameter
     *
     * @param fileName The filename to validate
     * @throws JMeterManagerException if filename is invalid
     */
    protected void validateFileName(String fileName) throws JMeterManagerException {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new JMeterManagerException("Filename cannot be null or empty");
        }
    }
    
    /**
     * Validate input stream parameter
     *
     * @param stream The stream to validate
     * @param streamName The name of the stream for error messages
     * @throws JMeterManagerException if stream is invalid
     */
    protected void validateInputStream(InputStream stream, String streamName) throws JMeterManagerException {
        if (stream == null) {
            throw new JMeterManagerException(streamName + " cannot be null");
        }
    }
    
    /**
     * Derive the base filename from a JMX path by removing the .jmx extension
     * 
     * @param jmxPath The JMX file path (e.g., "test.jmx")
     * @return The base filename without extension (e.g., "test")
     * @throws JMeterManagerException if the path doesn't contain .jmx extension
     */
    protected String deriveBaseFilename(String jmxPath) throws JMeterManagerException {
        if (jmxPath == null || !jmxPath.contains(JMeterConstants.JMX_EXTENSION)) {
            throw new JMeterManagerException("Invalid JMX path: " + jmxPath);
        }
        return jmxPath.substring(0, jmxPath.indexOf(JMeterConstants.JMX_EXTENSION));
    }
    
    /**
     * Derive the results file path from a JMX path
     * 
     * @param jmxPath The JMX file path (e.g., "test.jmx")
     * @return The results file path (e.g., "test.jtl")
     * @throws JMeterManagerException if the path is invalid
     */
    protected String deriveResultsPath(String jmxPath) throws JMeterManagerException {
        return deriveBaseFilename(jmxPath) + JMeterConstants.JTL_EXTENSION;
    }
    
    /**
     * Derive the log file path from a JMX path
     * 
     * @param jmxPath The JMX file path (e.g., "test.jmx")
     * @return The log file path (e.g., "test.log")
     * @throws JMeterManagerException if the path is invalid
     */
    protected String deriveLogPath(String jmxPath) throws JMeterManagerException {
        return deriveBaseFilename(jmxPath) + JMeterConstants.LOG_EXTENSION;
    }
    
    /**
     * Process a JMX template with Velocity parameters
     * 
     * @param jmxStream The JMX template input stream
     * @param parameters Map of parameter names to values
     * @return Processed JMX content as string
     * @throws JMeterManagerException if template processing fails
     */
    protected String processJmxTemplate(InputStream jmxStream, Map<String, Object> parameters)
            throws JMeterManagerException {
        validateInputStream(jmxStream, "JMX stream");
        try {
            String jmxTemplate = new String(jmxStream.readAllBytes(), StandardCharsets.UTF_8);
            return velocityProcessor.processTemplate(jmxTemplate, parameters);
        } catch (Exception e) {
            throw new JMeterManagerException("Failed to process JMX template for session " + sessionID, e);
        }
    }
    
    /**
     * Process a properties stream with Velocity parameters
     * 
     * @param propStream The properties input stream
     * @param parameters Map of parameter names to values
     * @return Processed properties as InputStream
     * @throws JMeterManagerException if template processing fails
     */
    protected InputStream processPropertiesTemplate(InputStream propStream, Map<String, Object> parameters)
            throws JMeterManagerException {
        validateInputStream(propStream, "Properties stream");
        return velocityProcessor.processStream(propStream, parameters);
    }
}
