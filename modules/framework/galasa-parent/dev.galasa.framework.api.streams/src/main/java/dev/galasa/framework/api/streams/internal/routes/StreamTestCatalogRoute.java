/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.framework.api.streams.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.MimeType;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
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

    // 100 MB max response size
    private static final int MAX_RESPONSE_SIZE = 100 * 1024 * 1024;
    private static final int READ_INPUT_BUFFER_BYTES = 8 * 1024;

    private static final Duration READ_TIMEOUT_SECONDS = Duration.ofSeconds(60);

    private ICredentialsService credentialsService;
    private StreamValidator streamValidator = new StreamValidator();
    private HttpClient httpClient;

    public StreamTestCatalogRoute(
        ResponseBuilder responseBuilder,
        IStreamsService streamsService,
        ICredentialsService credentialsService,
        RBACService rbacService,
        HttpClient httpClient
    ) throws StreamsException {
        super(responseBuilder, path, rbacService, streamsService);
        this.credentialsService = credentialsService;
        this.httpClient = httpClient;
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

        String streamMavenSecret = stream.getMavenSecretName();
        ICredentials mavenCredentials = null;
        if (streamMavenSecret != null) {
            mavenCredentials = credentialsService.getCredentials(streamMavenSecret);

            if (mavenCredentials == null) {
                ServletError error = new ServletError(GAL5093_ERROR_SECRET_NOT_FOUND);
                throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MimeType.APPLICATION_JSON.toString());
        streamTestCatalogToResponse(testCatalogUrl, mavenCredentials, response);

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

    private void streamTestCatalogToResponse(
        URL testCatalogUrl,
        ICredentials mavenCredentials,
        HttpServletResponse servletResponse
    ) throws InternalServletException {
        try {
            HttpRequest request = buildHttpRequest(testCatalogUrl, mavenCredentials);

            HttpResponse<InputStream> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofInputStream());

            validateResponseCode(response.statusCode(), testCatalogUrl);
            validateContentType(response.headers().firstValue("Content-Type").orElse(null));

            try (OutputStream outputStream = servletResponse.getOutputStream();
                InputStream inputStream = response.body()) {
                streamResponseBodyToOutput(inputStream, servletResponse.getOutputStream());
            }
        } catch (IllegalArgumentException e) {
            logger.error("Invalid test catalog URL provided", e);
            ServletError error = new ServletError(GAL5456_ERROR_INVALID_TEST_CATALOG_URL);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to fetch test catalog", e);
            ServletError error = new ServletError(GAL5459_ERROR_FAILED_TO_FETCH_TEST_CATALOG);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    private void streamResponseBodyToOutput(InputStream inputStream, OutputStream outputStream) throws IOException, InternalServletException {
        byte[] chunk = new byte[READ_INPUT_BUFFER_BYTES];
        int bytesRead;
        int totalBytesRead = 0;

        while ((bytesRead = inputStream.read(chunk)) != -1) {
            totalBytesRead += bytesRead;
            if (totalBytesRead > MAX_RESPONSE_SIZE) {
                ServletError error = new ServletError(GAL5460_ERROR_TEST_CATALOG_TOO_LARGE);
                throw new InternalServletException(error, HttpServletResponse.SC_BAD_GATEWAY);
            }
            outputStream.write(chunk, 0, bytesRead);
        }
        outputStream.flush();
        outputStream.close();
    }

    private HttpRequest buildHttpRequest(URL testCatalogUrl, ICredentials mavenCredentials) throws InternalServletException {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(testCatalogUrl.toURI())
                .timeout(READ_TIMEOUT_SECONDS)
                .GET()
                .header("Accept", MimeType.APPLICATION_JSON.toString());

            // Set the Maven credentials if available
            if (mavenCredentials != null && mavenCredentials instanceof ICredentialsUsernamePassword) {
                ICredentialsUsernamePassword credentials = (ICredentialsUsernamePassword) mavenCredentials;

                if (credentials.getUsername() != null && credentials.getPassword() != null) {
                    String auth = credentials.getUsername() + ":" + credentials.getPassword();
                    String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
                    requestBuilder.header("Authorization", "Basic " + encodedAuth);
                } else {
                    logger.trace("Maven credentials were not populated, will not set authorization header");
                }
            }

            return requestBuilder.build();
        } catch (Exception e) {
            logger.error("Failed to build HTTP request: " + e.getMessage());
            ServletError error = new ServletError(GAL5456_ERROR_INVALID_TEST_CATALOG_URL);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void validateResponseCode(int responseCode, URL url) throws InternalServletException {
        if (responseCode == HttpServletResponse.SC_MOVED_PERMANENTLY
            || responseCode == HttpServletResponse.SC_TEMPORARY_REDIRECT
            || responseCode == HttpServletResponse.SC_FOUND) {
            ServletError error = new ServletError(GAL5458_ERROR_TEST_CATALOG_REDIRECT_NOT_ALLOWED);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_GATEWAY);
        }

        if (responseCode != HttpServletResponse.SC_OK) {
            ServletError error = new ServletError(GAL5459_ERROR_FAILED_TO_FETCH_TEST_CATALOG);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_GATEWAY);
        }
    }

    private void validateContentType(String contentType) throws InternalServletException {
        if (contentType == null || contentType != null && !contentType.toLowerCase().equals(MimeType.APPLICATION_JSON.toString())) {
            ServletError error = new ServletError(GAL5461_ERROR_TEST_CATALOG_INVALID_CONTENT_TYPE, contentType, MimeType.APPLICATION_JSON.toString());
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_GATEWAY);
        }
    }
}
