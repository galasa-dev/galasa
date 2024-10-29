/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270;

import dev.galasa.ManagerException;

public class Zos3270ManagerException extends ManagerException {
    private static final long serialVersionUID = 1L;

    public Zos3270ManagerException() {
    }

    public Zos3270ManagerException(String message) {
        super(message);
    }

    public Zos3270ManagerException(Throwable cause) {
        super(cause);
    }

    public Zos3270ManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public Zos3270ManagerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
