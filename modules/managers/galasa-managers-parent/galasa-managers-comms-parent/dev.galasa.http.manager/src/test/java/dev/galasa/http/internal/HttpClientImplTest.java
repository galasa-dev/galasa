/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.http.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.List;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.junit.Before;
import org.junit.Test;

import dev.galasa.http.mocks.MockHttpRequestExecutor;
import dev.galasa.http.mocks.MockLog;

/**
 * Unit tests for HttpClientImpl class focusing on setAuthorisation and execute methods
 */
public class HttpClientImplTest {

    private HttpClientImpl httpClient;
    private MockHttpRequestExecutor mockHttpRequestExecutor;
    private static final int DEFAULT_TIMEOUT = 5000;

    @Before
    public void setUp() throws Exception {
        MockLog mockLog = new MockLog();
        mockHttpRequestExecutor = new MockHttpRequestExecutor();

        httpClient = new HttpClientImpl(DEFAULT_TIMEOUT, mockLog, mockHttpRequestExecutor);
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
    }

    @Test
    public void testCloseHttpClientWhenNotBuilt() throws Exception {
        // When
        httpClient.close();

        // Then - should not throw exception
    }

    @Test
    public void testCanBuildUriWithSingleKeyValueQueryParameter() throws Exception {
        // Given...
        ClassicHttpResponse mockResponse = new BasicClassicHttpResponse(200);
        mockHttpRequestExecutor.setMockResponse(mockResponse);

        // When...
        httpClient.getText("/helloworld?key1=value1");

        // Then...
        List<ClassicHttpRequest> requests = mockHttpRequestExecutor.getRequests();
        assertThat(requests).hasSize(1);

        ClassicHttpRequest request = requests.get(0);
        URI uri = request.getUri();
        assertThat(uri.getQuery()).isEqualTo("key1=value1");
    }

    @Test
    public void testCanBuildUriWithSingleKeyQueryParameter() throws Exception {
        // Given...
        ClassicHttpResponse mockResponse = new BasicClassicHttpResponse(200);
        mockHttpRequestExecutor.setMockResponse(mockResponse);

        // When...
        httpClient.getText("/helloworld?key1");

        // Then...
        List<ClassicHttpRequest> requests = mockHttpRequestExecutor.getRequests();
        assertThat(requests).hasSize(1);

        ClassicHttpRequest request = requests.get(0);
        URI uri = request.getUri();
        assertThat(uri.getQuery()).isEqualTo("key1");
    }

    @Test
    public void testCanBuildUriWithMultipleKeyValueQueryParameters() throws Exception {
        // Given...
        ClassicHttpResponse mockResponse = new BasicClassicHttpResponse(200);
        mockHttpRequestExecutor.setMockResponse(mockResponse);

        // When...
        httpClient.getText("/api/users?status=active&role=admin&limit=10");

        // Then...
        List<ClassicHttpRequest> requests = mockHttpRequestExecutor.getRequests();
        assertThat(requests).hasSize(1);

        ClassicHttpRequest request = requests.get(0);
        URI uri = request.getUri();
        assertThat(uri.getQuery()).contains("status=active");
        assertThat(uri.getQuery()).contains("role=admin");
        assertThat(uri.getQuery()).contains("limit=10");
    }

    @Test
    public void testCanBuildUriWithEncodedQueryParameters() throws Exception {
        // Given...
        ClassicHttpResponse mockResponse = new BasicClassicHttpResponse(200);
        mockHttpRequestExecutor.setMockResponse(mockResponse);

        // When...
        httpClient.getText("/api/search?q=hello%20world&email=user%40example.com");

        // Then...
        List<ClassicHttpRequest> requests = mockHttpRequestExecutor.getRequests();
        assertThat(requests).hasSize(1);

        ClassicHttpRequest request = requests.get(0);
        URI uri = request.getUri();
        assertThat(uri.getRawQuery()).contains("q=hello%20world");
        assertThat(uri.getRawQuery()).contains("email=user%40example.com");
    }

    @Test
    public void testCanBuildUriWithMultipleValuesForSameParameter() throws Exception {
        // Given...
        ClassicHttpResponse mockResponse = new BasicClassicHttpResponse(200);
        mockHttpRequestExecutor.setMockResponse(mockResponse);

        // When...
        httpClient.getText("/api/users?id=1&id=2&id=3");

        // Then...
        List<ClassicHttpRequest> requests = mockHttpRequestExecutor.getRequests();
        assertThat(requests).hasSize(1);

        ClassicHttpRequest request = requests.get(0);
        URI uri = request.getUri();
        String query = uri.getQuery();
        assertThat(query).contains("id=1");
        assertThat(query).contains("id=2");
        assertThat(query).contains("id=3");
    }

    @Test
    public void testCanBuildUriWithMixedKeyAndKeyValueParameters() throws Exception {
        // Given...
        ClassicHttpResponse mockResponse = new BasicClassicHttpResponse(200);
        mockHttpRequestExecutor.setMockResponse(mockResponse);

        // When...
        httpClient.getText("/api/users?active&status=verified&limit=5");

        // Then...
        List<ClassicHttpRequest> requests = mockHttpRequestExecutor.getRequests();
        assertThat(requests).hasSize(1);

        ClassicHttpRequest request = requests.get(0);
        URI uri = request.getUri();
        String query = uri.getQuery();
        assertThat(query).contains("active");
        assertThat(query).contains("status=verified");
        assertThat(query).contains("limit=5");
    }

