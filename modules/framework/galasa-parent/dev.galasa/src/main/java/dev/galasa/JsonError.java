/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa;

import javax.validation.constraints.NotNull;

public class JsonError {

	private final String error;
	
	public JsonError(@NotNull String error) {
		
		this.error = error;
		
	}
	
}
