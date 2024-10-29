/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

public class MetricsServerException extends Exception {
    private static final long serialVersionUID = 1L;

    public MetricsServerException() {
    }

    public MetricsServerException(String message) {
        super(message);
    }

    public MetricsServerException(Throwable cause) {
        super(cause);
    }

    public MetricsServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public MetricsServerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
