/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

/**
 * Unit tests for HttpFileResponse class
 */
public class HttpFileResponseTest {

    @Test
    public void testConstructorWithValidResponse() throws Exception {
        // Given
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse(200, "OK");
        mockResponse.addHeader("Content-Type", "application/json");
        mockResponse.addHeader("Content-Length", "1024");
        
        // When
        try (HttpFileResponse response = new HttpFileResponse(mockResponse)) {
            // Then
            assertThat(response.getStatusCode()).as("Status code should be 200").isEqualTo(200);
            assertThat(response.getStatusMessage()).as("Reason phrase should be OK").isEqualTo("OK");
            assertThat(response.getHeader("Content-Type")).as("Content-Type header should be present").isEqualTo("application/json");
            assertThat(response.getHeader("Content-Length")).as("Content-Length header should be present").isEqualTo("1024");
        }
    }

    @Test
    public void testGetContentWithEntity() throws Exception {
        // Given
        String testContent = "test file content";
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse(200, "OK");
        mockResponse.setEntity(new MockHttpEntity(testContent.getBytes()));
        
        // When
        try (HttpFileResponse response = new HttpFileResponse(mockResponse)) {
            InputStream content = response.getContent();
            
            // Then
            assertThat(content).as("Content stream should not be null").isNotNull();
            byte[] buffer = new byte[testContent.length()];
            int bytesRead = content.read(buffer);
            assertThat(bytesRead).as("Should read all bytes").isEqualTo(testContent.length());
            assertThat(new String(buffer)).as("Content should match").isEqualTo(testContent);
        }
    }

    @Test
    public void testGetContentWithNullEntity() throws Exception {
        // Given
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse(204, "No Content");
        mockResponse.setEntity(null);
        
        // When
        try (HttpFileResponse response = new HttpFileResponse(mockResponse)) {
            InputStream content = response.getContent();
            
            // Then
            assertThat(content).as("Content should be null when entity is null").isNull();
        }
    }

    @Test
    public void testGetContentThrowsExceptionWhenEntityFails() throws Exception {
        // Given
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse(200, "OK");
        mockResponse.setEntity(new MockHttpEntity(new IOException("Stream error")));
        
        // When/Then
        assertThatThrownBy(() -> new HttpFileResponse(mockResponse))
            .isInstanceOf(HttpClientException.class)
            .hasMessageContaining("Failed to get response content stream")
            .hasCauseInstanceOf(IOException.class);
    }

    @Test
    public void testGetStatusCode() throws Exception {
        // Given
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse(404, "Not Found");
        
        // When
        try (HttpFileResponse response = new HttpFileResponse(mockResponse)) {
            // Then
            assertThat(response.getStatusCode()).as("Status code should be 404").isEqualTo(404);
        }
    }

    @Test
    public void testGetReasonPhrase() throws Exception {
        // Given
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse(500, "Internal Server Error");
        
        // When
        try (HttpFileResponse response = new HttpFileResponse(mockResponse)) {
            // Then
            assertThat(response.getStatusMessage()).as("Reason phrase should match").isEqualTo("Internal Server Error");
        }
    }

    @Test
    public void testGetHeaderReturnsValue() throws Exception {
        // Given
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse(200, "OK");
        mockResponse.addHeader("X-Custom-Header", "custom-value");
        
        // When
        try (HttpFileResponse response = new HttpFileResponse(mockResponse)) {
            // Then
            assertThat(response.getHeader("X-Custom-Header")).as("Custom header should be present").isEqualTo("custom-value");
        }
    }

    @Test
    public void testGetHeaderReturnsNullForMissingHeader() throws Exception {
        // Given
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse(200, "OK");
        
        // When
        try (HttpFileResponse response = new HttpFileResponse(mockResponse)) {
            // Then
            assertThat(response.getHeader("Non-Existent-Header")).as("Missing header should return null").isNull();
        }
    }

    @Test
    public void testGetHeadersReturnsAllHeaders() throws Exception {
        // Given
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse(200, "OK");
        mockResponse.addHeader("Content-Type", "application/json");
        mockResponse.addHeader("Content-Length", "512");
        mockResponse.addHeader("X-Custom", "value");
        
        // When
        try (HttpFileResponse response = new HttpFileResponse(mockResponse)) {
            // Then
            assertThat(response.getheaders()).as("Should contain all headers").hasSize(3);
            assertThat(response.getheaders()).as("Should contain Content-Type").containsEntry("Content-Type", "application/json");
            assertThat(response.getheaders()).as("Should contain Content-Length").containsEntry("Content-Length", "512");
            assertThat(response.getheaders()).as("Should contain X-Custom").containsEntry("X-Custom", "value");
        }
    }

