/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.phoenix2.internal;

import dev.galasa.ManagerException;

public class Phoenix2ManagerException extends ManagerException {
	private static final long serialVersionUID = 1L;

	public Phoenix2ManagerException() {
	}

	public Phoenix2ManagerException(String message) {
		super(message);
	}

	public Phoenix2ManagerException(Throwable cause) {
		super(cause);
	}

	public Phoenix2ManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public Phoenix2ManagerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}