/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ivts.docker;

public class IvtException extends Exception {
    private static final long serialVersionUID = 1L;

    public IvtException() {
    }

    public IvtException(String message) {
        super(message);
    }

    public IvtException(Throwable cause) {
        super(cause);
    }

    public IvtException(String message, Throwable cause) {
        super(message, cause);
    }

    public IvtException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
