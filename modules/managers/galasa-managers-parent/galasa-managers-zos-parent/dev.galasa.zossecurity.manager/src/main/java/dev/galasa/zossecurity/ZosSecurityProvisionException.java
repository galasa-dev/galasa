/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity;

public class ZosSecurityProvisionException extends Exception {
	private static final long serialVersionUID = 1L;

	public ZosSecurityProvisionException() {
	}

	public ZosSecurityProvisionException(String message) {
		super(message);
	}

	public ZosSecurityProvisionException(Throwable cause) {
		super(cause);
	}

	public ZosSecurityProvisionException(String message, Throwable cause) {
		super(message, cause);
	}

}
