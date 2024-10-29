/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity;

public class RacfSyntaxErrorException extends ZosSecurityManagerException {
	private static final long serialVersionUID = 1L;

	public RacfSyntaxErrorException() {
	}

	public RacfSyntaxErrorException(String message) {
		super(message);
	}

	public RacfSyntaxErrorException(Throwable cause) {
		super(cause);
	}

	public RacfSyntaxErrorException(String message, Throwable cause) {
		super(message, cause);
	}

}
