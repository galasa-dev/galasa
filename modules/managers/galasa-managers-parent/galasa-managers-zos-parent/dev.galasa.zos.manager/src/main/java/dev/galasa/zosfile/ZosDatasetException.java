/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosfile;

public class ZosDatasetException extends ZosFileManagerException {
    private static final long serialVersionUID = 1L;

    public ZosDatasetException() {
    }

    public ZosDatasetException(String message) {
        super(message);
    }

    public ZosDatasetException(Throwable cause) {
        super(cause);
    }

    public ZosDatasetException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZosDatasetException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
