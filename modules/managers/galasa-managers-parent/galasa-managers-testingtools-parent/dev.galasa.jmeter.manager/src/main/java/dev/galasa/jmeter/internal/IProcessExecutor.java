/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.jmeter.internal;

import java.io.File;
import java.util.List;

import dev.galasa.jmeter.JMeterManagerException;

/**
 * Interface for executing system processes.
 */
public interface IProcessExecutor {
    
    /**
     * Executes a command and waits for it to complete.
     * 
     * @param command the command and arguments to execute
     * @param workingDirectory the working directory for the process
     * @param timeoutMs timeout in milliseconds (0 for no timeout)
     * @return the process result containing exit code
     * @throws JMeterManagerException if there's an error executing the command
     */
    ProcessResult execute(List<String> command, File workingDirectory, long timeoutMs) 
            throws JMeterManagerException;
}
