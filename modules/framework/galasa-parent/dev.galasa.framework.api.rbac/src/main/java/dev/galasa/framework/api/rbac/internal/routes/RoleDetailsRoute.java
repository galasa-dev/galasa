/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.rbac.internal.routes;

import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.api.beans.generated.RBACRole;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.ServletErrorMessage;
import dev.galasa.framework.api.rbac.RoleTransform;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.rbac.Role;
import dev.galasa.framework.spi.utils.ITimeService;

public class RoleDetailsRoute extends AbstractRBACRoute {

    // Regex to match /rbac/roles/{role-id}
    // where {role-id} can consist of the following characters:
    // - Alphanumeric characters (a-zA-Z0-9)
    // - Underscores (_)
    // - Dashes (-)
    private static final String PATH_PATTERN = "\\/roles\\/([a-zA-Z0-9\\-_]+)\\/?";

    private RoleTransform roleTransform = new RoleTransform();

    private Log logger = LogFactory.getLog(getClass());

    public RoleDetailsRoute(
        ResponseBuilder responseBuilder,
        RBACService rbacService,
        Environment env,
        ITimeService timeService
    ) {
        super(responseBuilder, PATH_PATTERN, env, timeService, rbacService);
    }

    @Override
    public HttpServletResponse handleGetRequest(
        String pathInfo,
        QueryParameters queryParams,
        HttpRequestContext requestContext,
        HttpServletResponse response
    ) throws FrameworkException {
        logger.info("handleGetRequest() entered. Getting a role");

        HttpServletRequest request = requestContext.getRequest();

        String roleId = getRoleIdFromPath(pathInfo);
        
        Role role = getRBACService().getRoleById(roleId);

        if (role == null) {
            ServletError error = new ServletError(ServletErrorMessage.GAL5123_ROLE_ID_NOT_FOUND);
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }

        String url = request.getRequestURL().toString();

        RBACRole roleBean = roleTransform.createRoleBean(role, url);        

        logger.info("handleGetRequest() exiting");
        return getResponseBuilder().buildResponse(request, response, "application/json",
            gson.toJson(roleBean), HttpServletResponse.SC_OK);
    }


    protected String getRoleIdFromPath(String pathInfo) throws InternalServletException {
        Matcher matcher = this.getPathRegex().matcher(pathInfo);
        matcher.matches();
        String roleId;
        try{
            roleId =  matcher.group(1);
        } catch( Exception ex) {
            ServletError error = new ServletError(ServletErrorMessage.GAL5121_INVALID_ROLE_ID_PROVIDED);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
        return roleId;
    }
}
