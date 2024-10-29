/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosliberty.angel;

public class ZosLibertyAngelException extends ZosLibertyAngelManagerException {
    private static final long serialVersionUID = 1L;

    public ZosLibertyAngelException() {
    }

    public ZosLibertyAngelException(String message) {
        super(message);
    }

    public ZosLibertyAngelException(Throwable cause) {
        super(cause);
    }

    public ZosLibertyAngelException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZosLibertyAngelException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
