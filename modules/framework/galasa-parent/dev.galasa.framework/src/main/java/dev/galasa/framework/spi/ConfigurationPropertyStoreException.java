/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

public class ConfigurationPropertyStoreException extends FrameworkException {
    private static final long serialVersionUID = 1L;

    public ConfigurationPropertyStoreException() {
    }

    public ConfigurationPropertyStoreException(String message) {
        super(message);
    }

    public ConfigurationPropertyStoreException(Throwable cause) {
        super(cause);
    }

    public ConfigurationPropertyStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationPropertyStoreException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
