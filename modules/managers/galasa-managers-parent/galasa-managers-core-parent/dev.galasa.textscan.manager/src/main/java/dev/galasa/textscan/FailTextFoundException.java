/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.textscan;

public class FailTextFoundException extends TextScanManagerException {
	private static final long serialVersionUID = 1L;

	public FailTextFoundException() {
	}

	public FailTextFoundException(String message) {
		super(message);
	}

	public FailTextFoundException(Throwable cause) {
		super(cause);
	}

	public FailTextFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public FailTextFoundException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
