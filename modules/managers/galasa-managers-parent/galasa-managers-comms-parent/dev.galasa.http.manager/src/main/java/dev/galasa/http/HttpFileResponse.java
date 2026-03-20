/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.http;

import java.io.IOException;
import java.io.InputStream;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;

/**
 * Wrapper for HTTP file download responses. This class encapsulates the response
 * from file download operations, providing access to the response stream, headers,
 * and status information while hiding the underlying HTTP client implementation.
 *
 * <p>This class extends {@link HttpClientResponse} with {@link InputStream} content
 * and implements AutoCloseable to ensure proper resource cleanup. It should be used
 * in try-with-resources blocks:</p>
 *
 * <pre>
 * try (HttpFileResponse response = client.getFileStream("/path/to/file")) {
 *     InputStream stream = response.getContent();
 *     // Process the stream
 * }
 * </pre>
 *
 * @since 0.47.0
 */
public class HttpFileResponse extends HttpClientResponse<InputStream> implements AutoCloseable {

    private ClassicHttpResponse underlyingResponse;
    private HttpEntity entity;

    /**
     * Public constructor for creating HttpFileResponse from ClassicHttpResponse.
     * Typically used internally by IHttpClient implementations.
     *
     * @param httpResponse the underlying HTTP response
     * @throws HttpClientException if the response cannot be processed
     */
    public HttpFileResponse(ClassicHttpResponse httpResponse) throws HttpClientException {        
        this.underlyingResponse = httpResponse;
        this.entity = httpResponse.getEntity();
        populateGenericValues(httpResponse);

        // Set the InputStream content from the entity
        if (entity != null) {
            try {
                InputStream stream = entity.getContent();
                setContent(stream);
            } catch (IOException e) {
                throw new HttpClientException("Failed to get response content stream", e);
            }
        }
    }

    /**
     * Get the value of a specific response header.
     *
     * @param name the header name (case-insensitive)
     * @return the header value, or null if the header is not present
     */
    public String getHeader(String name) {
        return (String) super.getHeader(name);
    }

    /**
     * Get the content type of the response.
     *
     * @return the ContentType, or null if not specified
     */
    public ContentType getContentType() {
        ContentType contentType = null;

        String contentTypeHeader = getHeader("Content-Type");
        if (contentTypeHeader != null) {
            // Extract just the MIME type (before any semicolon)
            String mimeType = contentTypeHeader.split(";")[0].trim();

            try {
                contentType = ContentType.fromMimeTypeString(mimeType);
            } catch (IllegalArgumentException e) {
                // Content type not in our enum, return null
            }
        }
        return contentType;
    }

    /**
     * Get the content length of the response, if known.
     *
     * @return the content length in bytes, or -1 if unknown
     */
    public long getContentLength() {
        long contentLength = -1;
        if (entity != null) {
            contentLength = entity.getContentLength();
        }
        return contentLength;
    }

    /**
     * Check if the response was successful (status code 2xx or 3xx).
     *
     * @return true if the status code is in the 200-399 range
     */
    public boolean isSuccessful() {
        int statusCode = getStatusCode();
        return statusCode >= 200 && statusCode < 400;
    }

    /**
     * Close the underlying HTTP response and release any system resources.
     * This method is idempotent and can be called multiple times safely.
     *
     * @throws HttpClientException if an error occurs while closing
     */
    @Override
    public void close() throws HttpClientException {
        try {
            if (underlyingResponse != null) {
                underlyingResponse.close();
            }
        } catch (IOException e) {
            throw new HttpClientException("Failed to close HTTP response", e);
        }
    }
}
