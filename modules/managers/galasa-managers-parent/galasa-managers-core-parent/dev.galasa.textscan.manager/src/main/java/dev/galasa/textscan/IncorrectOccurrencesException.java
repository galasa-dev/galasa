/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.textscan;

public class IncorrectOccurrencesException extends TextScanManagerException {
	private static final long serialVersionUID = 1L;

	public IncorrectOccurrencesException() {
	}

	public IncorrectOccurrencesException(String message) {
		super(message);
	}

	public IncorrectOccurrencesException(Throwable cause) {
		super(cause);
	}

	public IncorrectOccurrencesException(String message, Throwable cause) {
		super(message, cause);
	}

	public IncorrectOccurrencesException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
