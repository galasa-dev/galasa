/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.example.docker;

import dev.galasa.ManagerException;

/**
 * DockerManagerException
 * 
 * Exception thrown by the Docker Manager when errors occur.
 */
public class DockerManagerException extends ManagerException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor with message
     * 
     * @param message the error message
     */
    public DockerManagerException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause
     * 
     * @param message the error message
     * @param cause the underlying cause
     */
    public DockerManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with cause
     * 
     * @param cause the underlying cause
     */
    public DockerManagerException(Throwable cause) {
        super(cause);
    }
}

