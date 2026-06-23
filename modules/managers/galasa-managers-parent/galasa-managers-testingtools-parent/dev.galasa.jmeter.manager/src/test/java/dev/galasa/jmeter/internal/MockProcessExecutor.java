/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.jmeter.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dev.galasa.jmeter.JMeterManagerException;

/**
 * Mock implementation of IProcessExecutor for testing.
 */
public class MockProcessExecutor implements IProcessExecutor {

    private int exitCode = 0;
    private boolean shouldThrowException = false;
    private String exceptionMessage = "Mock process execution failed";
    private List<List<String>> executedCommands = new ArrayList<>();
    private List<File> executedWorkingDirectories = new ArrayList<>();

    @Override
    public ProcessResult execute(List<String> command, File workingDirectory, long timeoutMs) 
            throws JMeterManagerException {
        
        // Record the command for verification in tests
        executedCommands.add(new ArrayList<>(command));
        executedWorkingDirectories.add(workingDirectory);
        
        if (shouldThrowException) {
            throw new JMeterManagerException(exceptionMessage);
        }
        
        return new ProcessResult(exitCode);
    }

    // Test helper methods

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public void setShouldThrowException(boolean shouldThrow) {
        this.shouldThrowException = shouldThrow;
    }

    public void setExceptionMessage(String message) {
        this.exceptionMessage = message;
    }

    public void setThrowException(JMeterManagerException exception) {
        this.shouldThrowException = true;
        this.exceptionMessage = exception.getMessage();
    }

    public List<List<String>> getExecutedCommands() {
        return new ArrayList<>(executedCommands);
    }

    public List<File> getExecutedWorkingDirectories() {
        return new ArrayList<>(executedWorkingDirectories);
    }

    public void clear() {
        exitCode = 0;
        shouldThrowException = false;
        exceptionMessage = "Mock process execution failed";
        executedCommands.clear();
        executedWorkingDirectories.clear();
    }
}
