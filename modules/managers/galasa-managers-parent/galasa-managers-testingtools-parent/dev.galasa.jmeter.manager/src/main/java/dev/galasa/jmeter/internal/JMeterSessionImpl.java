/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.jmeter.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import dev.galasa.ResultArchiveStoreContentType;
import dev.galasa.SetContentType;
import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.IDockerContainer;
import dev.galasa.docker.IDockerExec;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.jmeter.JMeterManagerException;

public class JMeterSessionImpl extends AbstractJMeterSession {

    private final JMeterManagerImpl jMeterManager;
    private String jmxPath;
    private String propPath;
    private String jmxAbsolutePath;
    private String propAbsolutePath;
    private String jmeterDockerPath;
    private String jmeter;
    private IDockerContainer container;
    private Path storedArtifactsRoot;
    private boolean testStarted = false;
    
    private static final String STORED_MESSAGE = " has been stored in the container.";

    public JMeterSessionImpl(IFramework framework, JMeterManagerImpl jMeterManager, int sessionID, String jmxPath,
            String propPath, IDockerContainer container, String jmeter) throws DockerManagerException {
        super(sessionID, framework);
        this.jMeterManager = jMeterManager;
        this.jmxPath = jmxPath;
        this.propPath = propPath;
        this.container = container;
        this.jmxAbsolutePath = "";
        this.propAbsolutePath = "";
        this.jmeterDockerPath = "/" + jmeter + "/";
        this.jmeter = jmeter;

        storedArtifactsRoot = framework.getResultArchiveStore().getStoredArtifactsRoot();

        container.start();

        logger.info(String.format("Session %d has been successfully initialized", this.sessionID));
    }

    /**
     * Actually executing JMeter with the given JMX that has been set with a specified timeout
     */
    @Override
    public void startJmeter(int timeout) throws JMeterManagerException {
        validateTimeout(timeout);
        
        if (testStarted) {
            throw new JMeterManagerException("JMeter test has already been started for session " + sessionID);
        }

        try {
            // Derive results and log paths using base class utility
            String jtlPath = deriveResultsPath(jmxPath);
            String logfile = deriveLogPath(jmxPath);

            if ((this.jmxPath.toLowerCase().endsWith(JMeterConstants.JMX_EXTENSION)) && (!this.jmxAbsolutePath.isEmpty())) {

                if (this.propAbsolutePath.isEmpty()) {

                    IDockerExec exec = container.exec(timeout, jmeter, "-n", "-t", this.jmxPath, "-l", jtlPath, "-j", logfile);
                    boolean finished = exec.waitForExec(timeout);

                    if (!finished) {
                        throw new JMeterManagerException("JMeter execution timeout after " + timeout + "ms for session " + this.sessionID);
                    }

                    if (exec.getExitCode() != 0L) {
                        logger.info("JMeter commands have failed with exitcode " + exec.getExitCode());
                        throw new JMeterManagerException("JMeter execution failed with exit code " + exec.getExitCode() + " for session " + this.sessionID);
                    }
                } else {
    
                    IDockerExec exec = container.exec(timeout, jmeter, "-n", "-t", this.jmxPath, "-l", jtlPath, "-p", this.propPath, "-j", logfile);
                    boolean finished = exec.waitForExec(timeout);

                    if (!finished) {
                        throw new JMeterManagerException("JMeter execution timeout after " + timeout + "ms for session " + this.sessionID);
                    }

                    if (exec.getExitCode() != 0L) {
                        logger.info("JMeter commands have failed with exitcode " + exec.getExitCode());
                        throw new JMeterManagerException("JMeter execution failed with exit code " + exec.getExitCode() + " for session " + this.sessionID);
                    }
                }
                testStarted = true;
                storeOutput("jtlOutput_" + this.sessionID + ".txt", getListenerFile(jtlPath));
                storeOutput("logOutput_" + this.sessionID + ".txt", getLogFile());
            } else {
                throw new JMeterManagerException("The JmxPath has not been specified correctly for session " + this.sessionID + ".");
            }
        
        } catch (JMeterManagerException e) {
            // Re-throw JMeterManagerException to preserve specific error messages (e.g., timeout)
            throw e;
        } catch (Exception e) {
            throw new JMeterManagerException("JMeter session " + sessionID + " could not be executed properly.", e);
        }
    }

    /**
     * Uses the annotation of JmxPath to store it in the container with the ArtifactManager
     * @param jmxStream
     * @throws JMeterManagerException
    */
    @Override
    public void setDefaultGeneratedJmxFile(InputStream jmxStream) throws JMeterManagerException{
        validateInputStream(jmxStream, "JMX stream");
        
        if (testStarted) {
            throw new JMeterManagerException("Cannot set JMX file after test has started for session " + sessionID);
        }
        
        try {
            this.jmxAbsolutePath = jmeterDockerPath + this.jmxPath;
            container.storeFile(this.jmxAbsolutePath, jmxStream);
            logger.info(jmxPath + STORED_MESSAGE);
            
        } catch (Exception e) {
            throw new JMeterManagerException("Failed to store JMX file in container for session " + sessionID, e);
        }
    }