    @Test
    public void testGetContentTypeWithValidHeader() throws Exception {
        // Given
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse(200, "OK");
        mockResponse.addHeader("Content-Type", "application/json");
        
        // When
        try (HttpFileResponse response = new HttpFileResponse(mockResponse)) {
            // Then
            assertThat(response.getContentType()).as("Content type should be APPLICATION_JSON").isEqualTo(ContentType.APPLICATION_JSON);
        }
    }

    @Test
    public void testGetContentTypeWithCharset() throws Exception {
        // Given
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse(200, "OK");
        mockResponse.addHeader("Content-Type", "text/plain; charset=UTF-8");
        
        // When
        try (HttpFileResponse response = new HttpFileResponse(mockResponse)) {
            // Then
            assertThat(response.getContentType()).as("Content type should be TEXT_PLAIN").isEqualTo(ContentType.TEXT_PLAIN);
        }
    }

    @Test
    public void testGetContentTypeWithUnknownMimeType() throws Exception {
        // Given
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse(200, "OK");
        mockResponse.addHeader("Content-Type", "application/unknown-type");
        
        // When
        try (HttpFileResponse response = new HttpFileResponse(mockResponse)) {
            // Then
            assertThat(response.getContentType()).as("Unknown content type should return null").isNull();
        }
    }

    @Test
    public void testGetContentTypeWithNoHeader() throws Exception {
        // Given
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse(200, "OK");
        
        // When
        try (HttpFileResponse response = new HttpFileResponse(mockResponse)) {
            // Then
            assertThat(response.getContentType()).as("Missing Content-Type header should return null").isNull();
        }
    }

    @Test
    public void testGetContentLengthWithEntity() throws Exception {
        // Given
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse(200, "OK");
        mockResponse.setEntity(new MockHttpEntity(new byte[2048]));
        
        // When
        try (HttpFileResponse response = new HttpFileResponse(mockResponse)) {
            // Then
            assertThat(response.getContentLength()).as("Content length should be 2048").isEqualTo(2048);
        }
    }

    @Test
    public void testGetContentLengthWithNullEntity() throws Exception {
        // Given
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse(204, "No Content");
        mockResponse.setEntity(null);
        
        // When
        try (HttpFileResponse response = new HttpFileResponse(mockResponse)) {
            // Then
            assertThat(response.getContentLength()).as("Content length should be -1 when entity is null").isEqualTo(-1);
        }
    }

    @Test
    public void testIsSuccessfulWith2xxStatusCode() throws Exception {
        // Given
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse(200, "OK");
        
        // When
        try (HttpFileResponse response = new HttpFileResponse(mockResponse)) {
            // Then
            assertThat(response.isSuccessful()).as("200 should be successful").isTrue();
        }
    }

    @Test
    public void testIsSuccessfulWith3xxStatusCode() throws Exception {
        // Given
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse(301, "Moved Permanently");
        
        // When
        try (HttpFileResponse response = new HttpFileResponse(mockResponse)) {
            // Then
            assertThat(response.isSuccessful()).as("301 should be successful").isTrue();
        }
    }

    @Test
    public void testIsSuccessfulWith4xxStatusCode() throws Exception {
        // Given
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse(404, "Not Found");
        
        // When
        try (HttpFileResponse response = new HttpFileResponse(mockResponse)) {
            // Then
            assertThat(response.isSuccessful()).as("404 should not be successful").isFalse();
        }
    }

    @Test
    public void testIsSuccessfulWith5xxStatusCode() throws Exception {
        // Given
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse(500, "Internal Server Error");
        
        // When
        try (HttpFileResponse response = new HttpFileResponse(mockResponse)) {
            // Then
            assertThat(response.isSuccessful()).as("500 should not be successful").isFalse();
        }
    }

