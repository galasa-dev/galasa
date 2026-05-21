/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.maven.repository.internal;

import static org.assertj.core.api.Assertions.*;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.Test;

import dev.galasa.framework.maven.repository.internal.mocks.MockMavenRepository;
import dev.galasa.framework.maven.repository.internal.mocks.MockURLConnection;

public class GalasaMavenUrlHandlerServiceTest {

    
    @Test
    public void TestCanCreateHandlerServiceObject() {
        new GalasaMavenUrlHandlerService();
    }

    @Test 
    public void TestCanBuildAnArtifactURLWithoutTrailingSlashOnOBR() throws Exception {
        GalasaMavenUrlHandlerService service = new GalasaMavenUrlHandlerService();

        URL repositoryUrl = new URL("http://myhost/myrepository");

        URL url = service.buildArtifactUrl(repositoryUrl, "myGroupId", "myArtifactId", "0.myVersion.0", "myFileName");

        assertThat(url.toString()).doesNotContain("//myGroupId");
    }

    @Test 
    public void TestCanBuildAnArtifactURLWithTrailingSlashOnOBR() throws Exception {
        GalasaMavenUrlHandlerService service = new GalasaMavenUrlHandlerService();

        URL repositoryUrl = new URL("http://myhost/myrepository/");

        URL url = service.buildArtifactUrl(repositoryUrl, "myGroupId", "myArtifactId", "0.myVersion.0", "myFileName");

        assertThat(url.toString()).doesNotContain("//myGroupId");
    }

    @Test
    public void testAuthenticationHeaderAddedWhenCredentialsProvided() throws Exception {
        String username = "testuser";
        String password = "testpassword";
        
        MockMavenRepository mockRepo = new MockMavenRepository();
        mockRepo.setCredentials(username, password);
        
        GalasaMavenUrlHandlerService service = new GalasaMavenUrlHandlerService(mockRepo);
        MockURLConnection mockConnection = new MockURLConnection(new URL("http://example.com"));
        
        service.addAuthenticationIfRequired(mockConnection);
        
        String expectedAuth = username + ":" + password;
        String expectedEncodedAuth = Base64.getEncoder().encodeToString(expectedAuth.getBytes(StandardCharsets.UTF_8));
        String expectedHeader = "Basic " + expectedEncodedAuth;
        
        assertThat(mockConnection.getRequestProperty("Authorization")).isEqualTo(expectedHeader);
    }

    @Test
    public void testAuthenticationHeaderNotAddedWhenNoCredentials() throws Exception {
        MockMavenRepository mockRepo = new MockMavenRepository();
        
        GalasaMavenUrlHandlerService service = new GalasaMavenUrlHandlerService(mockRepo);
        MockURLConnection mockConnection = new MockURLConnection(new URL("http://example.com"));
        
        service.addAuthenticationIfRequired(mockConnection);
        
        assertThat(mockConnection.getRequestProperty("Authorization")).isNull();
    }

    @Test
    public void testAuthenticationHeaderNotAddedWhenOnlyUsernameProvided() throws Exception {
        MockMavenRepository mockRepo = new MockMavenRepository();
        mockRepo.setUsername("testuser");
        
        GalasaMavenUrlHandlerService service = new GalasaMavenUrlHandlerService(mockRepo);
        MockURLConnection mockConnection = new MockURLConnection(new URL("http://example.com"));
        
        service.addAuthenticationIfRequired(mockConnection);
        
        assertThat(mockConnection.getRequestProperty("Authorization")).isNull();
    }

    @Test
    public void testAuthenticationHeaderNotAddedWhenOnlyPasswordProvided() throws Exception {
        MockMavenRepository mockRepo = new MockMavenRepository();
        mockRepo.setPassword("testpassword");
        
        GalasaMavenUrlHandlerService service = new GalasaMavenUrlHandlerService(mockRepo);
        MockURLConnection mockConnection = new MockURLConnection(new URL("http://example.com"));
        
        service.addAuthenticationIfRequired(mockConnection);
        
        assertThat(mockConnection.getRequestProperty("Authorization")).isNull();
    }

    @Test
    public void testAuthenticationEncodesCredentialsCorrectlyWithSpecialCharacters() throws Exception {
        String username = "user@example.com";
        String password = "p@ssw0rd!#$%";
        
        MockMavenRepository mockRepo = new MockMavenRepository();
        mockRepo.setCredentials(username, password);
        
        GalasaMavenUrlHandlerService service = new GalasaMavenUrlHandlerService(mockRepo);
        MockURLConnection mockConnection = new MockURLConnection(new URL("http://example.com"));
        
        service.addAuthenticationIfRequired(mockConnection);
        
        String authHeader = mockConnection.getRequestProperty("Authorization");
        assertThat(authHeader).isNotNull();
        assertThat(authHeader).startsWith("Basic ");
        
        String encodedPart = authHeader.substring(6);
        String decoded = new String(Base64.getDecoder().decode(encodedPart), StandardCharsets.UTF_8);
        assertThat(decoded).isEqualTo(username + ":" + password);
    }

    @Test
    public void testAuthenticationHandlesColonsInCredentials() throws Exception {
        String username = "user:with:colons";
        String password = "pass:word:too";
        
        MockMavenRepository mockRepo = new MockMavenRepository();
        mockRepo.setCredentials(username, password);
        
        GalasaMavenUrlHandlerService service = new GalasaMavenUrlHandlerService(mockRepo);
        MockURLConnection mockConnection = new MockURLConnection(new URL("http://example.com"));
        
        service.addAuthenticationIfRequired(mockConnection);
        
        String authHeader = mockConnection.getRequestProperty("Authorization");
        assertThat(authHeader).isNotNull();
        
        String encodedPart = authHeader.substring(6);
        String decoded = new String(Base64.getDecoder().decode(encodedPart), StandardCharsets.UTF_8);
        assertThat(decoded).isEqualTo(username + ":" + password);
    }

    @Test
    public void testAuthenticationWithEmptyStringCredentialsTreatedAsNull() throws Exception {
        MockMavenRepository mockRepo = new MockMavenRepository();
        mockRepo.setUsername("");
        mockRepo.setPassword("");
        
        GalasaMavenUrlHandlerService service = new GalasaMavenUrlHandlerService(mockRepo);
        MockURLConnection mockConnection = new MockURLConnection(new URL("http://example.com"));
        
        service.addAuthenticationIfRequired(mockConnection);
        
        assertThat(mockConnection.getRequestProperty("Authorization")).isNull();
    }

    @Test
    public void testAuthenticationWithVeryLongCredentials() throws Exception {
        String username = "a".repeat(1000);
        String password = "b".repeat(1000);
        
        MockMavenRepository mockRepo = new MockMavenRepository();
        mockRepo.setCredentials(username, password);
        
        GalasaMavenUrlHandlerService service = new GalasaMavenUrlHandlerService(mockRepo);
        MockURLConnection mockConnection = new MockURLConnection(new URL("http://example.com"));
        
        service.addAuthenticationIfRequired(mockConnection);
        
        String authHeader = mockConnection.getRequestProperty("Authorization");
        assertThat(authHeader).isNotNull();
        
        String encodedPart = authHeader.substring(6);
        String decoded = new String(Base64.getDecoder().decode(encodedPart), StandardCharsets.UTF_8);
        assertThat(decoded).isEqualTo(username + ":" + password);
    }
}
