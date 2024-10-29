/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.java.windows;

import dev.galasa.java.JavaManagerException;

public class JavaWindowsManagerException extends JavaManagerException {
    private static final long serialVersionUID = 1L;

    public JavaWindowsManagerException() {
    }

    public JavaWindowsManagerException(String message) {
        super(message);
    }

    public JavaWindowsManagerException(Throwable cause) {
        super(cause);
    }

    public JavaWindowsManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public JavaWindowsManagerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
