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
public class ActionsRouteTest {

    protected static final GalasaGson gson = new GalasaGson();
    protected static final Map<String, String> REQUEST_HEADERS = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);
    
    @Test
    public void testActionsRouteGetReturnsActionBeansOk() throws Exception {
        
        Action action1 = new MockAction("action1Id","action1Name","action1Description");
        Action action2 = new MockAction("action2Id","action2Name","action2Description");

        List<Action> actions = List.of(action1,action2);

        Role role1 = new MockRole("myRole1Id","myRole1Name","Description of myRole1Name", List.of(action1.getId(), action2.getId()));
        List<Role> roles = List.of(role1);

        MockRBACService rbacService = new MockRBACService(roles , actions, role1);
        MockTimeService timeService = new MockTimeService(Instant.EPOCH);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", REQUEST_HEADERS);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ResponseBuilder respBuilder = new ResponseBuilder();
        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();
        ActionsRoute route = new ActionsRoute(respBuilder, rbacService, env, timeService);

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
            JsonObject action1MetadataObj = new JsonObject();
            action1MetadataObj.addProperty("url","http://mock.galasa.server/action1Id");
            action1MetadataObj.addProperty("name","action1Name");
            action1MetadataObj.addProperty("id","action1Id");
            action1MetadataObj.addProperty("description","action1Description");
            expectedJson.add(action1MetadataObj);
        }

        {
            JsonObject action2MetadataObj = new JsonObject();
            action2MetadataObj.addProperty("url","http://mock.galasa.server/action2Id");
            action2MetadataObj.addProperty("name","action2Name");
            action2MetadataObj.addProperty("id","action2Id");
            action2MetadataObj.addProperty("description","action2Description");

            expectedJson.add(action2MetadataObj);
        }


        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(output).isEqualTo(gson.toJson(expectedJson));

    }
}
