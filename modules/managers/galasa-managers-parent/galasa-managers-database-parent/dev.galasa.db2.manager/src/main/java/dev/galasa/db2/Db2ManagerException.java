/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.db2;

import dev.galasa.ManagerException;

public class Db2ManagerException extends ManagerException {
	private static final long serialVersionUID = 1L;

	public Db2ManagerException() {
	}

	public Db2ManagerException(String message) {
		super(message);
	}

	public Db2ManagerException(Throwable cause) {
		super(cause);
	}

	public Db2ManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public Db2ManagerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}