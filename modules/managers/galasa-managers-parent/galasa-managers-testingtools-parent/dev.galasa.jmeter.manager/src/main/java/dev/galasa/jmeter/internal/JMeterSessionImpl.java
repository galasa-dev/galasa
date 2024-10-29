/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.jmeter.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import dev.galasa.ResultArchiveStoreContentType;
import dev.galasa.SetContentType;
import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.IDockerContainer;
import dev.galasa.docker.IDockerExec;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.jmeter.IJMeterSession;
import dev.galasa.jmeter.JMeterManagerException;

public class JMeterSessionImpl implements IJMeterSession {

    private final IFramework framework;
    private final JMeterManagerImpl jMeterManager;
    private final int sessionID;
    private String jmxPath;
    private String propPath;
    private String jmxAbsolutePath;
    private String propAbsolutePath;
    private String jmeterDockerPath;
    private String jmeter;
    private IDockerContainer container;
    private Path storedArtifactsRoot;
    private Log logger;
    private static final int DEFAULT_TIMER              = 60000;
    
    private static final String STOREDMESSAGE           = " has been stored in the container.";
    private static final String ERRORMESSAGE            = "Could not store the .jmx file correctly.";

    public JMeterSessionImpl(IFramework framework, JMeterManagerImpl jMeterManager, int sessionID, String jmxPath,
            String propPath, IDockerContainer container, Log logger, String jmeter) throws DockerManagerException {
        this.framework = framework;
        this.jMeterManager = jMeterManager;
        this.sessionID = sessionID;
        this.container = container;
        this.jmxPath = jmxPath;
        this.propPath = propPath;
        this.logger = logger;
        this.jmxAbsolutePath = "";
        this.propAbsolutePath = "";
        this.jmeterDockerPath = "/" + jmeter + "/";
        this.jmeter = jmeter;

        storedArtifactsRoot = framework.getResultArchiveStore().getStoredArtifactsRoot();

        container.start();

        logger.info(String.format("Session %d have been succesfully initialised", this.sessionID));
    }

    @Override
    public int getSessionID() {
        return this.sessionID;
    }

    /**
     * Actually executing JMeter with the given JMX that has been set with the default 60s timeout
     */
    @Override
    public void startJmeter() throws JMeterManagerException {
        startJmeter(DEFAULT_TIMER);
    }

    /**
     * Actually executing JMeter with the given JMX that has been set with a specified timeout
     */
    @Override
    public void startJmeter(int timeout) throws JMeterManagerException {

        try {


            String jtlPath = jmxPath.substring(0, jmxPath.indexOf(".jmx")) + ".jtl";
            String logfile = jmxPath.substring(0, jmxPath.indexOf(".jmx")) + ".log";

            if (( this.jmxPath.toLowerCase().endsWith(".jmx") ) && ( !this.jmxAbsolutePath.isEmpty() )) {

                if (this.propAbsolutePath.isEmpty()) {

                    IDockerExec exec = container.exec(timeout, jmeter, "-n", "-t", this.jmxPath, "-l", jtlPath, "-j", logfile);
                    exec.waitForExec(timeout);

                    if ( exec.getExitCode() != 0L ) {
                        logger.info("JMeter commands have failed with exitcode " + exec.getExitCode());
                        throw new JMeterManagerException();
                    }
                } else {
    
                    IDockerExec exec = container.exec(timeout, jmeter, "-n", "-t", this.jmxPath, "-l", jtlPath, "-p", this.propPath, "-j", logfile);
                    exec.waitForExec(timeout); 

                    if ( exec.getExitCode() != 0L) {
                        logger.info("JMeter commands have failed with exitcode " + exec.getExitCode());
                        throw new JMeterManagerException();
                    }
                } 
                storeOutput("jtlOutput_" + this.sessionID + ".txt", getListenerFile(this.jmxPath.substring(0, jmxPath.indexOf(".jmx")) + ".jtl")); 
                storeOutput("logOutput_" + this.sessionID + ".txt", getLogFile()); 
            } else {
                throw new JMeterManagerException("The JmxPath has not been specified correctly of session " + this.sessionID + ".");
            }
        
        } catch (Exception e) {
            throw new JMeterManagerException("JMeter session " + sessionID + " could not be executed properly.",e);
        }
    }

    /**
     * Uses the annotation of JmxPath to store it in the container with the ArtifactManager
     * @param jmxStream
     * @throws JMeterManagerException
    */
    @Override
    public void setDefaultGeneratedJmxFile(InputStream jmxStream) throws JMeterManagerException{
    
        try {
            this.jmxAbsolutePath = jmeterDockerPath + this.jmxPath;
            container.storeFile(this.jmxAbsolutePath, jmxStream);
            logger.info(jmxPath + STOREDMESSAGE);
            
        } catch (Exception e) {
            throw new JMeterManagerException(ERRORMESSAGE,e);
        }
    } 

