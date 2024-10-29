/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosmf;

import dev.galasa.zos.ZosManagerException;

public class ZosmfManagerException extends ZosManagerException {
    private static final long serialVersionUID = 1L;

    public ZosmfManagerException() {
    }

    public ZosmfManagerException(String message) {
        super(message);
    }

    public ZosmfManagerException(Throwable cause) {
        super(cause);
    }

    public ZosmfManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZosmfManagerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
