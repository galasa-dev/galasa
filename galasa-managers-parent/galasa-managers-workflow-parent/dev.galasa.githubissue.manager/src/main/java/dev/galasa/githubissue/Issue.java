/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.githubissue;

public class Issue {
	
	private String issue;
	private String state;
	private String url;
	private String title;
	
	public Issue(String issue, String state, String url, String title) {
		this.issue = issue;
		this.state = state;
		this.url = url;
		this.title = title;
	}
	
	public String getIssue() {
		return this.issue;
	}
	
	public String getState() {
		return this.state;
	}
	
	public String getUrl() {
		return this.url;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public boolean isClosed() {
		return this.state.equals("closed");
	}

}
