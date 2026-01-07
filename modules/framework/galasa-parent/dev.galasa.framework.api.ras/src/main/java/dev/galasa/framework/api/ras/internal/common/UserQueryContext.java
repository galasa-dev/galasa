/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.common;

public class UserQueryContext {
	private final String matchedRequestor;
	private final String matchedUser;
	private final boolean shouldReturnEmptyResultsPage;

	public UserQueryContext(String matchedRequestor, String matchedUser, boolean shouldReturnEmptyResultsPage) {
		this.matchedRequestor = matchedRequestor;
		this.matchedUser = matchedUser;
		this.shouldReturnEmptyResultsPage = shouldReturnEmptyResultsPage;
	}

	public String getMatchedRequestor() {
		return matchedRequestor;
	}

	public String getMatchedUser() {
		return matchedUser;
	}

	public boolean shouldReturnEmptyResultsPage() {
		return shouldReturnEmptyResultsPage;
	}
}
