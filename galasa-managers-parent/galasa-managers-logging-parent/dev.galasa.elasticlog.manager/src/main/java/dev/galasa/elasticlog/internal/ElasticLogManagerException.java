/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.elasticlog.internal;

import dev.galasa.ManagerException;

public class ElasticLogManagerException extends ManagerException {
	private static final long serialVersionUID = 1L;

	public ElasticLogManagerException() {
	}

	public ElasticLogManagerException(String message) {
		super(message);
	}

	public ElasticLogManagerException(Throwable cause) {
		super(cause);
	}

	public ElasticLogManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ElasticLogManagerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}