    @Test
    public void testIsSuccessfulWithBoundaryStatusCodes() throws Exception {
        // Test boundary conditions
        try (HttpFileResponse response199 = new HttpFileResponse(new MockClassicHttpResponse(199, ""))) {
            assertThat(response199.isSuccessful()).as("199 should not be successful").isFalse();
        }
        try (HttpFileResponse response200 = new HttpFileResponse(new MockClassicHttpResponse(200, ""))) {
            assertThat(response200.isSuccessful()).as("200 should be successful").isTrue();
        }
        try (HttpFileResponse response399 = new HttpFileResponse(new MockClassicHttpResponse(399, ""))) {
            assertThat(response399.isSuccessful()).as("399 should be successful").isTrue();
        }
        try (HttpFileResponse response400 = new HttpFileResponse(new MockClassicHttpResponse(400, ""))) {
            assertThat(response400.isSuccessful()).as("400 should not be successful").isFalse();
        }
    }

    @Test
    public void testCloseClosesUnderlyingResponse() throws Exception {
        // Given
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse(200, "OK");
        HttpFileResponse response = new HttpFileResponse(mockResponse);
        
        // When
        response.close();
        
        // Then
        assertThat(mockResponse.isClosed()).as("Underlying response should be closed").isTrue();
    }

    @Test
    public void testCloseIsIdempotent() throws Exception {
        // Given
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse(200, "OK");
        HttpFileResponse response = new HttpFileResponse(mockResponse);
        
        // When
        response.close();
        response.close();
        response.close();
        
        // Then - should not throw exception
        assertThat(mockResponse.getCloseCount()).as("Close should be called multiple times").isEqualTo(3);
    }

    @Test
    public void testCloseThrowsExceptionWhenUnderlyingResponseFails() throws Exception {
        // Given
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse(200, "OK");
        mockResponse.setCloseException(new IOException("Close failed"));
        HttpFileResponse response = new HttpFileResponse(mockResponse);
        
        // When/Then
        assertThatThrownBy(() -> response.close())
            .isInstanceOf(HttpClientException.class)
            .hasMessageContaining("Failed to close HTTP response")
            .hasCauseInstanceOf(IOException.class);
    }

    @Test
    public void testTryWithResourcesAutoClose() throws Exception {
        // Given
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse(200, "OK");
        mockResponse.setEntity(new MockHttpEntity("test content".getBytes()));
        
        // When
        try (HttpFileResponse response = new HttpFileResponse(mockResponse)) {
            assertThat(response.getStatusCode()).as("Should be able to use response").isEqualTo(200);
        }
        
        // Then
        assertThat(mockResponse.isClosed()).as("Response should be auto-closed").isTrue();
    }

    @Test
    public void testMultipleHeadersWithSameName() throws Exception {
        // Given
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse(200, "OK");
        mockResponse.addHeader("Set-Cookie", "cookie1=value1");
        mockResponse.addHeader("Set-Cookie", "cookie2=value2");
        
        // When
        try (HttpFileResponse response = new HttpFileResponse(mockResponse)) {
            // Then - last header value wins in the map
            assertThat(response.getHeader("Set-Cookie")).as("Should return last header value").isEqualTo("cookie2=value2");
        }
    }

    @Test
    public void testGetContentTypeWithAllSupportedTypes() throws Exception {
        // Test all ContentType enum values
        testContentTypeMapping("application/xml", ContentType.APPLICATION_XML);
        testContentTypeMapping("application/json", ContentType.APPLICATION_JSON);
        testContentTypeMapping("application/octet-stream", ContentType.APPLICATION_OCTET_STREAM);
        testContentTypeMapping("application/x-tar", ContentType.APPLICATION_X_TAR);
        testContentTypeMapping("multipart/form-data", ContentType.MULTIPART_FORM_DATA);
        testContentTypeMapping("application/x-www-form-urlencoded", ContentType.APPLICATION_FORM_URLENCODED);
        testContentTypeMapping("text/plain", ContentType.TEXT_PLAIN);
        testContentTypeMapping("text/html", ContentType.TEXT_HTML);
        testContentTypeMapping("text/xml", ContentType.TEXT_XML);
        testContentTypeMapping("application/rdf+xml", ContentType.RDF_XML);
        testContentTypeMapping("application/soap+xml", ContentType.SOAP_XML);
    }

    private void testContentTypeMapping(String mimeType, ContentType expectedContentType) throws Exception {
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse(200, "OK");
        mockResponse.addHeader("Content-Type", mimeType);
        try (HttpFileResponse response = new HttpFileResponse(mockResponse)) {
            assertThat(response.getContentType()).as("Content type for " + mimeType + " should match").isEqualTo(expectedContentType);
        }
    }
}