    @Override
    public void setChangedParametersJmxFile(InputStream jmxStream, Map<String, Object> parameters) throws JMeterManagerException {
        validateInputStream(jmxStream, "JMX stream");
        
        if (testStarted) {
            throw new JMeterManagerException("Cannot set JMX file after test has started for session " + sessionID);
        }

        HashMap<String, Object> changes = new HashMap<>();
        changes.put("HOST", "");
        changes.put("PORT", "");
        changes.put("PROTOCOL", "");
        changes.put("PATH", "/");
        changes.put("THREADS", "1");
        changes.put("RAMPUP", "");
        changes.put("DURATION", "15");

        if (parameters != null) {
            for (Entry<String, Object> entry : parameters.entrySet()) {
                changes.put(entry.getKey(), entry.getValue());
            }
        }

        try {
            // Use base class utility to process template with Velocity
            InputStream processedStream = processPropertiesTemplate(jmxStream, changes);
            
            this.jmxAbsolutePath = jmeterDockerPath + this.jmxPath;
            container.storeFile(this.jmxAbsolutePath, processedStream);
            logger.info(jmxPath + STORED_MESSAGE);
            
        } catch (JMeterManagerException e) {
            throw e;
        } catch (Exception e) {
            throw new JMeterManagerException("Failed to store parameterized JMX file in container for session " + sessionID, e);
        }
    }

    /**
     * If the property annotation is filled in, the custom property file gets used
     * Uses the annotation of PropPath to store it in the container with the use ArtifactManager
     * @param propStream
     * @throws JMeterManagerException
     */
    @Override
    public void applyProperties(InputStream propStream) throws JMeterManagerException {
        validateInputStream(propStream, "Properties stream");
        
        if (testStarted) {
            throw new JMeterManagerException("Cannot apply properties after test has started for session " + sessionID);
        }
        
        try {
            this.propAbsolutePath = jmeterDockerPath + propPath;
            container.storeFile(this.propAbsolutePath, propStream);

            logger.info(propPath + STORED_MESSAGE);
        } catch (Exception e) {
            throw new JMeterManagerException("Failed to store properties file in container for session " + sessionID, e);
        }
    }

    @Override
    public void applyProperties(InputStream propStream, Map<String, Object> properties) throws JMeterManagerException {
        // Use base class utility to process template with Velocity
        InputStream processedStream = processPropertiesTemplate(propStream, properties);
        applyProperties(processedStream);
    }
    
     
    /**
     * @return the jmxFile gets returned as a string like "cat" would in a linux container
     * @throws JMeterManagerException 
     */
    @Override
    public String getJmxFile() throws JMeterManagerException {
        try {
            String jmx = jmeterDockerPath + this.jmxPath;
            String jmxStr = "";
            if (container.isRunning()) {
                jmxStr = container.retrieveFileAsString(jmx);
            }

            return jmxStr;
        } catch (Exception e) {
            throw new JMeterManagerException("Could not retrieve the JMX file from container for session " + sessionID, e);
        }
    }

    /**
     * @return the logFile gets returned as a string like "cat" would in a linux container
     * @throws JMeterManagerException 
     */
    @Override
    public String getLogFile() throws JMeterManagerException {
        try {
            String logPath = jmeterDockerPath + deriveLogPath(this.jmxPath);
            String logAsStr = "";
            if (container.isRunning()) {
                logAsStr = container.retrieveFileAsString(logPath);
            }
            
            return logAsStr;
        } catch (Exception e) {
            throw new JMeterManagerException("Could not retrieve the log file from container for session " + sessionID, e);
        }

    }

    /**
     * @return the consoleOutput gets returned as a string
     * @throws JMeterManagerException 
     */
    @Override
    public String getConsoleOutput() throws JMeterManagerException {
        try {
            String consoleStr = "";
            if (container.isRunning()) {
                consoleStr = container.retrieveStdOut();
            }

            return consoleStr;
        } catch (Exception e) {
            throw new JMeterManagerException("Could not retrieve console output from container for session " + sessionID, e);
        }
    }

    /**
     * @param fileName
     * @return the specified file gets returned as a string
     * @throws JMeterManagerException 
     */
    @Override
    public String getListenerFile(String fileName) throws JMeterManagerException {
        validateFileName(fileName);
        try {
            String filePath = jmeterDockerPath + fileName;
            String listenerStr = "";
            if (container.isRunning()) {
                listenerStr = container.retrieveFileAsString(filePath);
            }

            return listenerStr;
        } catch (Exception e) {
            throw new JMeterManagerException("Could not retrieve file '" + fileName + "' from container for session " + sessionID, e);
        }
    }

    /**
     * @return if the test has been performed properly or not
     */
    @Override
    public boolean isTestSuccessful() throws JMeterManagerException {
        String logOutput = this.getLogFile();
        boolean test = false;

        if (logOutput.contains("Loading file: " + this.jmxPath) && logOutput.contains("Running test") && logOutput.contains("Notifying test listeners of end of test")) {
            test = true;
        } else {
            throw new JMeterManagerException("The test didn't succeed with the given jmx for the session " + sessionID);
        }

        return test;
    }

     /**
     * Kills off this full session 
     * @throws JMeterManagerException 
     */
    @Override
    public void stopTest() throws JMeterManagerException {   
        try {
            container.stop();
            jMeterManager.activeContainers.remove(container);
            jMeterManager.activeSessions.remove(this);
        } catch (DockerManagerException e) {
            throw new JMeterManagerException("Issue with the shutdown of the container and JMeter session " + sessionID);
        } 
    }

    /**
     * @return the exit code from the last executed command of the container
     */
    @Override
    public long getExitCode() throws JMeterManagerException {     
        try {
            return container.getExitCode();
        } catch (DockerManagerException e) {
            throw new JMeterManagerException("Issue with retrieving the latest exit code for session " + sessionID);
        } 
    }

    /**
     * Allows for the connection with the RAS so that all the JMeter-sessions get stored
     */
    private void storeOutput(String file, String content) throws IOException {
        Path requestPath = storedArtifactsRoot.resolve(jmeter).resolve(file);
        Files.write(requestPath, content.getBytes(), new SetContentType(ResultArchiveStoreContentType.TEXT),
                StandardOpenOption.CREATE);
    }

}