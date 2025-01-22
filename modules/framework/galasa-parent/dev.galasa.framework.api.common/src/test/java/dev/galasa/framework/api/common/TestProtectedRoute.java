/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import org.junit.Test;

import dev.galasa.framework.api.common.mocks.FilledMockEnvironment;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.mocks.FilledMockRBACService;
import dev.galasa.framework.mocks.MockRBACService;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.rbac.Action;
import dev.galasa.framework.spi.rbac.BuiltInAction;
import dev.galasa.framework.spi.rbac.RBACService;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestProtectedRoute extends BaseServletTest {

    private Map<String, String> REQUEST_HEADERS = new HashMap<>(Map.of("Authorization", "Bearer " + DUMMY_JWT));

    public class MockProtectedRoute extends ProtectedRoute {

        public MockProtectedRoute(RBACService rbacService) {
            super(new ResponseBuilder(), "/", rbacService, FilledMockEnvironment.createTestEnvironment());
        }

        @Override
        public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams, 
                HttpServletRequest request, HttpServletResponse response) 
                throws ServletException, IOException, FrameworkException {
            response.setStatus(HttpServletResponse.SC_OK);
            return response;
        }
    }

    @Test
    public void testValidateActionPermittedWithMissingPermissionsThrowsError() throws Exception {
        // Given...
        List<Action> userActions = new ArrayList<>();

        MockRBACService rbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME, userActions);
        ProtectedRoute route = new MockProtectedRoute(rbacService);
        MockHttpServletRequest request = new MockHttpServletRequest("", REQUEST_HEADERS);

        // When...
        InternalServletException thrown = catchThrowableOfType(() -> {
            route.validateActionPermitted(BuiltInAction.CPS_PROPERTIES_SET, request);
        }, InternalServletException.class);


        // Then...
        assertThat(thrown).isNotNull();

        String actionId = BuiltInAction.CPS_PROPERTIES_SET.getAction().getId();
        checkErrorStructure(thrown.getMessage(), 5125, "GAL5125E", actionId);
    }

    @Test
    public void testValidateActionPermittedWithCorrectPermissionsSucceeds() throws Exception {
        // Given...
        List<Action> userActions = List.of(BuiltInAction.SECRETS_GET_UNREDACTED_VALUES.getAction());

        MockRBACService rbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME, userActions);
        ProtectedRoute route = new MockProtectedRoute(rbacService);
        MockHttpServletRequest request = new MockHttpServletRequest("", REQUEST_HEADERS);

        // When...
        InternalServletException thrown = catchThrowableOfType(() -> {
            route.validateActionPermitted(BuiltInAction.SECRETS_GET_UNREDACTED_VALUES, request);
        }, InternalServletException.class);


        // Then...
        // No exceptions should have been thrown
        assertThat(thrown).isNull();
    }
}