    @Test
    public void testCanBuildUriWithEmptyPath() throws Exception {
        // Given...
        ClassicHttpResponse mockResponse = new BasicClassicHttpResponse(200);
        mockHttpRequestExecutor.setMockResponse(mockResponse);

        // When...
        httpClient.getText("");

        // Then...
        List<ClassicHttpRequest> requests = mockHttpRequestExecutor.getRequests();
        assertThat(requests).hasSize(1);

        ClassicHttpRequest request = requests.get(0);
        URI uri = request.getUri();
        assertThat(uri.getPath()).isNotNull();
    }

    @Test
    public void testCanBuildUriWithRootPath() throws Exception {
        // Given...
        ClassicHttpResponse mockResponse = new BasicClassicHttpResponse(200);
        mockHttpRequestExecutor.setMockResponse(mockResponse);

        // When...
        httpClient.getText("/");

        // Then...
        List<ClassicHttpRequest> requests = mockHttpRequestExecutor.getRequests();
        assertThat(requests).hasSize(1);

        ClassicHttpRequest request = requests.get(0);
        URI uri = request.getUri();
        assertThat(uri.getPath()).isEqualTo("/");
    }

    @Test
    public void testCanBuildUriWithPathNotStartingWithSlash() throws Exception {
        // Given...
        ClassicHttpResponse mockResponse = new BasicClassicHttpResponse(200);
        mockHttpRequestExecutor.setMockResponse(mockResponse);

        // When...
        httpClient.getText("api/users");

        // Then...
        List<ClassicHttpRequest> requests = mockHttpRequestExecutor.getRequests();
        assertThat(requests).hasSize(1);

        ClassicHttpRequest request = requests.get(0);
        URI uri = request.getUri();
        assertThat(uri.getPath()).startsWith("/");
        assertThat(uri.getPath()).contains("api/users");
    }

    @Test
    public void testCanBuildUriWithComplexPath() throws Exception {
        // Given...
        ClassicHttpResponse mockResponse = new BasicClassicHttpResponse(200);
        mockHttpRequestExecutor.setMockResponse(mockResponse);

        // When...
        httpClient.getText("/api/v1/organizations/123/projects/456/users");

        // Then...
        List<ClassicHttpRequest> requests = mockHttpRequestExecutor.getRequests();
        assertThat(requests).hasSize(1);

        ClassicHttpRequest request = requests.get(0);
        URI uri = request.getUri();
        assertThat(uri.getPath()).isEqualTo("/api/v1/organizations/123/projects/456/users");
    }

    @Test
    public void testCanBuildUriWithSpecialCharactersInPath() throws Exception {
        // Given...
        ClassicHttpResponse mockResponse = new BasicClassicHttpResponse(200);
        mockHttpRequestExecutor.setMockResponse(mockResponse);

        // When...
        httpClient.getText("/api/users/john.doe@example.com");

        // Then...
        List<ClassicHttpRequest> requests = mockHttpRequestExecutor.getRequests();
        assertThat(requests).hasSize(1);

        ClassicHttpRequest request = requests.get(0);
        URI uri = request.getUri();
        assertThat(uri.getPath()).contains("john.doe@example.com");
    }

    @Test
    public void testCanBuildUriWithQueryParameterContainingAmpersand() throws Exception {
        // Given...
        ClassicHttpResponse mockResponse = new BasicClassicHttpResponse(200);
        mockHttpRequestExecutor.setMockResponse(mockResponse);

        // When...
        httpClient.getText("/api/search?q=foo%26bar");

        // Then...
        List<ClassicHttpRequest> requests = mockHttpRequestExecutor.getRequests();
        assertThat(requests).hasSize(1);

        ClassicHttpRequest request = requests.get(0);
        URI uri = request.getUri();
        assertThat(uri.getRawQuery()).isEqualTo("q=foo%26bar");
    }

    @Test
    public void testCanBuildUriWithQueryParameterContainingEquals() throws Exception {
        // Given...
        ClassicHttpResponse mockResponse = new BasicClassicHttpResponse(200);
        mockHttpRequestExecutor.setMockResponse(mockResponse);

        // When...
        httpClient.getText("/api/search?filter=type%3Duser");

        // Then...
        List<ClassicHttpRequest> requests = mockHttpRequestExecutor.getRequests();
        assertThat(requests).hasSize(1);

        ClassicHttpRequest request = requests.get(0);
        URI uri = request.getUri();
        assertThat(uri.getRawQuery()).isEqualTo("filter=type%3Duser");
    }

