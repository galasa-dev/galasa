/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.bootstrap.routes;

import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.PublicRoute;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.QueryParameters;

import dev.galasa.framework.spi.FrameworkException;

import java.io.IOException;

public class BootstrapExternalRoute extends PublicRoute {

    protected static final String path = "\\/external";

    public BootstrapExternalRoute(ResponseBuilder responseBuilder) {
        super(responseBuilder, path);
    }

    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,
            HttpRequestContext requestContext, HttpServletResponse response)
            throws ServletException, IOException, FrameworkException {

        HttpServletRequest request = requestContext.getRequest();

        Properties properties = new Properties();
        properties.store(response.getWriter(), "Galasa Bootstrap Properties");
        response = getResponseBuilder().buildResponse(request, response, "text/plain", HttpServletResponse.SC_OK);
        return response;
    }

}