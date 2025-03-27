/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.streams;

import dev.galasa.framework.spi.FrameworkErrorDetails;
import dev.galasa.framework.spi.FrameworkException;

public class StreamsException extends FrameworkException {

    private static final long serialVersionUID = 1L;

    public StreamsException() {

    }

    public StreamsException(String message) {
        super(message);
    }

    public StreamsException(Throwable cause) {
        super(cause);
    }

    public StreamsException(String message, Throwable cause) {
        super(message, cause);
    }

    public StreamsException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public StreamsException(FrameworkErrorDetails errorDetails, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(errorDetails, cause, enableSuppression, writableStackTrace);
    }

}
