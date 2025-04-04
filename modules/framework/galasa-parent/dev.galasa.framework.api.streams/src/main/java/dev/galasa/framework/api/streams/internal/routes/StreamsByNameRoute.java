/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.framework.api.streams.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.GAL5420_ERROR_STREAM_NOT_FOUND;

import java.io.IOException;

import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.beans.generated.Stream;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.streams.IStream;
import dev.galasa.framework.spi.streams.IStreamsService;
import dev.galasa.framework.spi.streams.StreamsException;

public class StreamsByNameRoute extends AbstractStreamsRoute {

    // Regex to match endpoint /streams/{streamName}
    protected static final String path = "\\/([a-zA-Z0-9\\-\\_]+)\\/?";
    protected Pattern pathPattern;
    protected String baseServletUrl;

    private StreamsTransform streamsTransform;

    public StreamsByNameRoute(ResponseBuilder responseBuilder, Environment env, IStreamsService streamsService,
            RBACService rbacService)
            throws StreamsException {
        super(responseBuilder, path, rbacService, streamsService);
        this.pathPattern = getPathRegex();
        this.baseServletUrl = env.getenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL);
        this.streamsTransform = new StreamsTransform();
    }

    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,
            HttpRequestContext requestContext, HttpServletResponse response)
            throws ServletException, IOException, FrameworkException {

        logger.info("StreamsByName: handleGetRequest() entered.");
        HttpServletRequest request = requestContext.getRequest();

        String streamName = getStreamName(pathInfo);

        IStream stream = validateAndGetStream(streamName);

        Stream streamsBean = streamsTransform.createStreamBean(stream, baseServletUrl);
        String payloadContent = gson.toJson(streamsBean);

        return getResponseBuilder().buildResponse(request, response, "application/json", payloadContent,
                HttpServletResponse.SC_OK);
    }

    @Override
    public HttpServletResponse handleDeleteRequest(
        String pathInfo,
        HttpRequestContext requestContext,
        HttpServletResponse response
    ) throws FrameworkException {

        logger.info("handleDeleteRequest() entered");
        HttpServletRequest request = requestContext.getRequest();

        String streamName = getStreamName(pathInfo);

        validateAndGetStream(streamName);
        
        streamsService.deleteStream(streamName);

        logger.info("handleDeleteRequest() exiting");
        return getResponseBuilder().buildResponse(request, response, HttpServletResponse.SC_NO_CONTENT);
    }

    private String getStreamName(String urlPath) throws InternalServletException {
        StreamsUrlParameterExtractor parser = new StreamsUrlParameterExtractor(pathPattern);
        return parser.getStreamName(urlPath);
    }

    private IStream validateAndGetStream(String streamName) throws InternalServletException, FrameworkException {
        IStream stream = streamsService.getStreamByName(streamName);
        if (stream == null) {
            ServletError error = new ServletError(GAL5420_ERROR_STREAM_NOT_FOUND);
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
        return stream;
    }

}
