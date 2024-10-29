/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.mq;
import dev.galasa.ManagerException;

public class MqManagerException extends ManagerException {

	public MqManagerException() {
		// TODO Auto-generated constructor stub
	}

	public MqManagerException(String message) {
		super(message);
	}

	public MqManagerException(Throwable cause) {
		super(cause);
	}

	public MqManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public MqManagerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
