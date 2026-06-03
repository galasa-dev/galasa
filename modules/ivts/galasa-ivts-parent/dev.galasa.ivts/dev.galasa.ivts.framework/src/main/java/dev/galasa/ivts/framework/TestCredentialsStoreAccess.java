/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ivts.framework;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.logging.Log;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsToken;
import dev.galasa.ICredentialsUsername;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.ICredentialsUsernameToken;
import dev.galasa.Summary;
import dev.galasa.Tags;
import dev.galasa.Test;
import dev.galasa.core.manager.CoreManager;
import dev.galasa.core.manager.ICoreManager;
import dev.galasa.core.manager.Logger;

@Test
@Summary("Verify the core manager can retrieve credentials store entries")
@Tags({"core","ivt"})
public class TestCredentialsStoreAccess {

    private static final String USERNAME_PASSWORD_CREDENTIALS_ID = "TEST_USERNAME_PASSWORD";
    private static final String USERNAME_ONLY_CREDENTIALS_ID = "TEST_USERNAME";
    private static final String TOKEN_ONLY_CREDENTIALS_ID = "TEST_TOKEN";
    private static final String USERNAME_TOKEN_CREDENTIALS_ID = "TEST_USERNAME_TOKEN";

    private static final String EXPECTED_USERNAME_PASSWORD_USERNAME = "testuser1";
    private static final String EXPECTED_USERNAME_PASSWORD_PASSWORD = "testpass1";

    private static final String EXPECTED_USERNAME_ONLY_USERNAME = "useronly";

    private static final String EXPECTED_TOKEN_ONLY_TOKEN = "ghp_test123token456";

    private static final String EXPECTED_USERNAME_TOKEN_USERNAME = "tokenuser";
    private static final String EXPECTED_USERNAME_TOKEN_TOKEN = "tokenvalue123";

    @Logger
    public Log logger;

    @CoreManager
    public ICoreManager coreManager;

    @Test
    public void checkUsernamePasswordCredentials() throws Exception {
        ICredentials credentials = coreManager.getCredentials(USERNAME_PASSWORD_CREDENTIALS_ID);

        assertThat(credentials)
                .as("Username and password credentials should be retrieved")
                .isInstanceOf(ICredentialsUsernamePassword.class);

        ICredentialsUsernamePassword usernamePasswordCredentials =
                (ICredentialsUsernamePassword) credentials;

        assertThat(usernamePasswordCredentials.getUsername())
                .as("Username should match the expected username")
                .isEqualTo(EXPECTED_USERNAME_PASSWORD_USERNAME);

        assertThat(usernamePasswordCredentials.getPassword())
                .as("Password should match the expected password")
                .isEqualTo(EXPECTED_USERNAME_PASSWORD_PASSWORD);

        ICredentialsUsernamePassword directUsernamePasswordCredentials =
                coreManager.getUsernamePassword(USERNAME_PASSWORD_CREDENTIALS_ID);

        assertThat(directUsernamePasswordCredentials.getUsername())
                .as("Direct username and password lookup should return the expected username")
                .isEqualTo(EXPECTED_USERNAME_PASSWORD_USERNAME);

        assertThat(directUsernamePasswordCredentials.getPassword())
                .as("Direct username and password lookup should return the expected password")
                .isEqualTo(EXPECTED_USERNAME_PASSWORD_PASSWORD);

        logger.info("Successfully verified username and password credentials");
    }

    @Test
    public void checkUsernameOnlyCredentials() throws Exception {
        ICredentials credentials = coreManager.getCredentials(USERNAME_ONLY_CREDENTIALS_ID);

        assertThat(credentials)
                .as("Username only credentials should be retrieved")
                .isInstanceOf(ICredentialsUsername.class);

        ICredentialsUsername usernameCredentials = (ICredentialsUsername) credentials;

        assertThat(usernameCredentials.getUsername())
                .as("Username only credential should match the expected username")
                .isEqualTo(EXPECTED_USERNAME_ONLY_USERNAME);

        logger.info("Successfully verified username only credentials");
    }

    @Test
    public void checkTokenOnlyCredentials() throws Exception {
        ICredentials credentials = coreManager.getCredentials(TOKEN_ONLY_CREDENTIALS_ID);

        assertThat(credentials)
                .as("Token only credentials should be retrieved")
                .isInstanceOf(ICredentialsToken.class);

        ICredentialsToken tokenCredentials = (ICredentialsToken) credentials;

        assertThat(new String(tokenCredentials.getToken()))
                .as("Token only credential should match the expected token")
                .isEqualTo(EXPECTED_TOKEN_ONLY_TOKEN);

        logger.info("Successfully verified token only credentials");
    }

    @Test
    public void checkUsernameTokenCredentials() throws Exception {
        ICredentials credentials = coreManager.getCredentials(USERNAME_TOKEN_CREDENTIALS_ID);

        assertThat(credentials)
                .as("Username and token credentials should be retrieved")
                .isInstanceOf(ICredentialsUsernameToken.class);

        ICredentialsUsernameToken usernameTokenCredentials = (ICredentialsUsernameToken) credentials;

        assertThat(usernameTokenCredentials.getUsername())
                .as("Username and token credential should return the expected username")
                .isEqualTo(EXPECTED_USERNAME_TOKEN_USERNAME);

        assertThat(new String(usernameTokenCredentials.getToken()))
                .as("Username and token credential should return the expected token")
                .isEqualTo(EXPECTED_USERNAME_TOKEN_TOKEN);

        logger.info("Successfully verified username and token credentials");
    }

}
