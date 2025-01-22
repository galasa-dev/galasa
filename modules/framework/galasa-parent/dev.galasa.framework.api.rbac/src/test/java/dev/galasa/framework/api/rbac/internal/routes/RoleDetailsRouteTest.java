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
import dev.galasa.framework.api.common.InternalServletException;
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
public class RoleDetailsRouteTest {

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

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/roles/myRole1Id", REQUEST_HEADERS);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ResponseBuilder respBuilder = new ResponseBuilder();
        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();

        RoleDetailsRoute route = new RoleDetailsRoute(respBuilder, rbacService, env, timeService);

        QueryParameters queryParams = new QueryParameters(new HashMap<String,String[]>());
        String pathInfo = "/roles/myRole1Id";

        HttpRequestContext requestContext = new HttpRequestContext(mockRequest, env);
        
        // When...
        route.handleGetRequest(pathInfo, queryParams, requestContext, servletResponse);

        // Then...
        ServletOutputStream outStream = servletResponse.getOutputStream();
        String output = outStream.toString();

        
        JsonObject role1Obj = new JsonObject();
        role1Obj.addProperty("kind","GalasaRole");
        role1Obj.addProperty("apiVersion","galasa-dev/v1alpha1");

        JsonObject role1MetadataObj = new JsonObject();
        role1MetadataObj.addProperty("url","http://mock.galasa.server/roles/myRole1Id");
        role1MetadataObj.addProperty("name","myRole1Name");
        role1MetadataObj.addProperty("id","myRole1Id");
        role1MetadataObj.addProperty("description","Description of myRole1Name");
        role1Obj.add("metadata", role1MetadataObj);

        JsonObject role1DataObj = new JsonObject();
        role1Obj.add("data",role1DataObj);
        JsonArray actionsNameListJsonArray = new JsonArray();
        actionsNameListJsonArray.add("action1Id");
        actionsNameListJsonArray.add("action2Id");
        role1DataObj.add("actions",actionsNameListJsonArray);

 

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(output).isEqualTo(gson.toJson(role1Obj));
    }


    @Test
    public void testInvalidIdGetsRejectedOK() {
        Action action1 = new MockAction("action1Id","action1Name","action1Description");
        Action action2 = new MockAction("action2Id","action2Name","action2Description");

        List<Action> actions = List.of(action1,action2);

        Role role1 = new MockRole("myRole1Name","myRole1Id","Description of myRole1Name", List.of(action1.getId(), action2.getId()));
        List<Role> roles = List.of(role1);

        MockRBACService rbacService = new MockRBACService(roles , actions, role1);
        MockTimeService timeService = new MockTimeService(Instant.EPOCH);

        ResponseBuilder respBuilder = new ResponseBuilder();
        MockEnvironment env = new MockEnvironment();

        RoleDetailsRoute route = new RoleDetailsRoute(respBuilder, rbacService, env, timeService);

        InternalServletException ex = catchThrowableOfType( ()-> { route.getRoleIdFromPath("/roles/but/not/valid"); }, InternalServletException.class ); 
        assertThat(ex.getError()).isNotNull();
        assertThat(ex.getError().getMessage()).contains("5121");
    }
}
