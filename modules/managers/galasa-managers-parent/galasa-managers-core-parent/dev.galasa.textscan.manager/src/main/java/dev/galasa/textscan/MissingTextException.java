/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.textscan;

public class MissingTextException extends TextScanManagerException {
	private static final long serialVersionUID = 1L;

	public MissingTextException() {
	}

	public MissingTextException(String message) {
		super(message);
	}

	public MissingTextException(Throwable cause) {
		super(cause);
	}

	public MissingTextException(String message, Throwable cause) {
		super(message, cause);
	}

	public MissingTextException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
