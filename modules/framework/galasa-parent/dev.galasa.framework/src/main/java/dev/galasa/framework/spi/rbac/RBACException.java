/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.rbac;

import dev.galasa.framework.spi.FrameworkErrorDetails;
import dev.galasa.framework.spi.FrameworkException;

public class RBACException extends FrameworkException {
    private static final long serialVersionUID = 1L;

    public RBACException() {
    }

    public RBACException(String message) {
        super(message);
    }

    public RBACException(Throwable cause) {
        super(cause);
    }

    public RBACException(String message, Throwable cause) {
        super(message, cause);
    }

    public RBACException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public RBACException(FrameworkErrorDetails errorDetails, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace
    ) {
        super(errorDetails,cause,enableSuppression, writableStackTrace);
    }
    
}
