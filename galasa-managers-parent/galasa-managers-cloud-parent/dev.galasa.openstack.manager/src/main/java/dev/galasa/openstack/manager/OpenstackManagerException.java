/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.openstack.manager;

import dev.galasa.ManagerException;

public class OpenstackManagerException extends ManagerException {
    private static final long serialVersionUID = 1L;

    public OpenstackManagerException() {
    }

    public OpenstackManagerException(String message) {
        super(message);
    }

    public OpenstackManagerException(Throwable cause) {
        super(cause);
    }

    public OpenstackManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public OpenstackManagerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
