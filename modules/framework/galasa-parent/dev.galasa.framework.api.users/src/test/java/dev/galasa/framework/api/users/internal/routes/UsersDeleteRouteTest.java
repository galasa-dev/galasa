/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.users.internal.routes;

import java.time.Instant;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.HttpMethod;
import dev.galasa.framework.api.common.InternalUser;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.mocks.FilledMockRBACService;
import dev.galasa.framework.mocks.MockTimeService;
import dev.galasa.framework.api.users.mocks.MockUsersServlet;
import dev.galasa.framework.auth.spi.internal.AuthService;
import dev.galasa.framework.auth.spi.mocks.MockAuthStoreService;
import dev.galasa.framework.auth.spi.mocks.MockDexGrpcClient;
import dev.galasa.framework.auth.spi.mocks.MockFrontEndClient;
import dev.galasa.framework.auth.spi.mocks.MockUser;
import dev.galasa.framework.spi.auth.IInternalUser;

public class UsersDeleteRouteTest extends BaseServletTest {

    Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

    private static final Pattern pattern = Pattern.compile(UsersDeleteRoute.path);

    @Test
    public void testUsersDeleteRequestReturnsNotFoundDueToMissingUserDoc() throws Exception {
        // Given...
        MockEnvironment env = new MockEnvironment();
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockAuthStoreService authStoreService = new MockAuthStoreService(mockTimeService);

        String baseUrl = "http://my.server/api";

        String userNumber = "admin";

        env.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, "preferred_username");
        env.setenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL,baseUrl);
        MockUsersServlet servlet = new MockUsersServlet(new AuthService(authStoreService, null), env, FilledMockRBACService.createTestRBACService());

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + userNumber, headerMap);
        mockRequest.setMethod(HttpMethod.DELETE.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();      

        // When...
        servlet.init();
        servlet.doDelete(mockRequest, servletResponse);

        assertThat(servletResponse.getStatus()).isEqualTo(404);
        checkErrorStructure(outStream.toString(), 5083, "GAL5083E",
            "Unable to retrieve a user with the given user number. No such user exists. Check your request query parameters and try again.");
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
    }

    @Test
    public void testUsersDeletesAUserReturnsOK() throws Exception {
        // Given...
        MockEnvironment env = new MockEnvironment();
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockAuthStoreService authStoreService = new MockAuthStoreService(mockTimeService);

        String baseUrl = "http://my.server/api";

        String userNumber = "docid";

        env.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, "preferred_username");
        env.setenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL,baseUrl);
        MockUsersServlet servlet = new MockUsersServlet(new AuthService(authStoreService, null), env, FilledMockRBACService.createTestRBACService());

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + userNumber, headerMap);
        mockRequest.setMethod(HttpMethod.DELETE.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();     
        
        MockUser mockUser1 = createMockUser("user-1", "docid", "web-ui");
        authStoreService.addUser(mockUser1);

        // When...
        servlet.init();
        servlet.doDelete(mockRequest, servletResponse);

        assertThat(servletResponse.getStatus()).isEqualTo(204);
        assertThat(authStoreService.getUser(userNumber)).isNull();
    }

    @Test
    public void testUsersDeletesAUserWhenMultipleUsersPresentReturnsOK() throws Exception {
        // Given...
        MockEnvironment env = new MockEnvironment();
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockAuthStoreService authStoreService = new MockAuthStoreService(mockTimeService);

        String baseUrl = "http://my.server/api";

        String userNumber = "docid-2";

        env.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, "preferred_username");
        env.setenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL,baseUrl);
        MockUsersServlet servlet = new MockUsersServlet(new AuthService(authStoreService, null), env, FilledMockRBACService.createTestRBACService());

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + userNumber, headerMap);
        mockRequest.setMethod(HttpMethod.DELETE.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();     
        
        MockUser mockUser1 = createMockUser("user-1", "docid", "web-ui");
        MockUser mockUser2 = createMockUser("user-2", "docid-2", "web-ui");
        MockUser mockUser3 = createMockUser("user-3", "docid-3", "web-ui");
        authStoreService.addUser(mockUser1);
        authStoreService.addUser(mockUser2);
        authStoreService.addUser(mockUser3);

        // When...
        servlet.init();
        servlet.doDelete(mockRequest, servletResponse);

        assertThat(servletResponse.getStatus()).isEqualTo(204);
        assertThat(authStoreService.getUser(userNumber)).isNull();
        assertThat(authStoreService.getAllUsers()).hasSize(2);
    }

    @Test
    public void testUsersDeletesAUserAndTheirAccessTokensReturnsOK() throws Exception {
        // Given...
        MockEnvironment env = new MockEnvironment();
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockAuthStoreService authStoreService = new MockAuthStoreService(mockTimeService);

        MockDexGrpcClient mockDexGrpcClient = new MockDexGrpcClient("http://my-issuer");

        String baseUrl = "http://my.server/api";
        String userNumber = "docid";

        env.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, "preferred_username");
        env.setenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL,baseUrl);
        MockUsersServlet servlet = new MockUsersServlet(new AuthService(authStoreService, mockDexGrpcClient), env, FilledMockRBACService.createTestRBACService());

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + userNumber, headerMap);
        mockRequest.setMethod(HttpMethod.DELETE.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();     

        IInternalUser owner = new InternalUser("user-1", "dexId");
        authStoreService.storeToken("some-client-id", "test-token", owner);
        
        MockUser mockUser1 = createMockUser("user-1", "docid", "web-ui");
        authStoreService.addUser(mockUser1);

        // When...
        servlet.init();
        servlet.doDelete(mockRequest, servletResponse);

        assertThat(servletResponse.getStatus()).isEqualTo(204);
        assertThat(authStoreService.getUser(userNumber)).isNull();
        assertThat(authStoreService.getTokensByLoginId("user-1")).hasSize(0);
    }


    /// ---------------------------------------
    /// ---------------------------------------
    /// REGEX TESTS
    /// ---------------------------------------
    /// ---------------------------------------
    
    private boolean matchesPattern(String input) {
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    @Test
    public void testValidPathWithoutTrailingSlash() throws Exception {
        assertThat(matchesPattern("/test123")).isTrue();
    }

    @Test
    public void testValidPathWithTrailingSlash() {
        assertThat(matchesPattern("/test123/")).isTrue();
    }

    @Test
    public void testPathWithDash() {
        assertThat(matchesPattern("/test-123/")).isTrue();
    }

    @Test
    public void testPathWithUnderscore() {
        assertThat(matchesPattern("/test_123")).isTrue();
    }

    @Test
    public void testPathWithUppercaseCharacters() {
        assertThat(matchesPattern("/TEST")).isTrue();
    }

    @Test
    public void testEmptyPath() {
        assertThat(matchesPattern("/")).isFalse();
    }

    @Test
    public void testPathWithMixedCharacters() {
        assertThat(matchesPattern("//Test-123_Abc")).isFalse();
    }

    @Test
    public void testInvalidPathWithSpecialCharacters() {
        assertThat(matchesPattern("/test!123")).isFalse();
    }

    @Test
    public void testInvalidPathWithMultipleSegments() {
        assertThat(matchesPattern("/test/123")).isFalse();
    }

    @Test
    public void testInvalidPathWithMultipleSpecialChars() {
        assertThat(matchesPattern("/@..#2231!")).isFalse();
    }

    @Test
    public void testInvalidPathWithoutLeadingSlash() {
        assertThat(matchesPattern("test123")).isFalse();
    }

    

    private MockUser createMockUser(String loginId, String userNumber, String clientName){

        MockFrontEndClient newClient = new MockFrontEndClient("web-ui");
        newClient.name = clientName;
        newClient.lastLoginTime = Instant.parse("2024-10-18T14:49:50.096329Z");
            

        MockUser mockUser = new MockUser();
        mockUser.userNumber = userNumber;
        mockUser.loginId = loginId;
        mockUser.addClient(newClient);

        return mockUser;

    }

}
