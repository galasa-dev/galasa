/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.framework.api.streams.internal.routes;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.streams.internal.common.StreamsJsonTransformer;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.streams.IStream;
import dev.galasa.framework.spi.streams.IStreamsService;
import dev.galasa.framework.spi.streams.StreamsException;

public class StreamsRoute extends AbstractStreamsRoute {

    // Regex to match endpoint /streams and /streams/
    private static final String path = "\\/?";
    protected Pattern pathPattern;
    protected String baseServletUrl;

    public StreamsRoute(ResponseBuilder responseBuilder, Environment env,
            IStreamsService streamsService,RBACService rbacService)
            throws StreamsException {
        super(responseBuilder, path, rbacService, streamsService);
        this.pathPattern = getPathRegex();
        baseServletUrl = env.getenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL);
    }

    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,
            HttpRequestContext requestContext, HttpServletResponse response)
            throws ServletException, IOException, FrameworkException {

        logger.info("StreamsRoute: handleGetRequest() entered.");
        HttpServletRequest request = requestContext.getRequest();
        StreamsJsonTransformer jsonTransformer = new StreamsJsonTransformer();

        List<IStream> streams = streamsService.getStreams();
        String streamsJson = jsonTransformer.getStreamsAsJsonString(streams, baseServletUrl);

        return getResponseBuilder().buildResponse(
                request, response, "application/json", streamsJson,
                HttpServletResponse.SC_OK);
    }


}
