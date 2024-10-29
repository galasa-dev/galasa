/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.githubissue;

import dev.galasa.ManagerException;

public class GitHubIssueManagerException extends ManagerException {
	
	private static final long serialVersionUID = 1L;

    public GitHubIssueManagerException() {
    }

    public GitHubIssueManagerException(String message) {
        super(message);
    }

    public GitHubIssueManagerException(Throwable cause) {
        super(cause);
    }

    public GitHubIssueManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public GitHubIssueManagerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
