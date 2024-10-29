/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.vtp.manager;

import dev.galasa.ManagerException;

public class VtpManagerException extends ManagerException {

	public VtpManagerException() {
	}

	public VtpManagerException(String message) {
		super(message);
	}

	public VtpManagerException(Throwable cause) {
		super(cause);
	}

	public VtpManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public VtpManagerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
