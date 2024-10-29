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

public class GitHubCredentials extends CpsProperties {

	public static String get(GitHubIssue gitHubInstance) throws GitHubIssueManagerException {
        try {
            String credentialsKey = getStringNulled(GitHubIssuePropertiesSingleton.cps(), "instance", "credentials", gitHubInstance.githubId());
           
            // Default value
            if (credentialsKey == null) {
                return "GITHUB";
            }

            return credentialsKey;
        } catch (ConfigurationPropertyStoreException e) {
            throw new GitHubIssueManagerException("Failed to access the CPS to find the credential");
        } 
    }
	
}
