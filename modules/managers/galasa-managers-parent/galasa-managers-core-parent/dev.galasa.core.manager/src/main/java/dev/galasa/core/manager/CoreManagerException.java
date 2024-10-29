/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.core.manager;

import dev.galasa.ManagerException;

public class CoreManagerException extends ManagerException {
	private static final long serialVersionUID = 1L;

	public CoreManagerException() {
	}

	public CoreManagerException(String message) {
		super(message);
	}

	public CoreManagerException(Throwable cause) {
		super(cause);
	}

	public CoreManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public CoreManagerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
