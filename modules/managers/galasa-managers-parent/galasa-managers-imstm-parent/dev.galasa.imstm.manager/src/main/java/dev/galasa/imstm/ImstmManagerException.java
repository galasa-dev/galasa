/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm;

import dev.galasa.ManagerException;

public class ImstmManagerException extends ManagerException {
    private static final long serialVersionUID = 1L;

    public ImstmManagerException() {
    }

    public ImstmManagerException(String message) {
        super(message);
    }

    public ImstmManagerException(Throwable cause) {
        super(cause);
    }

    public ImstmManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImstmManagerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
