/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.bootstrap.routes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.PublicRoute;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.spi.FrameworkException;

public class BootstrapInternalRoute extends PublicRoute {

    private final ArrayList<String> bootstrapKeys = new ArrayList<>(
            Arrays.asList("framework.config.store", "framework.extra.bundles", "framework.testcatalog.url"));

    private Properties configurationProperties = new Properties();

    public BootstrapInternalRoute(ResponseBuilder responseBuilder) {
        super(responseBuilder, "");
        logger.info("Galasa Bootstrap API activated");
    }

    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,
            HttpRequestContext requestContext, HttpServletResponse response)
            throws ServletException, IOException, FrameworkException {

        HttpServletRequest request = requestContext.getRequest();

        Properties actualBootstrap = new Properties();
        synchronized (this.configurationProperties) {
            actualBootstrap.putAll(this.configurationProperties);
        }

        actualBootstrap.store(response.getWriter(), "Galasa Bootstrap Properties");
        response = getResponseBuilder().buildResponse(request, response, "text/plain", HttpServletResponse.SC_OK);
        return response;
    }

    public void onModified(Map<String, Object> properties) {
        synchronized (configurationProperties) {
            for (String key : bootstrapKeys) {
                String value = (String) properties.get(key);
                if (value != null) {
                    this.configurationProperties.put(key, value);
                } else {
                    this.configurationProperties.remove(key);
                }
            }
        }
    }

    public void deactivate() {
        synchronized (configurationProperties) {
            this.configurationProperties.clear();
        }
    }
}
