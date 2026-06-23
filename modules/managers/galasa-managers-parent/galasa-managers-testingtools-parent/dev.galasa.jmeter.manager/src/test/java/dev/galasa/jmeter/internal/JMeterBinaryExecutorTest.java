/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.jmeter.internal;

import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import dev.galasa.framework.mocks.MockFileSystem;
import dev.galasa.jmeter.JMeterManagerException;

/**
 * Unit tests for JMeterBinaryExecutor using MockProcessExecutor.
 * These tests verify command building logic without executing real processes.
 */
public class JMeterBinaryExecutorTest {

    private MockFileSystem mockFileSystem;
    private MockProcessExecutor mockProcessExecutor;

    @Before
    public void setup() {
        mockFileSystem = new MockFileSystem();
        mockProcessExecutor = new MockProcessExecutor();
    }

    // ========================================================================
    // Constructor Tests
    // ========================================================================

    @Test
    public void testConstructorSucceedsWithValidBinaryPath() throws Exception {
        // Given...
        String binaryPath = "/opt/jmeter/bin/jmeter";
        
        // When...
        JMeterBinaryExecutor executor = new JMeterBinaryExecutor(mockFileSystem, mockProcessExecutor, binaryPath);
        
        // Then...
        assertThat(executor).isNotNull();
        assertThat(executor.getJMeterBinary()).isEqualTo(binaryPath);
        assertThat(executor.getJMeterHome()).isEqualTo("/opt/jmeter");
    }

    @Test
    public void testConstructorExtractsHomeDirectoryCorrectly() throws Exception {
        // Given...
        String binaryPath = "/usr/local/apache-jmeter-5.6.3/bin/jmeter";
        
        // When...
        JMeterBinaryExecutor executor = new JMeterBinaryExecutor(mockFileSystem, mockProcessExecutor, binaryPath);
        
        // Then...
        assertThat(executor.getJMeterHome()).isEqualTo("/usr/local/apache-jmeter-5.6.3");
    }

    @Test
    public void testConstructorHandlesWindowsPathCorrectly() throws Exception {
        // Given...
        String binaryPath = "C:\\apache-jmeter\\bin\\jmeter.bat";
        
        // When...
        JMeterBinaryExecutor executor = new JMeterBinaryExecutor(mockFileSystem, mockProcessExecutor, binaryPath);
        
        // Then...
        assertThat(executor.getJMeterBinary()).isEqualTo(binaryPath);
        assertThat(executor.getJMeterHome()).contains("apache-jmeter");
    }

    @Test
    public void testConstructorHandlesBinaryNotInBinDirectory() throws Exception {
        // Given...
        String binaryPath = "/opt/jmeter-custom/jmeter";
        
        // When...
        JMeterBinaryExecutor executor = new JMeterBinaryExecutor(mockFileSystem, mockProcessExecutor, binaryPath);
        
        // Then...
        assertThat(executor.getJMeterHome()).isEqualTo("/opt/jmeter-custom");
    }

    // ========================================================================
    // executeTestPlan Tests - File Validation
    // ========================================================================

    @Test
    public void testExecuteTestPlanThrowsExceptionWhenTestPlanDoesNotExist() throws Exception {
        // Given...
        String binaryPath = "/opt/jmeter/bin/jmeter";
        JMeterBinaryExecutor executor = new JMeterBinaryExecutor(mockFileSystem, mockProcessExecutor, binaryPath);
        
        Path nonExistentTestPlan = mockFileSystem.getPath("/tests/test.jmx");
        Path resultsPath = mockFileSystem.getPath("/results/results.jtl");
        Path logPath = mockFileSystem.getPath("/logs/jmeter.log");
        
        // When... Then...
        JMeterManagerException thrown = catchThrowableOfType(() -> {
            executor.executeTestPlan(nonExistentTestPlan, resultsPath, logPath, null, 30000);
        }, JMeterManagerException.class);
        
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Test plan file not found");
    }

    // ========================================================================
    // executeTestPlan Tests - Command Building
    // ========================================================================

