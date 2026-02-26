/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.PublicRoute;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;

/**
 * Route to check the health status of the Result Archive Store (RAS).
 * This endpoint is publicly accessible (no authentication required).
 */
public class RasHealthRoute extends PublicRoute {

    protected static final String path = "\\/health\\/?";

    private IFramework framework;

    public RasHealthRoute(ResponseBuilder responseBuilder, IFramework framework) {
        /*
         * Regex to match endpoints:
         * -> /ras/health
         * -> /ras/health/
         */
        super(responseBuilder, path);
        this.framework = framework;
    }

    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,
            HttpRequestContext requestContext, HttpServletResponse response)
            throws ServletException, IOException, FrameworkException {

        HttpServletRequest request = requestContext.getRequest();

        boolean isHealthy = checkRasHealth();

        if (isHealthy) {
            JsonObject healthResponse = new JsonObject();
            healthResponse.addProperty("status", "ok");
            String outputString = healthResponse.toString();
            return getResponseBuilder().buildResponse(request, response, "application/json",
                    outputString, HttpServletResponse.SC_OK);
        } else {
            ServletError error = new ServletError(GAL5449_RAS_NOT_AVAILABLE);
            throw new InternalServletException(error, HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }

    /**
     * Checks if the RAS is healthy by querying all directory services.
     * 
     * @return true if at least one RAS directory service is healthy, false
     *         otherwise
     */
    private boolean checkRasHealth() {
        try {
            for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore()
                    .getDirectoryServices()) {
                if (directoryService.isHealthy()) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
