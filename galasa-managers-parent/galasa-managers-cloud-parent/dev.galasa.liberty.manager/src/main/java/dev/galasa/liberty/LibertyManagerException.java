/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.liberty;

import dev.galasa.ManagerException;

public class LibertyManagerException extends ManagerException {
    private static final long serialVersionUID = 1L;

    public LibertyManagerException() {
    }

    public LibertyManagerException(String message) {
        super(message);
    }

    public LibertyManagerException(Throwable cause) {
        super(cause);
    }

    public LibertyManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public LibertyManagerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
