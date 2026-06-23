/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.jmeter.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import dev.galasa.framework.FileSystem;
import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.jmeter.JMeterManagerException;
import dev.galasa.jmeter.internal.properties.JMeterBinaryPath;

/**
 * Implementation of IJMeterSession that uses an external JMeter binary.
 */
public class JMeterLocalSessionImpl extends AbstractJMeterSession {
    
    private final Path workingDirectory;
    private final JMeterBinaryExecutor executor;
    private final IFileSystem fileSystem;
    
    private Path jmxFilePath;
    private Path resultsFilePath;
    private Path logFilePath;
    private Map<String, String> jmeterProperties = new HashMap<>();
    
    private int exitCode = -1;
    private boolean testStarted = false;
    
    public JMeterLocalSessionImpl(int sessionID, IFramework framework, Path workingDirectory,
            String jmxPath, String propPath) throws JMeterManagerException {
        super(sessionID, framework);
        this.workingDirectory = workingDirectory;
        this.fileSystem = new FileSystem();
        
        // Get binary path from CPS
        String binaryPath;
        try {
            binaryPath = JMeterBinaryPath.get();
        } catch (ConfigurationPropertyStoreException e) {
            throw new JMeterManagerException("Failed to retrieve JMeter binary path from CPS", e);
        }
        
        // Create validator and validate binary before creating executor
        IJMeterBinaryValidator validator = new JMeterBinaryValidator(fileSystem);
        validator.validate(Paths.get(binaryPath));
        
        // Create process executor and binary executor with validated path
        IProcessExecutor processExecutor = new ProcessExecutorImpl();
        this.executor = new JMeterBinaryExecutor(fileSystem, processExecutor, binaryPath);
        
        // Initialize file paths using original filenames
        this.jmxFilePath = workingDirectory.resolve(jmxPath);
        
        // Derive results and log filenames from JMX filename using base class utility
        String baseFilename = deriveBaseFilename(jmxPath);
        this.resultsFilePath = workingDirectory.resolve(baseFilename + JMeterConstants.JTL_EXTENSION);
        this.logFilePath = workingDirectory.resolve(baseFilename + JMeterConstants.LOG_EXTENSION);
        
        logger.info("JMeter local session " + sessionID + " initialized with working directory: " + workingDirectory);
        logger.info("JMX file: " + jmxPath + ", Results: " + baseFilename + JMeterConstants.JTL_EXTENSION + 
                    ", Log: " + baseFilename + JMeterConstants.LOG_EXTENSION);
    }

    @Override
    public void applyProperties(InputStream propStream, Map<String, Object> properties) 
            throws JMeterManagerException {
        try {
            // Load properties from stream
            Properties props = new Properties();
            if (propStream != null) {
                props.load(propStream);
                for (String key : props.stringPropertyNames()) {
                    jmeterProperties.put(key, props.getProperty(key));
                }
            }
            
            // Add additional properties
            if (properties != null) {
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    jmeterProperties.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }
            
            logger.info("Applied " + jmeterProperties.size() + " properties to JMeter session");
            
        } catch (IOException e) {
            throw new JMeterManagerException("Failed to load JMeter properties", e);
        }
    }

    @Override
    public void startJmeter(int timeout) throws JMeterManagerException {
        validateTimeout(timeout);
        
        if (testStarted) {
            throw new JMeterManagerException("JMeter test has already been started for session " + sessionID);
        }
        
        if (jmxFilePath == null || !fileSystem.exists(jmxFilePath)) {
            throw new JMeterManagerException("No JMX test plan file has been set for session " + sessionID + ". Call setDefaultGeneratedJmxFile() first.");
        }
        
        logger.info("Starting JMeter test execution for session " + sessionID + " with timeout " + timeout + "ms");
        
        try {
            // Execute JMeter binary with timeout
            exitCode = executor.executeTestPlan(jmxFilePath, resultsFilePath, logFilePath, jmeterProperties, timeout);
            testStarted = true;
            
            // Store results in RAS
            storeResultsInRAS();
            
            logger.info("JMeter test execution completed with exit code: " + exitCode);
            
        } catch (JMeterManagerException e) {
            throw e;
        } catch (Exception e) {
            throw new JMeterManagerException("Failed to execute JMeter test for session " + sessionID, e);
        }
    }

    @Override
    public void setDefaultGeneratedJmxFile(InputStream jmxStream) throws JMeterManagerException {
        validateInputStream(jmxStream, "JMX stream");
        try {
            // Write JMX content to file
            byte[] jmxContent = jmxStream.readAllBytes();
            Files.write(jmxFilePath, jmxContent);
            logger.info("JMX test plan written to: " + jmxFilePath);
            
        } catch (IOException e) {
            throw new JMeterManagerException("Failed to write JMX file for session " + sessionID, e);
        }
    }

