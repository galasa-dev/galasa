/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity;

public class UseridNotFoundException extends ZosSecurityManagerException {
	private static final long serialVersionUID = 1L;

	public UseridNotFoundException() {
	}

	public UseridNotFoundException(String message) {
		super(message);
	}

	public UseridNotFoundException(Throwable cause) {
		super(cause);
	}

	public UseridNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
