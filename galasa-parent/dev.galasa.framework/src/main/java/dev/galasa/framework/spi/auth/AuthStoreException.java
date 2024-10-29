/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.FrameworkException;

public class AuthStoreException extends FrameworkException {
    private static final long serialVersionUID = 1L;

    private Log logger = LogFactory.getLog(this.getClass());

    public AuthStoreException() {
    }

    public AuthStoreException(String message) {
        super(message);
        logger.error(message);
    }

    public AuthStoreException(Throwable cause) {
        super(cause);
        logger.error(cause);
    }

    public AuthStoreException(String message, Throwable cause) {
        super(message, cause);
        logger.error(message, cause);
    }

    public AuthStoreException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        logger.error(message, cause);
    }

}