    @Test
    public void testCanBuildUriWithPlusSignInQueryParameter() throws Exception {
        // Given...
        ClassicHttpResponse mockResponse = new BasicClassicHttpResponse(200);
        mockHttpRequestExecutor.setMockResponse(mockResponse);

        // When...
        httpClient.getText("/api/search?name=John+Doe");

        // Then...
        List<ClassicHttpRequest> requests = mockHttpRequestExecutor.getRequests();
        assertThat(requests).hasSize(1);

        ClassicHttpRequest request = requests.get(0);
        URI uri = request.getUri();
        assertThat(uri.getQuery()).isEqualTo("name=John+Doe");
    }

    @Test
    public void testCanBuildUriWithEmptyQueryParameterValue() throws Exception {
        // Given...
        ClassicHttpResponse mockResponse = new BasicClassicHttpResponse(200);
        mockHttpRequestExecutor.setMockResponse(mockResponse);

        // When...
        httpClient.getText("/api/users?status=&role=admin");

        // Then...
        List<ClassicHttpRequest> requests = mockHttpRequestExecutor.getRequests();
        assertThat(requests).hasSize(1);

        ClassicHttpRequest request = requests.get(0);
        URI uri = request.getUri();
        String query = uri.getQuery();
        assertThat(query).contains("status=");
        assertThat(query).contains("role=admin");
    }

    @Test
    public void testBuildUriWithCompleteUrlInPath() throws Exception {
        // Given...
        // Create a client WITHOUT setting a host (host is null)
        httpClient.build();

        ClassicHttpResponse mockResponse = new BasicClassicHttpResponse(200);
        mockHttpRequestExecutor.setMockResponse(mockResponse);

        // When...
        // Pass a complete URL in the path parameter
        httpClient.getText("http://example.com/api/users");

        // Then...
        List<ClassicHttpRequest> requests = mockHttpRequestExecutor.getRequests();
        assertThat(requests).hasSize(1);

        ClassicHttpRequest request = requests.get(0);
        URI uri = request.getUri();
        assertThat(uri.getScheme()).isEqualTo("http");
        assertThat(uri.getHost()).isEqualTo("example.com");
        assertThat(uri.getPath()).isEqualTo("/api/users");
    }

    @Test
    public void testBuildUriWithCompleteUrlAndQueryParams() throws Exception {
        // Given...
        // Create a client WITHOUT setting a host (host is null)
        MockLog mockLog = new MockLog();
        HttpClientImpl client = new HttpClientImpl(DEFAULT_TIMEOUT, mockLog, mockHttpRequestExecutor);

        ClassicHttpResponse mockResponse = new BasicClassicHttpResponse(200);
        mockHttpRequestExecutor.setMockResponse(mockResponse);

        // When...
        // Pass a complete URL with query parameters in the path
        client.getText("https://api.example.com:8080/api/users?status=active&role=admin");

        // Then...
        List<ClassicHttpRequest> requests = mockHttpRequestExecutor.getRequests();
        assertThat(requests).hasSize(1);

        ClassicHttpRequest request = requests.get(0);
        URI uri = request.getUri();
        assertThat(uri.getScheme()).isEqualTo("https");
        assertThat(uri.getHost()).isEqualTo("api.example.com");
        assertThat(uri.getPort()).isEqualTo(8080);
        assertThat(uri.getPath()).isEqualTo("/api/users");
        
        String query = uri.getQuery();
        assertThat(query).contains("status=active");
        assertThat(query).contains("role=admin");
    }

    @Test
    public void testBuildUriWithCompleteUrlOverridesHost() throws Exception {
        // Given...
        // Create a client WITH a host set
        httpClient.setURI(URI.create("http://default-host.com"));

        ClassicHttpResponse mockResponse = new BasicClassicHttpResponse(200);
        mockHttpRequestExecutor.setMockResponse(mockResponse);

        // When...
        // Pass a complete URL that should override the default host
        httpClient.getText("http://override-host.com/api/data");

        // Then...
        List<ClassicHttpRequest> requests = mockHttpRequestExecutor.getRequests();
        assertThat(requests).hasSize(1);

        ClassicHttpRequest request = requests.get(0);
        URI uri = request.getUri();
        // The complete URL in path should override the default host
        assertThat(uri.getHost()).isEqualTo("override-host.com");
        assertThat(uri.getPath()).isEqualTo("/api/data");
    }

    @Test
    public void testBuildUriWithRelativePathUsesDefaultHost() throws Exception {
        // Given...
        httpClient.setURI(URI.create("http://default-host.com"));
        httpClient.build();

        ClassicHttpResponse mockResponse = new BasicClassicHttpResponse(200);
        mockHttpRequestExecutor.setMockResponse(mockResponse);

        // When...
        // Pass a relative path (no scheme/host)
        httpClient.getText("/api/users?id=123");

        // Then...
        List<ClassicHttpRequest> requests = mockHttpRequestExecutor.getRequests();
        assertThat(requests).hasSize(1);

        ClassicHttpRequest request = requests.get(0);
        URI uri = request.getUri();
        // Should use the default host
        assertThat(uri.getHost()).isEqualTo("default-host.com");
        assertThat(uri.getPath()).isEqualTo("/api/users");
        
        String query = uri.getQuery();
        assertThat(query).isEqualTo("id=123");
    }
}

