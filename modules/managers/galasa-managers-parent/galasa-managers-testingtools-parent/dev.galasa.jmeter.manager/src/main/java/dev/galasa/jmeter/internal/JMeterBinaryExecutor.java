/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.jmeter.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.jmeter.JMeterManagerException;
import dev.galasa.jmeter.internal.properties.JMeterBinaryPath;

/**
 * Executes JMeter tests using an external JMeter binary installation.
 *
 */
public class JMeterBinaryExecutor {
    
    private static final Log logger = LogFactory.getLog(JMeterBinaryExecutor.class);
    
    private final String jmeterHome;
    private final String jmeterBinary;
    
    public JMeterBinaryExecutor() throws JMeterManagerException {
        try {
            String configuredPath = JMeterBinaryPath.get();
            
            // Check if the configured path is directly to the binary or to JMeter home
            File configuredFile = new File(configuredPath);
            
            if (configuredFile.isFile() && configuredFile.canExecute()) {
                // Direct path to binary (e.g., /opt/homebrew/bin/jmeter)
                this.jmeterBinary = configuredPath;
                // Extract home directory (parent of bin directory)
                File binDir = configuredFile.getParentFile();
                if (binDir != null && binDir.getName().equals("bin")) {
                    this.jmeterHome = binDir.getParent();
                } else {
                    this.jmeterHome = binDir != null ? binDir.getAbsolutePath() : configuredPath;
                }
                logger.info("Using direct JMeter binary path: " + jmeterBinary);
            } else if (configuredFile.isDirectory()) {
                // Path to JMeter home directory (e.g., /opt/apache-jmeter-5.6.3)
                this.jmeterHome = configuredPath;
                
                // Determine the JMeter binary path based on OS
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    this.jmeterBinary = Paths.get(jmeterHome, "bin", "jmeter.bat").toString();
                } else {
                    this.jmeterBinary = Paths.get(jmeterHome, "bin", "jmeter").toString();
                }
                
                // Validate that the binary exists
                File binaryFile = new File(jmeterBinary);
                if (!binaryFile.exists()) {
                    throw new JMeterManagerException(
                        "JMeter binary not found at: " + jmeterBinary +
                        ". Please ensure jmeter.binary.path points to either:\n" +
                        "  1. A JMeter home directory (e.g., /opt/apache-jmeter-5.6.3), or\n" +
                        "  2. The direct path to the jmeter binary (e.g., /opt/homebrew/bin/jmeter)"
                    );
                }
                
                if (!binaryFile.canExecute()) {
                    throw new JMeterManagerException(
                        "JMeter binary is not executable: " + jmeterBinary +
                        ". Please check file permissions."
                    );
                }
                logger.info("Using JMeter home directory: " + jmeterHome);
            } else {
                throw new JMeterManagerException(
                    "Invalid jmeter.binary.path: " + configuredPath +
                    ". Path must be either:\n" +
                    "  1. A JMeter home directory (e.g., /opt/apache-jmeter-5.6.3), or\n" +
                    "  2. The direct path to the jmeter binary (e.g., /opt/homebrew/bin/jmeter)"
                );
            }
            
            logger.info("JMeter binary executor initialized with binary: " + jmeterBinary);
            
        } catch (ConfigurationPropertyStoreException e) {
            throw new JMeterManagerException("Failed to retrieve JMeter binary path from CPS", e);
        }
    }
    
    /**
     * Execute a JMeter test plan
     * 
     * @param testPlanPath Path to the JMX test plan file
     * @param resultsPath Path where results should be written
     * @param logPath Path where JMeter log should be written
     * @param properties Additional JMeter properties to set
     * @return Exit code from JMeter execution
     * @throws JMeterManagerException if execution fails
     */
    public int executeTestPlan(Path testPlanPath, Path resultsPath, Path logPath, Map<String, String> properties) 
            throws JMeterManagerException {
        
        if (!Files.exists(testPlanPath)) {
            throw new JMeterManagerException("Test plan file not found: " + testPlanPath);
        }
        
        List<String> command = buildCommand(testPlanPath, resultsPath, logPath, properties);
        
        logger.info("Executing JMeter command: " + String.join(" ", command));
        
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(new File(jmeterHome));
            processBuilder.redirectErrorStream(true);
            
            Process process = processBuilder.start();
            
            // Capture and log output
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info("[JMeter] " + line);
                }
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                logger.info("JMeter execution completed successfully");
            } else {
                logger.warn("JMeter execution completed with exit code: " + exitCode);
            }
            
            return exitCode;
            
        } catch (IOException e) {
            throw new JMeterManagerException("Failed to execute JMeter binary", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JMeterManagerException("JMeter execution was interrupted", e);
        }
    }
    
    /**
     * Build the command line for JMeter execution
     */
    private List<String> buildCommand(Path testPlanPath, Path resultsPath, Path logPath, 
            Map<String, String> properties) {
        
        List<String> command = new ArrayList<>();
        command.add(jmeterBinary);
        command.add("-n"); // Non-GUI mode
        command.add("-t"); // Test plan
        command.add(testPlanPath.toString());
        command.add("-l"); // Log file (results)
        command.add(resultsPath.toString());
        command.add("-j"); // JMeter log file
        command.add(logPath.toString());
        
        // Add any additional properties
        if (properties != null && !properties.isEmpty()) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                command.add("-J" + entry.getKey() + "=" + entry.getValue());
            }
        }
        
        return command;
    }
    
    /**
     * Get the JMeter home directory
     */
    public String getJMeterHome() {
        return jmeterHome;
    }
    
    /**
     * Get the JMeter binary path
     */
    public String getJMeterBinary() {
        return jmeterBinary;
    }
}