    @Override
    public void setChangedParametersJmxFile(InputStream jmxStream, Map<String,Object> parameters) throws JMeterManagerException {

        HashMap<String,Object> changes = new HashMap<>();
        changes.put("HOST", "");
        changes.put("PORT", "");
        changes.put("PROTOCOL", "");
        changes.put("PATH", "/");
        changes.put("THREADS", "1");
        changes.put("RAMPUP", "");
        changes.put("DURATION", "15");

        for(Entry<String,Object> entry : parameters.entrySet()) {
            changes.put(entry.getKey(), entry.getValue());
        }
        InputStream safeEOF = new ByteArrayInputStream(" ".getBytes());
        InputStream streamPlus = new SequenceInputStream(jmxStream, safeEOF);
        InputStreamReader ir = new InputStreamReader(streamPlus);

        try {
            Velocity.init();
        } catch (Exception e) {
            throw new JMeterManagerException("Error attempting to initialise velocity", e);
        }

        VelocityContext context = new VelocityContext();

        // Supplied parameters will override our defaults
        for (Entry<String, Object> entry : changes.entrySet()) {
            context.put(entry.getKey(), entry.getValue());
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter ow = new OutputStreamWriter(baos);

        try {
            Velocity.evaluate(context, ow, "VelocityRenderer", ir);
            ow.close();
        } catch (Exception e) {
            throw new JMeterManagerException("Error attempting to process jmx-parameters with velocity", e);
        }
        try {
            this.jmxAbsolutePath = jmeterDockerPath + this.jmxPath;
            container.storeFile(this.jmxAbsolutePath, new ByteArrayInputStream(baos.toByteArray()));
            logger.info(jmxPath + STOREDMESSAGE);
            
        } catch (Exception e) {
            throw new JMeterManagerException(ERRORMESSAGE,e);
        }
    }

    /**
     * If the the property annotation is filled in, the custom property file gets used
     * Uses the annotation of PropPath to store it in the container with the use ArtifactManager
     * @param propStream
     * @throws JMeterManagerException
     */
    @Override
    public void applyProperties(InputStream propStream) throws JMeterManagerException {
        try {
            this.propAbsolutePath = jmeterDockerPath + propPath;
            container.storeFile(this.propAbsolutePath, propStream);

            logger.info(propPath + STOREDMESSAGE);
        } catch (Exception e) {
            throw new JMeterManagerException(ERRORMESSAGE, e);
        }
    }

    @Override
    public void applyProperties(InputStream propStream, Map<String,Object> properties) throws JMeterManagerException {
        InputStream safeEOF = new ByteArrayInputStream(" ".getBytes());
        InputStream streamPlus = new SequenceInputStream(propStream, safeEOF);
        InputStreamReader ir = new InputStreamReader(streamPlus);

        try {
            Velocity.init();
        } catch (Exception e) {
            throw new JMeterManagerException("Error attempting to initialise velocity", e);
        }

        VelocityContext context = new VelocityContext();

        // Supplied parameters will override our defaults
        for (Entry<String, Object> entry : properties.entrySet()) {
            context.put(entry.getKey(), entry.getValue());
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter ow = new OutputStreamWriter(baos);

        try {
            Velocity.evaluate(context, ow, "VelocityRenderer", ir);
            ow.close();
        } catch (Exception e) {
            throw new JMeterManagerException("Error attempting to process properties with velocity", e);
        }

        applyProperties(new ByteArrayInputStream(baos.toByteArray()));
    }
    
     
    /**
     * @return the jmxFile gets returned as a string like "cat" would in a linux container
     * @throws JMeterManagerException 
     */
    @Override
    public String getJmxFile() throws JMeterManagerException {
        try{
            String jmx = jmeterDockerPath + this.jmxPath;
            String jmxStr = "";
            if ( container.isRunning() ) {
                jmxStr = container.retrieveFileAsString(jmx);
            }

            return jmxStr;
        } catch (Exception e) {
            throw new JMeterManagerException("Could not retrieve the jmx file from the container.");
        }
    }

    /**
     * @return the logFile gets returned as a string like "cat" would in a linux container
     * @throws JMeterManagerException 
     */
    @Override
    public String getLogFile() throws JMeterManagerException {
        try{
            String logPath = jmeterDockerPath + this.jmxPath.substring(0, jmxPath.indexOf(".jmx")) + ".log";
            String logAsStr = "";
            if ( container.isRunning() ) {
                logAsStr = container.retrieveFileAsString(logPath);
            }
            
            return logAsStr;
        } catch (Exception e) {
            throw new JMeterManagerException("Could not retrieve the log file from the container.");
        }

    }

    /**
     * @return the consoleOutput gets returned as a string
     * @throws JMeterManagerException 
     */
    @Override
    public String getConsoleOutput() throws JMeterManagerException {
        try{
            String consoleStr = "";
            if ( container.isRunning() ) {
                consoleStr = container.retrieveStdOut();
            }

            return consoleStr;
        } catch (Exception e) {
            throw new JMeterManagerException("Could not retrieve the console file from the container.");
        }
    }

    /**
     * @param fileName
     * @return the specified file gets returned as a string
     * @throws JMeterManagerException 
     */
    @Override
    public String getListenerFile(String fileName) throws JMeterManagerException {
        try{
            String filePath = jmeterDockerPath + fileName;
            String listenerStr = "";
            if ( container.isRunning() ) {        
                listenerStr = container.retrieveFileAsString(filePath);
            }

            return listenerStr;
        } catch (Exception e) {
            throw new JMeterManagerException("Could not retrieve " + fileName + " from the container.");
        }
    }

    /**
     * @return if the test has been performed properly or not
     */
    @Override
    public boolean statusTest() throws JMeterManagerException {
        String logOutput = this.getLogFile();
        boolean test = false;

        if ( logOutput.contains("Loading file: " + this.jmxPath) && logOutput.contains("Running test") && logOutput.contains("Notifying test listeners of end of test") ) {
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
            throw new JMeterManagerException("Issue with the shutdown of the container and JMeter session" + sessionID);
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
            throw new JMeterManagerException("Issue with retrieving the latest exit code" + sessionID);
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