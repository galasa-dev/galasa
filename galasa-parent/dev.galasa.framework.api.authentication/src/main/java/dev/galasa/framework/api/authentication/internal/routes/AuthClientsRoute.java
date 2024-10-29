/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal.routes;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.coreos.dex.api.DexOuterClass.Client;

import dev.galasa.framework.api.authentication.internal.DexClient;
import dev.galasa.framework.api.authentication.internal.DexGrpcClient;
import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.FrameworkException;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

public class AuthClientsRoute extends BaseRoute {

    private DexGrpcClient dexGrpcClient;

    private static final String PATH_PATTERN = "\\/clients\\/?";

    public AuthClientsRoute(ResponseBuilder responseBuilder, DexGrpcClient dexGrpcClient) {
        super(responseBuilder, PATH_PATTERN);
        this.dexGrpcClient = dexGrpcClient;
    }

    /**
     * Sending a POST request to /auth/clients creates a new Dex client and returns
     * the details of this new client.
     */
    @Override
    public HttpServletResponse handlePostRequest(String pathInfo, QueryParameters queryParameters,
            HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, FrameworkException {

        logger.info("handlePostRequest() entered");

        Client newDexClient = dexGrpcClient.createClient(AuthCallbackRoute.getExternalAuthCallbackUrl());
        if (newDexClient == null) {
            ServletError error = new ServletError(GAL5000_GENERIC_API_ERROR);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        // Marshal into a structure to be returned as JSON
        DexClient clientToReturn = new DexClient(newDexClient.getId());
        return getResponseBuilder().buildResponse(request, response, "application/json", gson.toJson(clientToReturn),
                HttpServletResponse.SC_CREATED);
    }
}
