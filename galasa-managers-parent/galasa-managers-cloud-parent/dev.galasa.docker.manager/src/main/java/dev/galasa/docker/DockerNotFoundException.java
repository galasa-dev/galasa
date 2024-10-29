/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker;

public class DockerNotFoundException extends DockerManagerException {
	private static final long serialVersionUID = 1L;
	
	public DockerNotFoundException() {
	}

	public DockerNotFoundException(String message) {
		super(message);
	}

	public DockerNotFoundException(Throwable cause) {
		super(cause);
	}

	public DockerNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public DockerNotFoundException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
