/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.textscan;

public class CheckpointException extends TextScanManagerException {
	private static final long serialVersionUID = 1L;

	public CheckpointException() {
	}

	public CheckpointException(String message) {
		super(message);
	}

	public CheckpointException(Throwable cause) {
		super(cause);
	}

	public CheckpointException(String message, Throwable cause) {
		super(message, cause);
	}

	public CheckpointException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