    @Override
    public void setChangedParametersJmxFile(InputStream jmxStream, Map<String, Object> parameters)
            throws JMeterManagerException {
        validateInputStream(jmxStream, "JMX stream");
        try {
            // Use base class utility to process JMX template with Velocity
            String processedJmx = processJmxTemplate(jmxStream, parameters);
            
            // Write processed JMX to file
            Files.writeString(jmxFilePath, processedJmx, StandardCharsets.UTF_8);
            logger.info("Parameterized JMX test plan written to: " + jmxFilePath);
            
        } catch (JMeterManagerException e) {
            throw e;
        } catch (IOException e) {
            throw new JMeterManagerException("Failed to process parameterized JMX file for session " + sessionID, e);
        }
    }

    @Override
    public String getJmxFile() throws JMeterManagerException {
        return readFileContent(jmxFilePath);
    }

    @Override
    public String getLogFile() throws JMeterManagerException {
        return readFileContent(logFilePath);
    }

    @Override
    public String getConsoleOutput() throws JMeterManagerException {
        // Console output is captured in the log file
        return getLogFile();
    }

    @Override
    public String getListenerFile(String fileName) throws JMeterManagerException {
        validateFileName(fileName);
        Path listenerFile = workingDirectory.resolve(fileName);
        return readFileContent(listenerFile);
    }

    @Override
    public boolean isTestSuccessful() throws JMeterManagerException {
        return exitCode == 0;
    }

    @Override
    public void stopTest() throws JMeterManagerException {
        // For binary execution, the test runs to completion
        // This method is a no-op for local binary execution
        logger.info("Stop test called for session " + sessionID + " (binary execution completes automatically)");
    }

    @Override
    public long getExitCode() throws JMeterManagerException {
        return exitCode;
    }
    
    /**
     * Read file content as string
     */
    private String readFileContent(Path filePath) throws JMeterManagerException {
        try {
            if (!Files.exists(filePath)) {
                throw new JMeterManagerException("File not found for session " + sessionID + ": " + filePath);
            }
            return Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new JMeterManagerException("Failed to read file for session " + sessionID + ": " + filePath, e);
        }
    }
    
    /**
     * Store results in Result Archive Store (RAS).
     * RAS uses a special filesystem, so we use java.nio.file.Files methods on RAS paths.
     *
     * @throws JMeterManagerException if RAS storage fails critically
     */
    private void storeResultsInRAS() throws JMeterManagerException {
        try {
            // Get RAS path for this session
            Path rasRoot = framework.getResultArchiveStore().getStoredArtifactsRoot();
            Path jmeterRasDir = rasRoot.resolve(JMeterConstants.JMETER_DIRECTORY)
                                       .resolve("session-" + sessionID);
            
            // Create RAS directory structure
            Files.createDirectories(jmeterRasDir);
            
            // Map source files to their RAS target names
            Map<Path, String> filesToStore = new LinkedHashMap<>();
            filesToStore.put(resultsFilePath, JMeterConstants.RAS_RESULTS_FILE);
            filesToStore.put(logFilePath, JMeterConstants.RAS_LOG_FILE);
            filesToStore.put(jmxFilePath, JMeterConstants.RAS_JMX_FILE);
            
            // Copy all files and collect failures
            List<String> failures = new ArrayList<>();
            for (Map.Entry<Path, String> entry : filesToStore.entrySet()) {
                Path sourceFile = entry.getKey();
                String targetName = entry.getValue();
                Path targetPath = jmeterRasDir.resolve(targetName);
                
                if (!copyFileToRAS(sourceFile, targetPath, targetName)) {
                    failures.add(targetName);
                }
            }
            
            // Report results
            if (failures.isEmpty()) {
                logger.info("All results successfully stored in RAS: " + jmeterRasDir);
            } else {
                logger.warn("Partial RAS storage failure for session " + sessionID +
                           ": Failed to store " + String.join(", ", failures));
            }
            
        } catch (Exception e) {
            String errorMsg = "Critical failure storing results in RAS for session " + sessionID;
            logger.error(errorMsg, e);
            throw new JMeterManagerException(errorMsg, e);
        }
    }
    
    /**
     * Copy a file to RAS if it exists, logging success or failure.
     *
     * @param sourceFile The source file path
     * @param targetFile The target RAS file path
     * @param description Human-readable description for logging
     * @return true if copy succeeded or file doesn't exist, false if copy failed
     */
    private boolean copyFileToRAS(Path sourceFile, Path targetFile, String description) {
        if (!Files.exists(sourceFile)) {
            logger.debug(description + " does not exist, skipping: " + sourceFile);
            return true; // Not an error if file doesn't exist
        }
        
        try {
            Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Stored " + description + " in RAS");
            return true;
        } catch (Exception e) {
            logger.warn("Failed to store " + description + " in RAS", e);
            return false;
        }
    }
}
