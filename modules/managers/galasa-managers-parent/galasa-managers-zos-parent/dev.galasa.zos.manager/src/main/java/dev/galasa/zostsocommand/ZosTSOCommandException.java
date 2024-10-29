/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zostsocommand;

public class ZosTSOCommandException extends ZosTSOCommandManagerException {
    private static final long serialVersionUID = 1L;

    public ZosTSOCommandException() {
    }

    public ZosTSOCommandException(String message) {
        super(message);
    }

    public ZosTSOCommandException(Throwable cause) {
        super(cause);
    }

    public ZosTSOCommandException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZosTSOCommandException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
