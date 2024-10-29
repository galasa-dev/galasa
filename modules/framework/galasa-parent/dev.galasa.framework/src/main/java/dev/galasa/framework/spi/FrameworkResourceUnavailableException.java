/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

public class FrameworkResourceUnavailableException extends FrameworkException {
    private static final long serialVersionUID = 1L;

    public FrameworkResourceUnavailableException() {
    }

    public FrameworkResourceUnavailableException(String message) {
        super(message);
    }

    public FrameworkResourceUnavailableException(Throwable cause) {
        super(cause);
    }

    public FrameworkResourceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public FrameworkResourceUnavailableException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
