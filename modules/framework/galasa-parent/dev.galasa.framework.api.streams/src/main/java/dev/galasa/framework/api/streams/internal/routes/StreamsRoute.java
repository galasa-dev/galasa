/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.framework.api.streams.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.beans.generated.Stream;
import dev.galasa.framework.api.beans.generated.StreamCreateRequest;
import dev.galasa.framework.api.beans.generated.StreamOBRData;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.MimeType;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.streams.internal.common.StreamsJsonTransformer;
import dev.galasa.framework.api.streams.internal.validators.StreamCreateRequestValidator;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.rbac.BuiltInAction;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.streams.IOBR;
import dev.galasa.framework.spi.streams.IStream;
import dev.galasa.framework.spi.streams.IStreamsService;
import dev.galasa.framework.spi.streams.OBR;
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
                request, response, MimeType.APPLICATION_JSON.toString(), streamsJson,
                HttpServletResponse.SC_OK);
    }

    @Override
    public HttpServletResponse handlePostRequest(
        String pathInfo,
        HttpRequestContext requestContext,
        HttpServletResponse response
    ) throws FrameworkException, IOException {

        logger.info("StreamsRoute: handlePostRequest() entered.");
        validateActionPermitted(BuiltInAction.CPS_PROPERTIES_SET, requestContext.getUsername());

        HttpServletRequest request = requestContext.getRequest();
        StreamCreateRequest createRequest = parseRequestBody(request, StreamCreateRequest.class);

        StreamCreateRequestValidator validator = new StreamCreateRequestValidator();
        validator.validate(createRequest);

        String streamName = createRequest.getname();

        // Check if stream already exists
        IStream existingStream = streamsService.getStreamByName(streamName);
        if (existingStream != null) {
            ServletError error = new ServletError(GAL5429_ERROR_STREAM_ALREADY_EXISTS);
            throw new InternalServletException(error, HttpServletResponse.SC_CONFLICT);
        }

        Stream streamToReturn = new Stream();
        try {
            dev.galasa.framework.spi.streams.Stream stream = createStreamFromRequest(createRequest);
            stream.validate();
            streamsService.setStream(stream);

            StreamsTransform streamsTransform = new StreamsTransform();
            streamToReturn = streamsTransform.createStreamBean(stream, baseServletUrl);
        } catch (StreamsException e) {
            ServletError error = new ServletError(GAL5433_FAILED_TO_SET_STREAM);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        // Return the created stream
        String payloadContent = gson.toJson(streamToReturn);

        logger.info("StreamsRoute: handlePostRequest() exiting.");
        return getResponseBuilder().buildResponse(request, response, MimeType.APPLICATION_JSON.toString(),
            payloadContent, HttpServletResponse.SC_CREATED);
    }

    private dev.galasa.framework.spi.streams.Stream createStreamFromRequest(StreamCreateRequest createRequest)
            throws StreamsException {
        dev.galasa.framework.spi.streams.Stream stream = new dev.galasa.framework.spi.streams.Stream();
        stream.setName(createRequest.getname());

        if (createRequest.getdescription() != null) {
            stream.setDescription(createRequest.getdescription());
        }

        stream.setMavenRepositoryUrl(createRequest.getrepository().geturl());
        stream.setTestCatalogUrl(createRequest.getTestCatalog().geturl());

        List<IOBR> obrs = new ArrayList<>();
        for (StreamOBRData obrData : createRequest.getobrs()) {
            OBR obr = new OBR(obrData.getGroupId(), obrData.getArtifactId(), obrData.getversion());
            obrs.add(obr);
        }
        stream.setObrs(obrs);

        return stream;
    }
}
