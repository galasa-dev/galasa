/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker;

import dev.galasa.ManagerException;

public class DockerProvisionException extends ManagerException {
    private static final long serialVersionUID = 1L;

    public DockerProvisionException() {
    }

    public DockerProvisionException(String message) {
        super(message);
    }

    public DockerProvisionException(Throwable cause) {
        super(cause);
    }

    public DockerProvisionException(String message, Throwable cause) {
        super(message, cause);
    }

    public DockerProvisionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}