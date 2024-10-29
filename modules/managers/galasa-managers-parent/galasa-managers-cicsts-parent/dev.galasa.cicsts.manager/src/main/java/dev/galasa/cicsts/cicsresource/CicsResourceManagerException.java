/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.cicsresource;

import dev.galasa.cicsts.CicstsManagerException;

public class CicsResourceManagerException extends CicstsManagerException {
    private static final long serialVersionUID = 1L;

    public CicsResourceManagerException() {
    }

    public CicsResourceManagerException(String message) {
        super(message);
    }

    public CicsResourceManagerException(Throwable cause) {
        super(cause);
    }

    public CicsResourceManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public CicsResourceManagerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
