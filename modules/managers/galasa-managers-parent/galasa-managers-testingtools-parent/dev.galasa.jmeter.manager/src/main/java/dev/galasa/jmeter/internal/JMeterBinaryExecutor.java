/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.jmeter.internal;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.IFileSystem;
import dev.galasa.jmeter.JMeterManagerException;

/**
 * Executes JMeter tests using an external JMeter binary installation.
 * This class is responsible for building JMeter commands and delegating
 * process execution to an IProcessExecutor implementation.
 */
public class JMeterBinaryExecutor {
    
    private static final Log logger = LogFactory.getLog(JMeterBinaryExecutor.class);
    
    private final IFileSystem fileSystem;
    private final IProcessExecutor processExecutor;
    private final String jmeterHome;
    private final String jmeterBinary;
    
    /**
     * Constructor to instantiate the JMeter binary executor.
     *
     * @param fileSystem The filesystem abstraction to use for file operations
     * @param processExecutor The process executor to use for running JMeter
     * @param binaryPathStr The path to the JMeter binary (must be pre-validated)
     */
    public JMeterBinaryExecutor(IFileSystem fileSystem, IProcessExecutor processExecutor, String binaryPathStr) {
        this.fileSystem = fileSystem;
        this.processExecutor = processExecutor;
        Path binaryPath = Paths.get(binaryPathStr);
        
        this.jmeterBinary = binaryPath.toString();
        this.jmeterHome = extractJMeterHome(binaryPath);
        
        logInitialization();
    }
    
    /**
     * Extract JMeter home directory from the binary path
     * Expects binary to be in JMETER_HOME/bin/ directory
     *
     * @param binaryPath The path to the JMeter binary
     * @return The JMeter home directory path
     */
    private String extractJMeterHome(Path binaryPath) {
        Path binDir = binaryPath.getParent();
        if (binDir != null && binDir.getFileName().toString().equals("bin")) {
            Path homeDir = binDir.getParent();
            return homeDir != null ? homeDir.toString() : binaryPath.toString();
        } else {
            return binDir != null ? binDir.toString() : binaryPath.toString();
        }
    }
    
    /**
     * Log initialization details
     */
    private void logInitialization() {
        logger.info("JMeter binary executor initialized");
        logger.info("  Binary: " + jmeterBinary);
        logger.info("  Home: " + jmeterHome);
    }
    
    /**
     * Execute a JMeter test plan with timeout
     *
     * @param testPlanPath Path to the JMX test plan file
     * @param resultsPath Path where results should be written
     * @param logPath Path where JMeter log should be written
     * @param properties Additional JMeter properties to set
     * @param timeoutMillis Maximum time in milliseconds to wait for completion
     * @return Exit code from JMeter execution
     * @throws JMeterManagerException if execution fails or times out
     */
    public int executeTestPlan(Path testPlanPath, Path resultsPath, Path logPath, Map<String, String> properties, long timeoutMillis)
            throws JMeterManagerException {
        
        // Validate test plan exists using IFileSystem
        if (!fileSystem.exists(testPlanPath)) {
            throw new JMeterManagerException("Test plan file not found: " + testPlanPath);
        }
        
        List<String> command = buildCommand(testPlanPath, resultsPath, logPath, properties);
        
        logger.info("Executing JMeter command: " + String.join(" ", command));
        
        // Delegate process execution to the process executor
        ProcessResult result = processExecutor.execute(command, new File(jmeterHome), timeoutMillis);
        
        int exitCode = result.getExitCode();
        
        if (exitCode == 0) {
            logger.info("JMeter execution completed successfully");
        } else {
            logger.warn("JMeter execution completed with exit code: " + exitCode);
        }
        
        return exitCode;
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