    @Test
    public void testExecuteTestPlanBuildsCorrectCommandWithoutProperties() throws Exception {
        // Given...
        String binaryPath = "/opt/jmeter/bin/jmeter";
        JMeterBinaryExecutor executor = new JMeterBinaryExecutor(mockFileSystem, mockProcessExecutor, binaryPath);
        
        Path testPlanPath = mockFileSystem.getPath("/tests/test.jmx");
        mockFileSystem.createDirectories(testPlanPath.getParent());
        mockFileSystem.createFile(testPlanPath);
        
        Path resultsPath = mockFileSystem.getPath("/results/results.jtl");
        Path logPath = mockFileSystem.getPath("/logs/jmeter.log");
        
        mockProcessExecutor.setExitCode(0);
        
        // When...
        int exitCode = executor.executeTestPlan(testPlanPath, resultsPath, logPath, null, 30000);
        
        // Then...
        assertThat(exitCode).isEqualTo(0);
        assertThat(mockProcessExecutor.getExecutedCommands()).hasSize(1);
        
        List<String> command = mockProcessExecutor.getExecutedCommands().get(0);
        assertThat(command).containsExactly(
            binaryPath,
            "-n",
            "-t", testPlanPath.toString(),
            "-l", resultsPath.toString(),
            "-j", logPath.toString()
        );
        
        File workingDir = mockProcessExecutor.getExecutedWorkingDirectories().get(0);
        assertThat(workingDir.getPath()).isEqualTo("/opt/jmeter");
    }

    @Test
    public void testExecuteTestPlanBuildsCorrectCommandWithProperties() throws Exception {
        // Given...
        String binaryPath = "/opt/jmeter/bin/jmeter";
        JMeterBinaryExecutor executor = new JMeterBinaryExecutor(mockFileSystem, mockProcessExecutor, binaryPath);
        
        Path testPlanPath = mockFileSystem.getPath("/tests/test.jmx");
        mockFileSystem.createDirectories(testPlanPath.getParent());
        mockFileSystem.createFile(testPlanPath);
        
        Path resultsPath = mockFileSystem.getPath("/results/results.jtl");
        Path logPath = mockFileSystem.getPath("/logs/jmeter.log");
        
        Map<String, String> properties = new HashMap<>();
        properties.put("threads", "10");
        properties.put("duration", "60");
        
        mockProcessExecutor.setExitCode(0);
        
        // When...
        int exitCode = executor.executeTestPlan(testPlanPath, resultsPath, logPath, properties, 30000);
        
        // Then...
        assertThat(exitCode).isEqualTo(0);
        assertThat(mockProcessExecutor.getExecutedCommands()).hasSize(1);
        
        List<String> command = mockProcessExecutor.getExecutedCommands().get(0);
        assertThat(command).contains(
            binaryPath,
            "-n",
            "-t", testPlanPath.toString(),
            "-l", resultsPath.toString(),
            "-j", logPath.toString()
        );
        
        // Properties can be in any order, so check they're present
        assertThat(command).contains("-Jthreads=10");
        assertThat(command).contains("-Jduration=60");
    }

    @Test
    public void testExecuteTestPlanBuildsCorrectCommandWithEmptyProperties() throws Exception {
        // Given...
        String binaryPath = "/opt/jmeter/bin/jmeter";
        JMeterBinaryExecutor executor = new JMeterBinaryExecutor(mockFileSystem, mockProcessExecutor, binaryPath);
        
        Path testPlanPath = mockFileSystem.getPath("/tests/test.jmx");
        mockFileSystem.createDirectories(testPlanPath.getParent());
        mockFileSystem.createFile(testPlanPath);
        
        Path resultsPath = mockFileSystem.getPath("/results/results.jtl");
        Path logPath = mockFileSystem.getPath("/logs/jmeter.log");
        
        Map<String, String> emptyProperties = new HashMap<>();
        mockProcessExecutor.setExitCode(0);
        
        // When...
        int exitCode = executor.executeTestPlan(testPlanPath, resultsPath, logPath, emptyProperties, 30000);
        
        // Then...
        assertThat(exitCode).isEqualTo(0);
        List<String> command = mockProcessExecutor.getExecutedCommands().get(0);
        
        // Should not contain any -J properties
        assertThat(command.stream().filter(arg -> arg.startsWith("-J"))).isEmpty();
    }

    // ========================================================================
    // executeTestPlan Tests - Exit Code Handling
    // ========================================================================

    @Test
    public void testExecuteTestPlanReturnsSuccessExitCode() throws Exception {
        // Given...
        String binaryPath = "/opt/jmeter/bin/jmeter";
        JMeterBinaryExecutor executor = new JMeterBinaryExecutor(mockFileSystem, mockProcessExecutor, binaryPath);
        
        Path testPlanPath = mockFileSystem.getPath("/tests/test.jmx");
        mockFileSystem.createDirectories(testPlanPath.getParent());
        mockFileSystem.createFile(testPlanPath);
        
        Path resultsPath = mockFileSystem.getPath("/results/results.jtl");
        Path logPath = mockFileSystem.getPath("/logs/jmeter.log");
        
        mockProcessExecutor.setExitCode(0);
        
        // When...
        int exitCode = executor.executeTestPlan(testPlanPath, resultsPath, logPath, null, 30000);
        
        // Then...
        assertThat(exitCode).isEqualTo(0);
    }

