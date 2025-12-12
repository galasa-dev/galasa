/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.tags;

import dev.galasa.framework.spi.FrameworkErrorDetails;
import dev.galasa.framework.spi.FrameworkException;

public class TagsException extends FrameworkException {
    private static final long serialVersionUID = 1L;

    public TagsException() {
    }

    public TagsException(String message) {
        super(message);
    }

    public TagsException(Throwable cause) {
        super(cause);
    }

    public TagsException(String message, Throwable cause) {
        super(message, cause);
    }

    public TagsException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public TagsException(FrameworkErrorDetails errorDetails, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace
    ) {
        super(errorDetails,cause,enableSuppression, writableStackTrace);
    }
}
