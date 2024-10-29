/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

public class EventsException extends FrameworkException {
    private static final long serialVersionUID = 1L;

    public EventsException() {
    }

    public EventsException(String message) {
        super(message);
    }

    public EventsException(Throwable cause) {
        super(cause);
    }

    public EventsException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventsException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}

