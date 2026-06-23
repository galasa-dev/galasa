/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.jmeter.internal;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dev.galasa.jmeter.JMeterManagerException;

/**
 * Real implementation of IProcessExecutor using ProcessBuilder.
 */
public class ProcessExecutorImpl implements IProcessExecutor {

    @Override
    public ProcessResult execute(List<String> command, File workingDirectory, long timeoutMs) 
            throws JMeterManagerException {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(workingDirectory);
            processBuilder.inheritIO();
            
            Process process = processBuilder.start();
            
            boolean completed;
            if (timeoutMs > 0) {
                completed = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
                if (!completed) {
                    process.destroyForcibly();
                    throw new JMeterManagerException(
                        "JMeter process timed out after " + timeoutMs + "ms");
                }
            } else {
                process.waitFor();
            }
            
            int exitCode = process.exitValue();
            return new ProcessResult(exitCode);
            
        } catch (IOException e) {
            throw new JMeterManagerException("Failed to start JMeter process", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JMeterManagerException("JMeter process was interrupted", e);
        }
    }
}
