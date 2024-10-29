/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zostsocommand;

import dev.galasa.zos.ZosManagerException;

public class ZosTSOCommandManagerException extends ZosManagerException {
    private static final long serialVersionUID = 1L;

    public ZosTSOCommandManagerException() {
    }

    public ZosTSOCommandManagerException(String message) {
        super(message);
    }

    public ZosTSOCommandManagerException(Throwable cause) {
        super(cause);
    }

    public ZosTSOCommandManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZosTSOCommandManagerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
