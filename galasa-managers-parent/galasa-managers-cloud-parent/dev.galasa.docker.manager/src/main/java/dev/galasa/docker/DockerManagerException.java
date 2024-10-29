/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker;

import dev.galasa.ManagerException;

public class DockerManagerException extends ManagerException {
	private static final long serialVersionUID = 1L;

	public DockerManagerException() {
	}

	public DockerManagerException(String message) {
		super(message);
	}

	public DockerManagerException(Throwable cause) {
		super(cause);
	}

	public DockerManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public DockerManagerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}