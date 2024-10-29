/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosrseapi;

import dev.galasa.zos.ZosManagerException;

public class RseapiManagerException extends ZosManagerException {
    private static final long serialVersionUID = 1L;

    public RseapiManagerException() {
    }

    public RseapiManagerException(String message) {
        super(message);
    }

    public RseapiManagerException(Throwable cause) {
        super(cause);
    }

    public RseapiManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public RseapiManagerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
