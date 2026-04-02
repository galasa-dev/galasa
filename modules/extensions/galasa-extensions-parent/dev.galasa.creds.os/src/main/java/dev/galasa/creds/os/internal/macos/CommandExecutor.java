/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal.macos;

import dev.galasa.creds.os.internal.OsCredentialsException;

/**
 * Interface for executing system commands.
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
}
