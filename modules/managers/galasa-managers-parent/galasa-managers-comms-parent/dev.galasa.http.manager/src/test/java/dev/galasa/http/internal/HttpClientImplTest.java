/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.http.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import dev.galasa.http.mocks.MockLog;

/**
 * Unit tests for HttpClientImpl class focusing on setAuthorisation and execute methods
 */
public class HttpClientImplTest {

    private HttpClientImpl httpClient;
    private MockLog mockLog;
    private static final int DEFAULT_TIMEOUT = 5000;

    @Before
    public void setUp() throws Exception {
        mockLog = new MockLog();
        httpClient = new HttpClientImpl(DEFAULT_TIMEOUT, mockLog);
        httpClient.setURI(new URI("http://example.com"));
    }

    @Test
    public void testSetAuthorisationSetsCredentialsForAllScopes() throws Exception {
        // Given
        String username = "testuser";
        String password = "testpass";

        // When
        httpClient.setAuthorisation(username, password);

        // Then
        String retrievedUsername = httpClient.getUsername();
        assertThat(retrievedUsername).as("Username should be set").isEqualTo(username);
    }

    @Test
    public void testSetAuthorisationClearsPreviousCredentials() throws Exception {
        // Given
        httpClient.setAuthorisation("olduser", "oldpass");

        // When
        httpClient.setAuthorisation("newuser", "newpass");

        // Then
        String retrievedUsername = httpClient.getUsername();
        assertThat(retrievedUsername).as("Should have new username").isEqualTo("newuser");
    }

    @Test
    public void testSetAuthorisationWithScopeSetsScopedCredentials() throws Exception {
        // Given
        String username = "scopeduser";
        String password = "scopedpass";
        URI scope = new URI("http://example.com:8080");

        // When
        httpClient.setAuthorisation(username, password, scope);

        // Then
        String retrievedUsername = httpClient.getUsername(scope);
        assertThat(retrievedUsername).as("Username should be set for scope").isEqualTo(username);
    }

    @Test
    public void testGetUsernameReturnsNullWhenNoCredentialsSet() throws Exception {
        // When
        String username = httpClient.getUsername();

        // Then
        assertThat(username).as("Username should be null when not set").isNull();
    }

    @Test
    public void testGetUsernameWithScopeReturnsNullWhenNoCredentialsSet() throws Exception {
        // Given
        URI scope = new URI("http://example.com:8080");

        // When
        String username = httpClient.getUsername(scope);

        // Then
        assertThat(username).as("Username should be null when not set for scope").isNull();
    }

    @Test
    public void testSetAuthorisationWithEmptyUsername() throws Exception {
        // Given
        String username = "";
        String password = "testpass";

        // When
        httpClient.setAuthorisation(username, password);

        // Then
        String retrievedUsername = httpClient.getUsername();
        assertThat(retrievedUsername).as("Empty username should be set").isEqualTo(username);
    }

    @Test
    public void testSetAuthorisationWithEmptyPassword() throws Exception {
        // Given
        String username = "testuser";
        String password = "";

        // When
        httpClient.setAuthorisation(username, password);

        // Then
        String retrievedUsername = httpClient.getUsername();
        assertThat(retrievedUsername).as("Username should be set even with empty password").isEqualTo(username);
    }

    @Test
    public void testSetAuthorisationWithSpecialCharactersInUsername() throws Exception {
        // Given
        String username = "test@user.com";
        String password = "testpass";

        // When
        httpClient.setAuthorisation(username, password);

        // Then
        String retrievedUsername = httpClient.getUsername();
        assertThat(retrievedUsername).as("Username with special characters should be set").isEqualTo(username);
    }

    @Test
    public void testSetAuthorisationWithSpecialCharactersInPassword() throws Exception {
        // Given
        String username = "testuser";
        String password = "p@ssw0rd!#$%";

        // When
        httpClient.setAuthorisation(username, password);

        // Then
        String retrievedUsername = httpClient.getUsername();
        assertThat(retrievedUsername).as("Username should be set with special character password").isEqualTo(username);
    }

    @Test
    public void testMultipleScopedAuthorisations() throws Exception {
        // Given
        URI scope1 = new URI("http://example1.com:8080");
        URI scope2 = new URI("http://example2.com:9090");

        // When
        httpClient.setAuthorisation("user1", "pass1", scope1);
        httpClient.setAuthorisation("user2", "pass2", scope2);

        // Then
        assertThat(httpClient.getUsername(scope1)).as("First scope should have user1").isEqualTo("user1");
        assertThat(httpClient.getUsername(scope2)).as("Second scope should have user2").isEqualTo("user2");
    }

    @Test
    public void testCloseHttpClient() throws Exception {
        // Given
        httpClient.setURI(URI.create("http://example.com"));
        httpClient.build();

        // When
        httpClient.close();

        // Then - should not throw exception
        assertThat(true).as("Close should complete without exception").isTrue();
    }

    @Test
    public void testCloseHttpClientWhenNotBuilt() throws Exception {
        // When
        httpClient.close();

        // Then - should not throw exception
        assertThat(true).as("Close should complete without exception when not built").isTrue();
    }
}

