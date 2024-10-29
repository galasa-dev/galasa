/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.java;

import dev.galasa.ManagerException;

public class JavaManagerException extends ManagerException {
    private static final long serialVersionUID = 1L;

    public JavaManagerException() {
    }

    public JavaManagerException(String message) {
        super(message);
    }

    public JavaManagerException(Throwable cause) {
        super(cause);
    }

    public JavaManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public JavaManagerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
