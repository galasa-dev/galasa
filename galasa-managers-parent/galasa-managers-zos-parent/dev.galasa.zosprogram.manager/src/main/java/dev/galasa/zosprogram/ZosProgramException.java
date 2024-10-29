/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosprogram;

public class ZosProgramException extends ZosProgramManagerException {
    private static final long serialVersionUID = 1L;

    public ZosProgramException() {
    }

    public ZosProgramException(String message) {
        super(message);
    }

    public ZosProgramException(Throwable cause) {
        super(cause);
    }

    public ZosProgramException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZosProgramException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
