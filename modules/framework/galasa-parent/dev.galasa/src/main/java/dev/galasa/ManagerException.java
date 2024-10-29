/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa;

public class ManagerException extends Exception {
    private static final long serialVersionUID = 1L;

    public ManagerException() {
    }

    public ManagerException(String message) {
        super(message);
    }

    public ManagerException(Throwable cause) {
        super(cause);
    }

    public ManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ManagerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
