/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

public class FrameworkPropertyFileException extends Exception {
    private static final long serialVersionUID = 2L;

    public FrameworkPropertyFileException() {
    }

    public FrameworkPropertyFileException(String message) {
        super(message);
    }

    public FrameworkPropertyFileException(Throwable cause) {
        super(cause);
    }

    public FrameworkPropertyFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public FrameworkPropertyFileException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
