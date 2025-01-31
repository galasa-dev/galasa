/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.users.internal.routes;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import java.time.Instant;
import org.junit.Test;

import dev.galasa.framework.api.beans.generated.FrontEndClient;
import dev.galasa.framework.api.beans.generated.RBACRole;
import dev.galasa.framework.api.beans.generated.RBACRoleMetadata;
import dev.galasa.framework.api.beans.generated.UserData;
import dev.galasa.framework.api.beans.generated.UserSynthetics;
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.mocks.FilledMockRBACService;
import dev.galasa.framework.mocks.MockAuthStoreService;
import dev.galasa.framework.mocks.MockFrontEndClient;
import dev.galasa.framework.mocks.MockRBACService;
import dev.galasa.framework.mocks.MockTimeService;
import dev.galasa.framework.mocks.MockUser;
import dev.galasa.framework.api.users.mocks.MockUsersServlet;
import dev.galasa.framework.auth.spi.IAuthService;
import dev.galasa.framework.auth.spi.internal.AuthService;
import dev.galasa.framework.spi.utils.GalasaGson;


public class UsersRouteTest extends BaseServletTest {

    Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

    GalasaGson gson = new GalasaGson();
    
    @Test
    public void testUsersGetRequestReturnsUserByLoginIdReturnsOK() throws Exception {
        // Given...
        MockEnvironment env = new MockEnvironment();
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockAuthStoreService authStoreService = new MockAuthStoreService(mockTimeService);
        MockRBACService rbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        IAuthService authService = new AuthService(authStoreService, null, rbacService);

        String baseUrl = "http://my.server/api";


        Map<String, String[]> queryParams = new HashMap<>();

        queryParams.put("loginId", new String[] { "user-1" });

        env.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, "preferred_username");
        env.setenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL,baseUrl);
        MockUsersServlet servlet = new MockUsersServlet(authService, env, rbacService );


        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams,null, headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        MockUser mockUser1 = createMockUser("user-1", "docid", "web-ui", rbacService.getDefaultRoleId());
        authStoreService.addUser(mockUser1);
    
        MockUser mockUser2 = createMockUser("user-2", "docid-2", "rest-api", rbacService.getDefaultRoleId());
        authStoreService.addUser(mockUser2);

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);


        // Then...
        // Check the response we got back was correct.
        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");

        // Now check the payload json.
        String gotBackPayload = outStream.toString();
        UserData[] usersGotBack = gson.fromJson(gotBackPayload, UserData[].class);

        // Should be two users returned.
        assertThat(usersGotBack).hasSize(1);

        UserData userGotBack = usersGotBack[0];
        {
            assertThat(userGotBack.getLoginId()).isEqualTo("user-1");
            assertThat(userGotBack.getid()).isEqualTo("docid");
            assertThat(userGotBack.geturl()).isEqualTo(baseUrl + "/users/" + userGotBack.getid());
            assertThat(userGotBack.getrole()).isEqualTo( rbacService.getDefaultRoleId() );
            FrontEndClient[] clients = userGotBack.getclients();

            assertThat(clients).hasSize(1);
            assertThat(clients[0].getClientName()).isEqualTo("web-ui");
            assertThat(clients[0].getLastLogin()).isEqualTo("2024-10-18T14:49:50.096329Z");

            UserSynthetics synthetics = userGotBack.getsynthetic();
            assertThat(synthetics).isNotNull();
            RBACRole role = synthetics.getrole();
            assertThat(role).isNotNull();
            assertThat(role.getkind()).isEqualTo("GalasaRole");
            RBACRoleMetadata roleMetadata = role.getmetadata();
            assertThat(roleMetadata).isNotNull();
            assertThat(roleMetadata.getname()).isEqualTo( rbacService.getRoleById(rbacService.getDefaultRoleId()).getName());
            // ... we could check more role stuff but this test is aiming at checking the user stuff, and the role material seems to be there.
        }
    }

    @Test
    public void testUsersGetRequestReturnsAllUsersReturnsOK() throws Exception {
        // Given...
        MockEnvironment env = new MockEnvironment();
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockAuthStoreService authStoreService = new MockAuthStoreService(mockTimeService);
        MockRBACService rbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        IAuthService authService = new AuthService(authStoreService, null, rbacService);

        String baseUrl = "http://my.server/api";

        env.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, "preferred_username");
        env.setenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL,baseUrl);
        MockUsersServlet servlet = new MockUsersServlet(authService, env, rbacService);


        MockHttpServletRequest mockRequest = new MockHttpServletRequest(null, headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        
        MockUser mockUser1 = createMockUser("user-1", "docid", "web-ui", rbacService.getDefaultRoleId());
        authStoreService.addUser(mockUser1);
    
        MockUser mockUser2 = createMockUser("user-2", "docid-2", "rest-api", rbacService.getDefaultRoleId());
        authStoreService.addUser(mockUser2);
        

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);



        // Check the response we got back was correct.
        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");

        // Now check the payload json.
        String gotBackPayload = outStream.toString();
        UserData[] usersGotBack = gson.fromJson(gotBackPayload, UserData[].class);

        // Should be two users returned.
        assertThat(usersGotBack).hasSize(2);

        UserData userGotBack = usersGotBack[0];
        {
            assertThat(userGotBack.getLoginId()).isEqualTo("user-1");
            assertThat(userGotBack.getid()).isEqualTo("docid");
            assertThat(userGotBack.geturl()).isEqualTo(baseUrl + "/users/" + userGotBack.getid());
            assertThat(userGotBack.getrole()).isEqualTo( rbacService.getDefaultRoleId() );
            FrontEndClient[] clients = userGotBack.getclients();

            assertThat(clients).hasSize(1);
            assertThat(clients[0].getClientName()).isEqualTo("web-ui");
            assertThat(clients[0].getLastLogin()).isEqualTo("2024-10-18T14:49:50.096329Z");

            UserSynthetics synthetics = userGotBack.getsynthetic();
            assertThat(synthetics).isNotNull();
            RBACRole role = synthetics.getrole();
            assertThat(role).isNotNull();
            assertThat(role.getkind()).isEqualTo("GalasaRole");
            RBACRoleMetadata roleMetadata = role.getmetadata();
            assertThat(roleMetadata).isNotNull();
            assertThat(roleMetadata.getname()).isEqualTo( rbacService.getRoleById(rbacService.getDefaultRoleId()).getName());
            // ... we could check more role stuff but this test is aiming at checking the user stuff, and the role material seems to be there.
        }

        userGotBack = usersGotBack[1];
        {
            assertThat(userGotBack.getLoginId()).isEqualTo("user-2");
            assertThat(userGotBack.getid()).isEqualTo("docid-2");
            assertThat(userGotBack.geturl()).isEqualTo(baseUrl + "/users/" + userGotBack.getid());
            assertThat(userGotBack.getrole()).isEqualTo( rbacService.getDefaultRoleId() );
            FrontEndClient[] clients = userGotBack.getclients();

            assertThat(clients).hasSize(1);
            assertThat(clients[0].getClientName()).isEqualTo("rest-api");
            assertThat(clients[0].getLastLogin()).isEqualTo("2024-10-18T14:49:50.096329Z");

            UserSynthetics synthetics = userGotBack.getsynthetic();
            assertThat(synthetics).isNotNull();
            RBACRole role = synthetics.getrole();
            assertThat(role).isNotNull();
            assertThat(role.getkind()).isEqualTo("GalasaRole");
            RBACRoleMetadata roleMetadata = role.getmetadata();
            assertThat(roleMetadata).isNotNull();
            assertThat(roleMetadata.getname()).isEqualTo( rbacService.getRoleById(rbacService.getDefaultRoleId()).getName());
            // ... we could check more role stuff but this test is aiming at checking the user stuff, and the role material seems to be there.
        }
    }

    private MockUser createMockUser(String loginId, String userNumber, String clientName, String roleId ){

        MockFrontEndClient newClient = new MockFrontEndClient("web-ui");
        newClient.name = clientName;
        newClient.lastLoginTime = Instant.parse("2024-10-18T14:49:50.096329Z");
            

        MockUser mockUser = new MockUser();
        mockUser.userNumber = userNumber;
        mockUser.loginId = loginId;
        mockUser.addClient(newClient);
        mockUser.roleId = roleId ;

        return mockUser;

    }


}
