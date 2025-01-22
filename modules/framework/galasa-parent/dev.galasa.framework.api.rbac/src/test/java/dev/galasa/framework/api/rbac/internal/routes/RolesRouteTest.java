/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.rbac.internal.routes;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.mocks.FilledMockEnvironment;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.mocks.MockAction;
import dev.galasa.framework.mocks.MockRBACService;
import dev.galasa.framework.mocks.MockRole;
import dev.galasa.framework.mocks.MockTimeService;
import dev.galasa.framework.spi.rbac.Action;
import dev.galasa.framework.spi.rbac.Role;
import dev.galasa.framework.spi.utils.GalasaGson;
public class RolesRouteTest {

    protected static final GalasaGson gson = new GalasaGson();
    protected static final Map<String, String> REQUEST_HEADERS = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);
    
    @Test
    public void testRolesRouteGetReturnsRolesBeansOk() throws Exception {
        
        Action action1 = new MockAction("action1Id","action1Name","action1Description");
        Action action2 = new MockAction("action2Id","action2Name","action2Description");

        List<Action> actions = List.of(action1,action2);

        Role role1 = new MockRole("myRole1Name","myRole1Id","Description of myRole1Name", List.of(action1.getId(), action2.getId()));
        List<Role> roles = List.of(role1);

        MockRBACService rbacService = new MockRBACService(roles , actions, role1);
        MockTimeService timeService = new MockTimeService(Instant.EPOCH);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", REQUEST_HEADERS);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ResponseBuilder respBuilder = new ResponseBuilder();
        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();

        RolesRoute route = new RolesRoute(respBuilder, rbacService, env, timeService);

        QueryParameters queryParams = new QueryParameters(new HashMap<String,String[]>());
        String pathInfo = "myPathInfo";

        HttpRequestContext requestContext = new HttpRequestContext(mockRequest, env);
        
        // When...
        route.handleGetRequest(pathInfo, queryParams, requestContext, servletResponse);

        // Then...
        ServletOutputStream outStream = servletResponse.getOutputStream();
        String output = outStream.toString();

        JsonArray expectedJson = new JsonArray();

        {
            JsonObject roleObj = new JsonObject();
            roleObj.addProperty("kind", "GalasaRole");
            roleObj.addProperty("apiVersion","galasa-dev/v1alpha1");

            JsonObject role1MetadataObj = new JsonObject();
            role1MetadataObj.addProperty("url","http://mock.galasa.server/myRole1Id");
            role1MetadataObj.addProperty("name","myRole1Name");
            role1MetadataObj.addProperty("id","myRole1Id");
            role1MetadataObj.addProperty("description","Description of myRole1Name");

            roleObj.add( "metadata", role1MetadataObj);

            JsonArray actionsList = new JsonArray();
            actionsList.add("action1Id");
            actionsList.add("action2Id");

            JsonObject dataObj = new JsonObject();
            dataObj.add("actions",actionsList);

            roleObj.add("data",dataObj);

            expectedJson.add(roleObj);
        }

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(output).isEqualTo(gson.toJson(expectedJson));
    }


    @Test
    public void testRolesRouteGetReturnsRoleWhenAskedForByName() throws Exception {
        
        Action action1 = new MockAction("action1Id","action1Name","action1Description");
        Action action2 = new MockAction("action2Id","action2Name","action2Description");

        List<Action> actions = List.of(action1,action2);

        Role role1 = new MockRole("myRole1Name","myRole1Id","Description of myRole1Name", List.of(action1.getId(), action2.getId()));
        Role role2 = new MockRole("myRole2Name","myRole2Id","Description of myRole2Name", List.of(action1.getId()));
        List<Role> roles = List.of(role1, role2);

        MockRBACService rbacService = new MockRBACService(roles , actions, role1);
        MockTimeService timeService = new MockTimeService(Instant.EPOCH);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", REQUEST_HEADERS);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ResponseBuilder respBuilder = new ResponseBuilder();
        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();

        RolesRoute route = new RolesRoute(respBuilder, rbacService, env, timeService);

        Map<String,String[]> queryParamMap = new HashMap<String,String[]>();
        String[] nameQueryValue = new String[1];
        nameQueryValue[0] = "myRole2Name";
        queryParamMap.put("name",nameQueryValue);
        QueryParameters queryParams = new QueryParameters(queryParamMap);
        String pathInfo = "myPathInfo";

        HttpRequestContext requestContext = new HttpRequestContext(mockRequest, env);
        
        // When...
        route.handleGetRequest(pathInfo, queryParams, requestContext, servletResponse);

        // Then...
        ServletOutputStream outStream = servletResponse.getOutputStream();
        String output = outStream.toString();

        JsonArray expectedJson = new JsonArray();

        {
            JsonObject roleObj = new JsonObject();
            roleObj.addProperty("kind", "GalasaRole");
            roleObj.addProperty("apiVersion","galasa-dev/v1alpha1");

            JsonObject role1MetadataObj = new JsonObject();
            role1MetadataObj.addProperty("url","http://mock.galasa.server/myRole2Id");
            role1MetadataObj.addProperty("name","myRole2Name");
            role1MetadataObj.addProperty("id","myRole2Id");
            role1MetadataObj.addProperty("description","Description of myRole2Name");

            roleObj.add( "metadata", role1MetadataObj);

            JsonArray actionsList = new JsonArray();
            actionsList.add("action1Id");

            JsonObject dataObj = new JsonObject();
            dataObj.add("actions",actionsList);

            roleObj.add("data",dataObj);

            expectedJson.add(roleObj);
        }

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(output).isEqualTo(gson.toJson(expectedJson));
    }

}
