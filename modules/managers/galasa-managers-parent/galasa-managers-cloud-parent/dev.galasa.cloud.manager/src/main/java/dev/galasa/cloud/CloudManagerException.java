/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cloud;

import dev.galasa.ManagerException;

public class CloudManagerException extends ManagerException {
	private static final long serialVersionUID = 1L;

	public CloudManagerException() {
	}

	public CloudManagerException(String message) {
		super(message);
	}

	public CloudManagerException(Throwable cause) {
		super(cause);
	}

	public CloudManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public CloudManagerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
