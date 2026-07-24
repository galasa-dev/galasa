/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.framework.spi.streams.IStream;

/**
 * Fetches a test catalog from the URL configured on a stream, optionally
 * authenticating with Maven credentials stored in the credentials service.
 *
 * <p>Both {@code StreamTestCatalogRoute} and {@code RunsPortfoliosRoute} require
 * this behaviour; this class is the single implementation shared between them.</p>
 */
public class TestCatalogFetcher implements ITestCatalogFetcher {

    private static final int MAX_CATALOG_SIZE_BYTES = 100 * 1024 * 1024;
    private static final int READ_BUFFER_BYTES = 8 * 1024;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);

    private final Log logger = LogFactory.getLog(getClass());

    private final HttpClient httpClient;
    private final ICredentialsService credentialsService;

    public TestCatalogFetcher(HttpClient httpClient, ICredentialsService credentialsService) {
        this.httpClient = httpClient;
        this.credentialsService = credentialsService;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Writes raw bytes from the remote catalog URL directly to
     * {@code outputStream} in fixed-size chunks, enforcing the maximum size limit
     * before any byte is written past it.</p>
     */
    @Override
    public void streamTestCatalog(IStream stream, OutputStream outputStream) throws InternalServletException {
        URL testCatalogUrl = stream.getTestCatalogUrl();
        if (testCatalogUrl == null) {
            return;
        }

        ICredentials mavenCredentials = resolveMavenCredentials(stream);

        try {
            HttpRequest request = buildHttpRequest(testCatalogUrl, mavenCredentials);
            HttpResponse<InputStream> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofInputStream());

            validateResponseCode(response.statusCode(), testCatalogUrl);
            validateContentType(response.headers().firstValue("Content-Type").orElse(null));

            pipeBody(response.body(), outputStream, stream.getName());

        } catch (InternalServletException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid test catalog URL: " + testCatalogUrl, e);
            ServletError error = new ServletError(GAL5456_ERROR_INVALID_TEST_CATALOG_URL);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST, e);
        } catch (Exception e) {
            logger.error("Failed to fetch test catalog for stream '" + stream.getName() + "'", e);
            ServletError error = new ServletError(GAL5459_ERROR_FAILED_TO_FETCH_TEST_CATALOG);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_GATEWAY, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link #streamTestCatalog} capturing output into a
     * {@link ByteArrayOutputStream}, then returns the result as a UTF-8 string.
     * Returns {@code null} when the stream has no test catalog URL configured.</p>
     */
    @Override
    public String fetchTestCatalog(IStream stream) throws InternalServletException {
        if (stream.getTestCatalogUrl() == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        streamTestCatalog(stream, baos);
        return baos.toString(StandardCharsets.UTF_8);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private ICredentials resolveMavenCredentials(IStream stream) throws InternalServletException {
        String secretName = stream.getMavenSecretName();
        if (secretName == null) {
            return null;
        }
        try {
            ICredentials credentials = credentialsService.getCredentials(secretName);
            if (credentials == null) {
                ServletError error = new ServletError(GAL5093_ERROR_SECRET_NOT_FOUND);
                throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
            }
            return credentials;
        } catch (InternalServletException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to retrieve Maven credentials for secret '" + secretName + "'", e);
            ServletError error = new ServletError(GAL5093_ERROR_SECRET_NOT_FOUND);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST, e);
        }
    }

    private HttpRequest buildHttpRequest(URL url, ICredentials credentials) throws InternalServletException {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(url.toURI())
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .header("Accept", MimeType.APPLICATION_JSON.toString());

            if (credentials instanceof ICredentialsUsernamePassword) {
                ICredentialsUsernamePassword usernamePassword = (ICredentialsUsernamePassword) credentials;
                if (usernamePassword.getUsername() != null && usernamePassword.getPassword() != null) {
                    String auth = usernamePassword.getUsername() + ":" + usernamePassword.getPassword();
                    String encoded = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
                    builder.header("Authorization", "Basic " + encoded);
                } else {
                    logger.trace("Maven credentials present but username/password not populated; skipping Authorization header");
                }
            }

            return builder.build();
        } catch (Exception e) {
            logger.error("Failed to build HTTP request for URL: " + url, e);
            ServletError error = new ServletError(GAL5456_ERROR_INVALID_TEST_CATALOG_URL);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST, e);
        }
    }

    private void validateResponseCode(int statusCode, URL url) throws InternalServletException {
        if (statusCode == HttpServletResponse.SC_MOVED_PERMANENTLY
            || statusCode == HttpServletResponse.SC_FOUND
            || statusCode == HttpServletResponse.SC_TEMPORARY_REDIRECT) {
            ServletError error = new ServletError(GAL5458_ERROR_TEST_CATALOG_REDIRECT_NOT_ALLOWED);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_GATEWAY);
        }
        if (statusCode != HttpServletResponse.SC_OK) {
            ServletError error = new ServletError(GAL5459_ERROR_FAILED_TO_FETCH_TEST_CATALOG);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_GATEWAY);
        }
    }

    private void validateContentType(String contentType) throws InternalServletException {
        if (contentType == null || !contentType.toLowerCase().contains(MimeType.APPLICATION_JSON.toString())) {
            ServletError error = new ServletError(GAL5461_ERROR_TEST_CATALOG_INVALID_CONTENT_TYPE,
                contentType, MimeType.APPLICATION_JSON.toString());
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_GATEWAY);
        }
    }

    /**
     * Pipes bytes from {@code inputStream} to {@code outputStream} in
     * {@value #READ_BUFFER_BYTES}-byte chunks, throwing if the total exceeds
     * {@value #MAX_CATALOG_SIZE_BYTES} bytes.
     */
    private void pipeBody(InputStream inputStream, OutputStream outputStream, String streamName)
            throws InternalServletException {
        try (InputStream is = inputStream) {
            byte[] chunk = new byte[READ_BUFFER_BYTES];
            int bytesRead;
            int total = 0;
            while ((bytesRead = is.read(chunk)) != -1) {
                total += bytesRead;
                if (total > MAX_CATALOG_SIZE_BYTES) {
                    ServletError error = new ServletError(GAL5460_ERROR_TEST_CATALOG_TOO_LARGE);
                    throw new InternalServletException(error, HttpServletResponse.SC_BAD_GATEWAY);
                }
                outputStream.write(chunk, 0, bytesRead);
            }
            outputStream.flush();
        } catch (InternalServletException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error reading test catalog body for stream '" + streamName + "'", e);
            ServletError error = new ServletError(GAL5459_ERROR_FAILED_TO_FETCH_TEST_CATALOG);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_GATEWAY, e);
        }
    }
}
