/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270;

public class TextNotFoundException extends Zos3270Exception {
    private static final long serialVersionUID = 1L;

    public TextNotFoundException() {
    }

    public TextNotFoundException(String message) {
        super(message);
    }

    public TextNotFoundException(Throwable cause) {
        super(cause);
    }

    public TextNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public TextNotFoundException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
