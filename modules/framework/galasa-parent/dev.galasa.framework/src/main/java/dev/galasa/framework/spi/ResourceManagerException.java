/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

public class ResourceManagerException extends Exception {
    private static final long serialVersionUID = 1L;

    public ResourceManagerException() {
    }

    public ResourceManagerException(String message) {
        super(message);
    }

    public ResourceManagerException(Throwable cause) {
        super(cause);
    }

    public ResourceManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceManagerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
