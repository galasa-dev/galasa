/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.cicsresource;

public class CicsBundleResourceException extends CicsResourceManagerException {
    private static final long serialVersionUID = 1L;

    public CicsBundleResourceException() {
    }

    public CicsBundleResourceException(String message) {
        super(message);
    }

    public CicsBundleResourceException(Throwable cause) {
        super(cause);
    }

    public CicsBundleResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public CicsBundleResourceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
