/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.rbac.internal.routes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.api.beans.generated.RBACRole;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.SupportedQueryParameterNames;
import dev.galasa.framework.api.rbac.RoleTransform;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.rbac.Role;
import dev.galasa.framework.spi.utils.ITimeService;

public class RolesRoute extends AbstractRBACRoute {

    // Regex to match /rbac/roles/ or /rbac/roles/
    private static final String PATH_PATTERN = "\\/roles\\/?";

    private RoleTransform roleTransform = new RoleTransform();

    private Log logger = LogFactory.getLog(getClass());

    public RolesRoute(
        ResponseBuilder responseBuilder,
        RBACService rbacService,
        Environment env,
        ITimeService timeService
    ) {
        super(responseBuilder, PATH_PATTERN, env, timeService, rbacService);
    }

    public static final String QUERY_PARAMETER_NAME_NAME = "name";
    public static final SupportedQueryParameterNames SUPPORTED_QUERY_PARAMETER_NAMES = new SupportedQueryParameterNames(
        QUERY_PARAMETER_NAME_NAME
    );

    @Override
    public SupportedQueryParameterNames getSupportedQueryParameterNames() {
        return SUPPORTED_QUERY_PARAMETER_NAMES;
    }

    @Override
    public HttpServletResponse handleGetRequest(
        String pathInfo,
        QueryParameters queryParams,
        HttpRequestContext requestContext,
        HttpServletResponse response
    ) throws FrameworkException {

        logger.info("handleGetRequest() entered. Getting roles");
        HttpServletRequest request = requestContext.getRequest();

        Collection<Role> roles ;

        String roleNameWanted = queryParams.getSingleString(QUERY_PARAMETER_NAME_NAME, null);

        if (roleNameWanted==null || roleNameWanted.trim().equals("")) {
            // The caller wants all the roles.
            roles = getRBACService().getRolesSortedByName();
        } else {
            // The caller wants a specific role.
            Role role = getRBACService().getRoleByName(roleNameWanted);
            roles = new ArrayList<Role>();
            if (role!=null) {
                roles.add(role);
            }
        }

        String baseUrl = request.getRequestURL().toString();

        List<RBACRole> roleBeans = roleTransform.createRolesBeans(roles, baseUrl);
    
        HttpServletResponse httpResponse = getResponseBuilder().buildResponse(request, response, "application/json",
            gson.toJson(roleBeans), HttpServletResponse.SC_OK);

        logger.info("handleGetRequest() exiting");
        return httpResponse;
    }

}
