/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.users.internal.routes;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletOutputStream;

import java.time.Instant;
import org.junit.Test;

import dev.galasa.framework.api.beans.generated.FrontEndClient;
import dev.galasa.framework.api.beans.generated.UserData;
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.mocks.MockAuthStoreService;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockFrontEndClient;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.common.mocks.MockTimeService;
import dev.galasa.framework.api.common.mocks.MockUser;
import dev.galasa.framework.api.users.mocks.MockUsersServlet;
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
        MockFramework framework = new MockFramework(authStoreService);

        String baseUrl = "http://my.server/api";


        Map<String, String[]> queryParams = new HashMap<>();

        queryParams.put("loginId", new String[] { "user-1" });

        env.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, "preferred_username");
        env.setenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL,baseUrl);
        MockUsersServlet servlet = new MockUsersServlet(framework, env);


        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams,null, headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        MockUser mockUser1 = createMockUser("user-1", "docid", "web-ui");
        authStoreService.addUser(mockUser1);
    
        MockUser mockUser2 = createMockUser("user-2", "docid-2", "rest-api");
        authStoreService.addUser(mockUser2);

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);


        UserData user1GotBack = createUserGotBack("user-1","docid","web-ui", baseUrl);

        List<UserData> users = List.of(user1GotBack);

        String gotBackPayload = outStream.toString();
        String expectedPayload = gson.toJson(users);

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(gotBackPayload).isEqualTo(expectedPayload);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
    }

    @Test
    public void testUsersGetRequestReturnsAllUsersReturnsOK() throws Exception {
        // Given...
        MockEnvironment env = new MockEnvironment();
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockAuthStoreService authStoreService = new MockAuthStoreService(mockTimeService);
        MockFramework framework = new MockFramework(authStoreService);

        String baseUrl = "http://my.server/api";

        env.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, "preferred_username");
        env.setenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL,baseUrl);
        MockUsersServlet servlet = new MockUsersServlet(framework, env);


        MockHttpServletRequest mockRequest = new MockHttpServletRequest(null, headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        
        MockUser mockUser1 = createMockUser("user-1", "docid", "web-ui");
        authStoreService.addUser(mockUser1);
    
        MockUser mockUser2 = createMockUser("user-2", "docid-2", "rest-api");
        authStoreService.addUser(mockUser2);
        

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        UserData user1GotBack = createUserGotBack("user-1","docid","web-ui", baseUrl);
        UserData user2GotBack = createUserGotBack("user-2","docid-2","rest-api", baseUrl);

        List<UserData> users = List.of(user1GotBack, user2GotBack);

        String gotBackPayload = outStream.toString();
        String expectedPayload = gson.toJson(users);

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(gotBackPayload).isEqualTo(expectedPayload);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
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

    private UserData createUserGotBack(String loginId, String userNumber, String clientName, String baseUrl){
        FrontEndClient[] user1clients = new FrontEndClient[1];
        FrontEndClient newClient = new FrontEndClient();
        newClient.setClientName(clientName);
        newClient.setLastLogin("2024-10-18T14:49:50.096329Z");
        user1clients[0] = newClient;

        UserData userData = new UserData();
        userData.setid(userNumber);
        userData.setLoginId(loginId);
        userData.setclients(user1clients);
        userData.seturl(baseUrl + "/users/" + userData.getid());

        return userData;
    }

}
