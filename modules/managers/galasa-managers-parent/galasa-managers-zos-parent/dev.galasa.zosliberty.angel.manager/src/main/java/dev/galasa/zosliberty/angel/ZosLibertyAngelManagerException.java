/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosliberty.angel;

import dev.galasa.ManagerException;

public class ZosLibertyAngelManagerException extends ManagerException {
    private static final long serialVersionUID = 1L;

    public ZosLibertyAngelManagerException() {
    }

    public ZosLibertyAngelManagerException(String message) {
        super(message);
    }

    public ZosLibertyAngelManagerException(Throwable cause) {
        super(cause);
    }

    public ZosLibertyAngelManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZosLibertyAngelManagerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
