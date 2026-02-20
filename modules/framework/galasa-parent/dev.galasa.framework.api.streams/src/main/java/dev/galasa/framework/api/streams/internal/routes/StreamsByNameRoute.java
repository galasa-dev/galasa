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
import dev.galasa.framework.api.beans.generated.StreamOBRData;
import dev.galasa.framework.api.beans.generated.StreamUpdateRequest;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.MimeType;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.streams.internal.validators.StreamUpdateRequestValidator;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.rbac.BuiltInAction;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.streams.IOBR;
import dev.galasa.framework.spi.streams.IStream;
import dev.galasa.framework.spi.streams.IStreamsService;
import dev.galasa.framework.spi.streams.OBR;
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

        IStream stream = getStreamByName(streamName);

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
        validateActionPermitted(BuiltInAction.CPS_PROPERTIES_DELETE, requestContext.getUsername());

        HttpServletRequest request = requestContext.getRequest();

        String streamName = getStreamName(pathInfo);

        getStreamByName(streamName);
        
        streamsService.deleteStream(streamName);

        logger.info("handleDeleteRequest() exiting");
        return getResponseBuilder().buildResponse(request, response, HttpServletResponse.SC_NO_CONTENT);
    }

    @Override
    public HttpServletResponse handlePutRequest(
        String pathInfo,
        HttpRequestContext requestContext,
        HttpServletResponse response
    ) throws FrameworkException, IOException {

        logger.info("StreamsByName: handlePutRequest() entered.");
        validateActionPermitted(BuiltInAction.CPS_PROPERTIES_SET, requestContext.getUsername());

        HttpServletRequest request = requestContext.getRequest();
        String streamName = getStreamName(pathInfo);

        // Find out if we are creating or updating a stream
        IStream existingStream = streamsService.getStreamByName(streamName);
        boolean isCreatingNewStream = (existingStream == null);

        StreamUpdateRequest updateRequest = parseRequestBody(request, StreamUpdateRequest.class);
        StreamUpdateRequestValidator validator = new StreamUpdateRequestValidator();
        validator.validate(updateRequest, isCreatingNewStream);

        Stream streamToReturn = new Stream();
        try {
            // Create or update the stream
            dev.galasa.framework.spi.streams.Stream stream;
            if (isCreatingNewStream) {
                stream = new dev.galasa.framework.spi.streams.Stream();
                stream.setName(streamName);
            } else {
                stream = convertToMutableStream(existingStream);
            }
    
            applyUpdatesToStream(stream, updateRequest);
            stream.validate();
            streamsService.setStream(stream);

            streamToReturn = streamsTransform.createStreamBean(stream, baseServletUrl);
        } catch (StreamsException e) {
            ServletError error = new ServletError(GAL5433_FAILED_TO_SET_STREAM);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        String payloadContent = gson.toJson(streamToReturn);
        int statusCode = isCreatingNewStream ? HttpServletResponse.SC_CREATED : HttpServletResponse.SC_OK;

        logger.info("StreamsByName: handlePutRequest() exiting.");
        return getResponseBuilder().buildResponse(request, response, MimeType.APPLICATION_JSON.toString(),
            payloadContent, statusCode);
    }

    private dev.galasa.framework.spi.streams.Stream convertToMutableStream(IStream existingStream)
            throws StreamsException {
        dev.galasa.framework.spi.streams.Stream stream = new dev.galasa.framework.spi.streams.Stream();
        stream.setName(existingStream.getName());
        
        if (existingStream.getDescription() != null) {
            stream.setDescription(existingStream.getDescription());
        }
        
        if (existingStream.getMavenRepositoryUrl() != null) {
            stream.setMavenRepositoryUrl(existingStream.getMavenRepositoryUrl().toString());
        }
        
        if (existingStream.getTestCatalogUrl() != null) {
            stream.setTestCatalogUrl(existingStream.getTestCatalogUrl().toString());
        }
        
        if (existingStream.getObrs() != null) {
            List<IOBR> obrs = new ArrayList<>();
            for (IOBR existingObr : existingStream.getObrs()) {
                OBR obr = new OBR(existingObr.getGroupId(), existingObr.getArtifactId(), existingObr.getVersion());
                obrs.add(obr);
            }
            stream.setObrs(obrs);
        }
        
        return stream;
    }

    private void applyUpdatesToStream(dev.galasa.framework.spi.streams.Stream stream, StreamUpdateRequest updateRequest)
            throws StreamsException {
        // Update description if provided
        if (updateRequest.getdescription() != null) {
            stream.setDescription(updateRequest.getdescription());
        }
        
        // Update repository URL if provided
        if (updateRequest.getrepository() != null &&
            updateRequest.getrepository().geturl() != null) {
            stream.setMavenRepositoryUrl(updateRequest.getrepository().geturl());
        }
        
        // Update test catalog URL if provided
        if (updateRequest.getTestCatalog() != null &&
            updateRequest.getTestCatalog().geturl() != null) {
            stream.setTestCatalogUrl(updateRequest.getTestCatalog().geturl());
        }
        
        // Update OBRs if provided
        if (updateRequest.getobrs() != null && updateRequest.getobrs().length > 0) {
            List<IOBR> obrs = new ArrayList<>();
            for (StreamOBRData obrData : updateRequest.getobrs()) {
                OBR obr = new OBR(obrData.getGroupId(), obrData.getArtifactId(), obrData.getversion());
                obrs.add(obr);
            }
            stream.setObrs(obrs);
        }
    }

    private String getStreamName(String urlPath) throws InternalServletException {
        StreamsUrlParameterExtractor parser = new StreamsUrlParameterExtractor(pathPattern);
        return parser.getStreamName(urlPath);
    }

    private IStream getStreamByName(String streamName) throws InternalServletException, FrameworkException {
        IStream stream = streamsService.getStreamByName(streamName);
        if (stream == null) {
            ServletError error = new ServletError(GAL5420_ERROR_STREAM_NOT_FOUND);
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
        return stream;
    }

}