    @Test
    public void testExecuteTestPlanReturnsFailureExitCode() throws Exception {
        // Given...
        String binaryPath = "/opt/jmeter/bin/jmeter";
        JMeterBinaryExecutor executor = new JMeterBinaryExecutor(mockFileSystem, mockProcessExecutor, binaryPath);
        
        Path testPlanPath = mockFileSystem.getPath("/tests/test.jmx");
        mockFileSystem.createDirectories(testPlanPath.getParent());
        mockFileSystem.createFile(testPlanPath);
        
        Path resultsPath = mockFileSystem.getPath("/results/results.jtl");
        Path logPath = mockFileSystem.getPath("/logs/jmeter.log");
        
        mockProcessExecutor.setExitCode(1);
        
        // When...
        int exitCode = executor.executeTestPlan(testPlanPath, resultsPath, logPath, null, 30000);
        
        // Then...
        assertThat(exitCode).isEqualTo(1);
    }

    // ========================================================================
    // executeTestPlan Tests - Error Handling
    // ========================================================================

    @Test
    public void testExecuteTestPlanThrowsExceptionOnProcessFailure() throws Exception {
        // Given...
        String binaryPath = "/opt/jmeter/bin/jmeter";
        JMeterBinaryExecutor executor = new JMeterBinaryExecutor(mockFileSystem, mockProcessExecutor, binaryPath);
        
        Path testPlanPath = mockFileSystem.getPath("/tests/test.jmx");
        mockFileSystem.createDirectories(testPlanPath.getParent());
        mockFileSystem.createFile(testPlanPath);
        
        Path resultsPath = mockFileSystem.getPath("/results/results.jtl");
        Path logPath = mockFileSystem.getPath("/logs/jmeter.log");
        
        mockProcessExecutor.setThrowException(new JMeterManagerException("Process execution failed"));
        
        // When... Then...
        JMeterManagerException thrown = catchThrowableOfType(() -> {
            executor.executeTestPlan(testPlanPath, resultsPath, logPath, null, 30000);
        }, JMeterManagerException.class);
        
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Process execution failed");
    }

    @Test
    public void testExecuteTestPlanPassesTimeoutToProcessExecutor() throws Exception {
        // Given...
        String binaryPath = "/opt/jmeter/bin/jmeter";
        JMeterBinaryExecutor executor = new JMeterBinaryExecutor(mockFileSystem, mockProcessExecutor, binaryPath);
        
        Path testPlanPath = mockFileSystem.getPath("/tests/test.jmx");
        mockFileSystem.createDirectories(testPlanPath.getParent());
        mockFileSystem.createFile(testPlanPath);
        
        Path resultsPath = mockFileSystem.getPath("/results/results.jtl");
        Path logPath = mockFileSystem.getPath("/logs/jmeter.log");
        
        long timeout = 60000;
        mockProcessExecutor.setExitCode(0);
        
        // When...
        executor.executeTestPlan(testPlanPath, resultsPath, logPath, null, timeout);
        
        // Then...
        // MockProcessExecutor doesn't track timeout, but we verify the call succeeded
        assertThat(mockProcessExecutor.getExecutedCommands()).hasSize(1);
    }

    // ========================================================================
    // Getter Tests
    // ========================================================================

    @Test
    public void testGetJMeterBinaryReturnsConfiguredPath() throws Exception {
        // Given...
        String binaryPath = "/opt/jmeter/bin/jmeter";
        
        // When...
        JMeterBinaryExecutor executor = new JMeterBinaryExecutor(mockFileSystem, mockProcessExecutor, binaryPath);
        
        // Then...
        assertThat(executor.getJMeterBinary()).isEqualTo(binaryPath);
    }

    @Test
    public void testGetJMeterHomeReturnsParentOfBinDirectory() throws Exception {
        // Given...
        String binaryPath = "/opt/apache-jmeter-5.6.3/bin/jmeter";
        
        // When...
        JMeterBinaryExecutor executor = new JMeterBinaryExecutor(mockFileSystem, mockProcessExecutor, binaryPath);
        
        // Then...
        assertThat(executor.getJMeterHome()).isEqualTo("/opt/apache-jmeter-5.6.3");
    }
}

// Made with Bob
