/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.gradle.obr;

public class ObrException extends Exception {
    private static final long serialVersionUID = 1L;

    public ObrException() {
    }

    public ObrException(String message) {
        super(message);
    }

    public ObrException(Throwable cause) {
        super(cause);
    }

    public ObrException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObrException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
