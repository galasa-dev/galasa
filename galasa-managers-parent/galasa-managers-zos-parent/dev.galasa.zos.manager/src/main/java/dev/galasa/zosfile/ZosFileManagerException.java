/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosfile;

import dev.galasa.zos.ZosManagerException;

public class ZosFileManagerException extends ZosManagerException {
    private static final long serialVersionUID = 1L;

    public ZosFileManagerException() {
    }

    public ZosFileManagerException(String message) {
        super(message);
    }

    public ZosFileManagerException(Throwable cause) {
        super(cause);
    }

    public ZosFileManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZosFileManagerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
