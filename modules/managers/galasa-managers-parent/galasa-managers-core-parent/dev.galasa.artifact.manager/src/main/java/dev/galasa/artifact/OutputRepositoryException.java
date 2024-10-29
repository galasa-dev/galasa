/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.artifact;

import dev.galasa.ManagerException;

public class OutputRepositoryException extends ManagerException {
    private static final long serialVersionUID = 1L;

    public OutputRepositoryException() {
    }

    public OutputRepositoryException(String message) {
        super(message);
    }

    public OutputRepositoryException(Throwable throwable) {
        super(throwable);
    }

    public OutputRepositoryException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
