/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosliberty;

public class ZosLibertyServerException extends ZosLibertyManagerException {
    private static final long serialVersionUID = 1L;

    public ZosLibertyServerException() {
    }

    public ZosLibertyServerException(String message) {
        super(message);
    }

    public ZosLibertyServerException(Throwable cause) {
        super(cause);
    }

    public ZosLibertyServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZosLibertyServerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
