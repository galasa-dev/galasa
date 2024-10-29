/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts;

public class CeciException extends CeciManagerException {
    private static final long serialVersionUID = 1L;

    public CeciException() {
    }

    public CeciException(String message) {
        super(message);
    }

    public CeciException(Throwable cause) {
        super(cause);
    }

    public CeciException(String message, Throwable cause) {
        super(message, cause);
    }

    public CeciException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
