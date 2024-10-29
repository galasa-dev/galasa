/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.selenium;

import dev.galasa.ManagerException;

public class SeleniumManagerException extends ManagerException {
    private static final long serialVersionUID = 1L;

    public SeleniumManagerException() {
    }

    public SeleniumManagerException(String message) {
        super(message);
    }

    public SeleniumManagerException(Throwable cause) {
        super(cause);
    }

    public SeleniumManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public SeleniumManagerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
