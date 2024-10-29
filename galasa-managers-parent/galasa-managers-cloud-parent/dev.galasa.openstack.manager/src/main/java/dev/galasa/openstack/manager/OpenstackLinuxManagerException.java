/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.openstack.manager;

import dev.galasa.linux.LinuxManagerException;

public class OpenstackLinuxManagerException extends LinuxManagerException {
    private static final long serialVersionUID = 1L;

    public OpenstackLinuxManagerException() {
    }

    public OpenstackLinuxManagerException(String message) {
        super(message);
    }

    public OpenstackLinuxManagerException(Throwable cause) {
        super(cause);
    }

    public OpenstackLinuxManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public OpenstackLinuxManagerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
