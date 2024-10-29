/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts;

public class CeciManagerException extends CicstsManagerException {
    private static final long serialVersionUID = 1L;

    public CeciManagerException() {
    }

    public CeciManagerException(String message) {
        super(message);
    }

    public CeciManagerException(Throwable cause) {
        super(cause);
    }

    public CeciManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public CeciManagerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
