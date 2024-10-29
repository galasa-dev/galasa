/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosunixcommand;

public class ZosUNIXCommandAuthFailException extends ZosUNIXCommandException {
    private static final long serialVersionUID = 1L;

    public ZosUNIXCommandAuthFailException() {
    }

    public ZosUNIXCommandAuthFailException(String message) {
        super(message);
    }

    public ZosUNIXCommandAuthFailException(Throwable cause) {
        super(cause);
    }

    public ZosUNIXCommandAuthFailException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZosUNIXCommandAuthFailException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
