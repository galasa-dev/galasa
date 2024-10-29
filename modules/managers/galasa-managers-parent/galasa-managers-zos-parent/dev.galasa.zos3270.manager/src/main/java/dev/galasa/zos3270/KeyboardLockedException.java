/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270;

public class KeyboardLockedException extends Zos3270Exception {
    private static final long serialVersionUID = 1L;

    public KeyboardLockedException() {
    }

    public KeyboardLockedException(String message) {
        super(message);
    }

    public KeyboardLockedException(Throwable cause) {
        super(cause);
    }

    public KeyboardLockedException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeyboardLockedException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
