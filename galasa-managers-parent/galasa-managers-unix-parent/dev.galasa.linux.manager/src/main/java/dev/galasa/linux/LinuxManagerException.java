/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.linux;

import dev.galasa.ManagerException;

public class LinuxManagerException extends ManagerException {
    private static final long serialVersionUID = 1L;

    public LinuxManagerException() {
    }

    public LinuxManagerException(String message) {
        super(message);
    }

    public LinuxManagerException(Throwable cause) {
        super(cause);
    }

    public LinuxManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public LinuxManagerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
