/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.liberty;

public class LibertyServerException extends LibertyManagerException {
    private static final long serialVersionUID = 1L;

    public LibertyServerException() {
    }

    public LibertyServerException(String message) {
        super(message);
    }

    public LibertyServerException(Throwable cause) {
        super(cause);
    }

    public LibertyServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public LibertyServerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
