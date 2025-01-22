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

import dev.galasa.framework.api.beans.generated.RBACAction;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.ServletErrorMessage;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.rbac.Action;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.utils.ITimeService;

public class ActionDetailsRoute extends AbstractRBACRoute {

    // Regex to match /rbac/actions/{action-id} and /rbac/actions/{action-id}/ only
    private static final String PATH_PATTERN = "\\/actions\\/([A-Z0-9\\-_]+)\\/?";

    private ActionTransform actionsTransform;

    private Log logger = LogFactory.getLog(getClass());

    public ActionDetailsRoute(
        ResponseBuilder responseBuilder,
        RBACService rbacService,
        Environment env,
        ITimeService timeService) {
        super(responseBuilder, PATH_PATTERN, env, timeService, rbacService);

        actionsTransform = new ActionTransform();
    }

    @Override
    public HttpServletResponse handleGetRequest(
        String pathInfo,
        QueryParameters queryParams,
        HttpRequestContext requestContext,
        HttpServletResponse response
    ) throws FrameworkException {
        logger.info("handleGetRequest() entered. Getting an action.");
        HttpServletRequest request = requestContext.getRequest();

        String actionName = getActionNameFromPath(pathInfo);

        Action action = getRBACService().getActionById(actionName);

        if (action == null) {
            ServletError error = new ServletError(ServletErrorMessage.GAL5122_ACTION_NAMED_NOT_FOUND);
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }

        String url = request.getRequestURL().toString();

        RBACAction actionBean = actionsTransform.createActionBean(action, url);

        logger.info("handleGetRequest() exiting");
        return getResponseBuilder().buildResponse(request, response, "application/json",
            gson.toJson(actionBean), HttpServletResponse.SC_OK);
    }

    private String getActionNameFromPath(String pathInfo) throws InternalServletException {
        Matcher matcher = this.getPathRegex().matcher(pathInfo);
        matcher.matches();
        String actionName;
        try {
            actionName =  matcher.group(1);
        } catch(Exception ex) {
            ServletError error = new ServletError(ServletErrorMessage.GAL5120_INVALID_ACTION_NAME_PROVIDED);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
        return actionName;
    }

}
