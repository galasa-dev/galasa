/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.java.ubuntu;

import dev.galasa.java.JavaManagerException;

public class JavaUbuntuManagerException extends JavaManagerException {
    private static final long serialVersionUID = 1L;

    public JavaUbuntuManagerException() {
    }

    public JavaUbuntuManagerException(String message) {
        super(message);
    }

    public JavaUbuntuManagerException(Throwable cause) {
        super(cause);
    }

    public JavaUbuntuManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public JavaUbuntuManagerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
