/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.rbac.BuiltInAction;

/**
 * IRoute provides methods for endpoints to implement when a request is sent through a servlet,
 * allowing for new routes to be added without needing servlets to know which route handles the request.
 *
 * Route paths represent the regex patterns that are used to match request paths against.
 */
public interface IRoute {
    Pattern getPathRegex();

    HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams, HttpRequestContext requestContext, HttpServletResponse response)
        throws ServletException, IOException, FrameworkException;

    HttpServletResponse handlePutRequest(String pathInfo, HttpRequestContext requestContext, HttpServletResponse response)
        throws ServletException, IOException, FrameworkException;

    HttpServletResponse handlePostRequest(String pathInfo, HttpRequestContext requestContext, HttpServletResponse response)
        throws ServletException, IOException, FrameworkException;

    HttpServletResponse handleDeleteRequest(String pathInfo, HttpRequestContext requestContext, HttpServletResponse response)
        throws ServletException, IOException, FrameworkException;

    /**
     * @return A set of query parameter names which are supported by this route. Any extra parameters will be reported as an error.
     */
    SupportedQueryParameterNames getSupportedQueryParameterNames();

    /**
     * Checks if the given action is permitted for the user that sent the given request
     * 
     * @param action the action being performed
     * @param loginId the login ID of the user sending a request to this route
     * @return true if the user is allowed to perform the given action, false otherwise
     * @throws InternalServletException if there was an issue accessing the RBAC service
     */
    boolean isActionPermitted(BuiltInAction action, String loginId) throws InternalServletException;
}
