/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270;

public class Zos3270Exception extends Exception {
    private static final long serialVersionUID = 1L;

    public Zos3270Exception() {
    }

    public Zos3270Exception(String message) {
        super(message);
    }

    public Zos3270Exception(Throwable cause) {
        super(cause);
    }

    public Zos3270Exception(String message, Throwable cause) {
        super(message, cause);
    }

    public Zos3270Exception(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
