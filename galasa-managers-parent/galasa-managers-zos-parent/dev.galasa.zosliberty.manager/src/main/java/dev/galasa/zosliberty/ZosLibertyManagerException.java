/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosliberty;

import dev.galasa.ManagerException;

public class ZosLibertyManagerException extends ManagerException {
    private static final long serialVersionUID = 1L;

    public ZosLibertyManagerException() {
    }

    public ZosLibertyManagerException(String message) {
        super(message);
    }

    public ZosLibertyManagerException(Throwable cause) {
        super(cause);
    }

    public ZosLibertyManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZosLibertyManagerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
