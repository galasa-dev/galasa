/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal.macos;

import dev.galasa.creds.os.internal.OsCredentialsException;

/**
 * Interface for executing system commands.
 * Allows for testing by providing a mock implementation.
 */
public interface CommandExecutor {
    
    /**
     * Executes a command and returns the result.
     * 
     * @param command the command and arguments to execute
     * @return the command result
     * @throws OsCredentialsException if there's an error executing the command
     */
    CommandResult execute(String... command) throws OsCredentialsException;
    
    /**
     * Result of a command execution.
     */
    class CommandResult {
        private final int exitCode;
        private final String output;
        
        public CommandResult(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }
        
        public int getExitCode() {
            return exitCode;
        }
        
        public String getOutput() {
            return output;
        }
    }
}

// Made with Bob
