/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts;

import dev.galasa.ManagerException;

public class CicstsManagerException extends ManagerException {
    private static final long serialVersionUID = 1L;

    public CicstsManagerException() {
    }

    public CicstsManagerException(String message) {
        super(message);
    }

    public CicstsManagerException(Throwable cause) {
        super(cause);
    }

    public CicstsManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public CicstsManagerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
