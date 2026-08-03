/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.framework.api.streams.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.http.HttpClient;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.ITestCatalogFetcher;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.MimeType;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.TestCatalogFetcher;
import dev.galasa.framework.api.common.resources.StreamValidator;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.streams.IStream;
import dev.galasa.framework.spi.streams.IStreamsService;
import dev.galasa.framework.spi.streams.StreamsException;

public class StreamTestCatalogRoute extends AbstractStreamsRoute {

    // Regex to match endpoint /streams/{streamName}/testcatalog
    protected static final String path = "\\/([a-zA-Z0-9\\-\\_]+)\\/testcatalog\\/?";

    private final StreamValidator streamValidator = new StreamValidator();
    private final ITestCatalogFetcher catalogFetcher;

    public StreamTestCatalogRoute(
        ResponseBuilder responseBuilder,
        IStreamsService streamsService,
        ICredentialsService credentialsService,
        RBACService rbacService,
        HttpClient httpClient
    ) throws StreamsException {
        super(responseBuilder, path, rbacService, streamsService);
        this.catalogFetcher = new TestCatalogFetcher(httpClient, credentialsService);
    }

    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,
            HttpRequestContext requestContext, HttpServletResponse response)
            throws ServletException, IOException, FrameworkException {

        logger.info("StreamTestCatalog: handleGetRequest() entered.");

        String streamName = getStreamName(pathInfo);
        IStream stream = getStreamByName(streamName);

        URL testCatalogUrl = stream.getTestCatalogUrl();
        if (testCatalogUrl == null) {
            ServletError error = new ServletError(GAL5455_ERROR_STREAM_NO_TEST_CATALOG);
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }

        streamValidator.validateTestCatalogUrl(testCatalogUrl.toString(), true);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MimeType.APPLICATION_JSON.toString());
        try (OutputStream out = response.getOutputStream()) {
            catalogFetcher.streamTestCatalog(stream, out);
        }

        logger.info("StreamTestCatalog: handleGetRequest() exiting.");
        return response;
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
