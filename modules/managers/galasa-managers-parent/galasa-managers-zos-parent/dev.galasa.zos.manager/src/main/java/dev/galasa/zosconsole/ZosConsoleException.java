/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosconsole;

public class ZosConsoleException extends ZosConsoleManagerException {
    private static final long serialVersionUID = 1L;

    public ZosConsoleException() {
    }

    public ZosConsoleException(String message) {
        super(message);
    }

    public ZosConsoleException(Throwable cause) {
        super(cause);
    }

    public ZosConsoleException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZosConsoleException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
