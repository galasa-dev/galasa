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

import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.mocks.MockAction;
import dev.galasa.framework.mocks.MockRBACService;
import dev.galasa.framework.mocks.MockRole;
import dev.galasa.framework.mocks.MockTimeService;
import dev.galasa.framework.spi.rbac.Action;
import dev.galasa.framework.spi.rbac.Role;
import dev.galasa.framework.spi.utils.GalasaGson;
public class ActionDetailsRouteTest {

    protected static final GalasaGson gson = new GalasaGson();
    protected static final Map<String, String> REQUEST_HEADERS = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);
    
    @Test
    public void testActionsRouteGetReturnsActionBeansOk() throws Exception {
        
        Action action1 = new MockAction("ACTION_ID_1","action1Name","action1Description");
        Action action2 = new MockAction("ACTION_ID_2","action2Name","action2Description");

        List<Action> actions = List.of(action1,action2);

        Role role1 = new MockRole("myRole1Id","myRole1Name","Description of myRole1Name", List.of(action1.getId(), action2.getId()));
        List<Role> roles = List.of(role1);

        MockRBACService rbacService = new MockRBACService(roles , actions, role1);
        MockTimeService timeService = new MockTimeService(Instant.EPOCH);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/actions/ACTION_ID_1", REQUEST_HEADERS);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ResponseBuilder respBuilder = new ResponseBuilder();
        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();
        ActionDetailsRoute route = new ActionDetailsRoute(respBuilder, rbacService, env, timeService);

        QueryParameters queryParams = new QueryParameters(new HashMap<String,String[]>());
        String pathInfo = "/actions/ACTION_ID_1";

        HttpRequestContext requestContext = new HttpRequestContext(mockRequest, env);
        
        // When...
        route.handleGetRequest(pathInfo, queryParams, requestContext, servletResponse);

        // Then...
        ServletOutputStream outStream = servletResponse.getOutputStream();
        String output = outStream.toString();

        JsonObject expectedJson = new JsonObject();

        expectedJson.addProperty("kind","GalasaAction");
        expectedJson.addProperty("apiVersion","galasa-dev/v1alpha1");

        JsonObject action1MetadataObj = new JsonObject();
        action1MetadataObj.addProperty("url","http://mock.galasa.server/actions/ACTION_ID_1");
        action1MetadataObj.addProperty("name","action1Name");
        action1MetadataObj.addProperty("id","ACTION_ID_1");
        action1MetadataObj.addProperty("description","action1Description");
        expectedJson.add("metadata",action1MetadataObj);


        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(output).isEqualTo(gson.toJson(expectedJson));

    }
}
