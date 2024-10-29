/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity;

public class ProfileAlreadyExistsException extends ZosSecurityManagerException {
	private static final long serialVersionUID = 1L;

	public ProfileAlreadyExistsException() {
	}

	public ProfileAlreadyExistsException(String message) {
		super(message);
	}

	public ProfileAlreadyExistsException(Throwable cause) {
		super(cause);
	}

	public ProfileAlreadyExistsException(String message, Throwable cause) {
		super(message, cause);
	}

}
