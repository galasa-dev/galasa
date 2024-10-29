/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.githubissue.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.githubissue.GitHubIssue;
import dev.galasa.githubissue.GitHubIssueManagerException;

public class GitHubIssueInstanceRepository extends CpsProperties {
	
	public static String get(GitHubIssue gitHubInstance) throws GitHubIssueManagerException {
		try {
			return getStringNulled(GitHubIssuePropertiesSingleton.cps(), "instance", "repository", gitHubInstance.githubId());
		} catch (ConfigurationPropertyStoreException e) {
			throw new GitHubIssueManagerException();
		}
	}
	
	

}
