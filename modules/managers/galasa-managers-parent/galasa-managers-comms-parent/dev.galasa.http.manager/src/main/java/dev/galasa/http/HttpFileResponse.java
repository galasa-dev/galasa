/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;

/**
 * Wrapper for HTTP file download responses. This class encapsulates the response
 * from file download operations, providing access to the response stream, headers,
 * and status information while hiding the underlying HTTP client implementation.
 * 
 * <p>This class implements AutoCloseable and should be used in try-with-resources
 * blocks to ensure proper resource cleanup:</p>
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
public class HttpFileResponse implements AutoCloseable {

    private final ClassicHttpResponse httpResponse;
    private final int statusCode;
    private final String reasonPhrase;
    private final Map<String, String> headers;
    private final HttpEntity entity;

    /**
     * Public constructor for creating HttpFileResponse from ClassicHttpResponse.
     * Typically used internally by IHttpClient implementations.
     *
     * @param httpResponse the underlying HTTP response
     * @throws HttpClientException if the response cannot be processed
     */
    public HttpFileResponse(ClassicHttpResponse httpResponse) throws HttpClientException {
        this.httpResponse = httpResponse;
        this.statusCode = httpResponse.getCode();
        this.reasonPhrase = httpResponse.getReasonPhrase();
        this.entity = httpResponse.getEntity();
        this.headers = new HashMap<>();
        
        // Extract headers
        for (Header header : httpResponse.getHeaders()) {
            headers.put(header.getName(), header.getValue());
        }
    }

    /**
     * Get the response body as an InputStream. The caller is responsible for
     * closing this stream, or use try-with-resources on the HttpFileResponse
     * which will close it automatically.
     * 
     * @return the response content stream, or null if there is no content
     * @throws HttpClientException if the stream cannot be obtained
     */
    public InputStream getContent() throws HttpClientException {
        InputStream contentStream = null;
        if (entity != null) {
            try {
                contentStream = entity.getContent();
            } catch (IOException e) {
                throw new HttpClientException("Failed to get response content stream", e);
            }
        }
        return contentStream;
    }

    /**
     * Get the HTTP status code of the response.
     * 
     * @return the status code (e.g., 200, 404, 500)
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Get the HTTP status message (reason phrase) of the response.
     * 
     * @return the status message (e.g., "OK", "Not Found", "Internal Server Error")
     */
    public String getReasonPhrase() {
        return reasonPhrase;
    }

    /**
     * Get the value of a specific response header.
     * 
     * @param name the header name (case-insensitive)
     * @return the header value, or null if the header is not present
     */
    public String getHeader(String name) {
        return headers.get(name);
    }

    /**
     * Get all response headers as a map.
     * 
     * @return a map of header names to values
     */
    public Map<String, String> getHeaders() {
        return new HashMap<>(headers);
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
            if (httpResponse != null) {
                httpResponse.close();
            }
        } catch (IOException e) {
            throw new HttpClientException("Failed to close HTTP response", e);
        }
    }
}
