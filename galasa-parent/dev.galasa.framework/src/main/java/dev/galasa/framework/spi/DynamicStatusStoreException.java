/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

public class DynamicStatusStoreException extends FrameworkException {
    private static final long serialVersionUID = 1L;

    public DynamicStatusStoreException() {
    }

    public DynamicStatusStoreException(String message) {
        super(message);
    }

    public DynamicStatusStoreException(Throwable cause) {
        super(cause);
    }

    public DynamicStatusStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public DynamicStatusStoreException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
