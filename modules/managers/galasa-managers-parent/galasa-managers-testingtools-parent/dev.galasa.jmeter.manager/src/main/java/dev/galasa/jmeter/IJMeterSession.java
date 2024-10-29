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
     * This method gets called before start of a session providing necessary properties
     * to the JMeter session
     * @param propStream An inputstream of the properties file
     * @throws JMeterManagerException
     */
    public void applyProperties(InputStream propStream) throws JMeterManagerException;

    /**
     * This method gets called before start of a session providing necessary properties
     * to the JMeter session
     * The properties hashmap adds upon the propStream input with personalised properties
     * @param propStream An inputstream of the properties file
     * @param properties A hashmap of dynamic properties
     * @throws JMeterManagerException
     */
    public void applyProperties(InputStream propStream, Map<String,Object> properties) throws JMeterManagerException;

    /**
     * Start up a jmeter thread to run through the lifetime of the tests with a default timeout of 60 seconds
     * All results are stored in the RAS
     * @throws JMeterManagerException
     */
    public void startJmeter() throws JMeterManagerException;

    /**
     * Start up a jmeter thread to run through the lifetime of the tests with a specified timeout
     * All results are stored in the RAS
     * @throws JMeterManagerException
     */
    public void startJmeter(int timeout) throws JMeterManagerException;

    /**
     * Allows the tester to provide a jmxFile to the running session
     * This method is provided to run static JMX-files that are NOT dynamic with the current session it is running in
     * @param jmxStream
     * @throws JMeterManagerException
     */
    public void setDefaultGeneratedJmxFile(InputStream jmxStream) throws JMeterManagerException;

    /**
     * Allows the tester to provide a jmxFile to the running session
     * This method is provided to run dynamic JMX-files that are dynamic with the current session it is running in
     * 
     * JMX-files have to be prepared for this by replacing ALL the $P__VARIABLE notations with $VARIABLE notations
     * Then by adding neccesary parameters inside a HashMap with it, it is possible to change the target of a test dynamically inside the test
     * Example:
     * <PRE>
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("HOST", "galasa.dev");
        session.setChangedParametersJmxFile(jmxStream, map);
     </PRE>
     * @param jmxStream
     * @param parameters
     * @throws JMeterManagerException
     */
    public void setChangedParametersJmxFile(InputStream jmxStream, Map<String,Object> parameters) throws JMeterManagerException;
    
    /**
     * Returning the jmxFile in UTF-8 encoding in String format
     * The working directory is found through the session instance
     * @throws JMeterManagerException
     */
    public String getJmxFile() throws JMeterManagerException;

    /**
     * Returning the logFile in UTF-8 encoding in String format
     * The working directory is found through the session instance
     * @throws JMeterManagerException
     */
    public String getLogFile() throws JMeterManagerException;

    /**
     * Returns the consoleOutput in String format
     * @return String of console
     * @throws JMeterManagerException
     */
    public String getConsoleOutput() throws JMeterManagerException;

    /**
     * Return the output file of your jmx execution in String format 
     * The working directory is found through the session instance
     * @param fileName the ListenerFile
     * @throws JMeterManagerException
     */
    public String getListenerFile(String fileName) throws JMeterManagerException;

    /**
     * 
     * @return the logFile gets returned as a string like "cat" would in a linux container
     * @throws JMeterManagerException
     */
    public boolean statusTest() throws JMeterManagerException;

    /**
     * Giving jmeter instance a shutdown signal to finish and clean up all running tests
     * @throws JMeterManagerException
     */
    public void stopTest() throws JMeterManagerException;

    /**
     * Returns the exit code of the the shutdown of the JMeterProcess
     * @return exitcode as a long
     * @throws JMeterManagerException
     */
    public long getExitCode() throws JMeterManagerException;

    public int getSessionID();
       
}