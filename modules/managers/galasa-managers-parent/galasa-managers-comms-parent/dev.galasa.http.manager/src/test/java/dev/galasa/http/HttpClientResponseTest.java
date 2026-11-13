/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.http;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * Unit tests for HttpClientResponse class
 */
public class HttpClientResponseTest {

    @Test
    public void testGetHeaderIsCaseInsensitiveForLowerCaseServerHeaders() throws Exception {
        // Given
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse(200, "OK");
        mockResponse.addHeader("content-type", "application/json");

        // When
        HttpClientResponse<String> response = HttpClientResponse.textResponse(mockResponse);

        // Then
        assertThat(response.getHeader("content-type")).isEqualTo("application/json");
        assertThat(response.getHeader("Content-Type")).isEqualTo("application/json");
        assertThat(response.getHeader("CONTENT-TYPE")).isEqualTo("application/json");
    }

    @Test
    public void testGetHeaderIsCaseInsensitiveForTitleCaseServerHeaders() throws Exception {
        // Given
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse(200, "OK");
        mockResponse.addHeader("Content-Type", "text/plain");

        // When
        HttpClientResponse<byte[]> response = HttpClientResponse.byteResponse(mockResponse);

        // Then
        assertThat(response.getHeader("content-type")).isEqualTo("text/plain");
        assertThat(response.getHeader("Content-Type")).isEqualTo("text/plain");
        assertThat(response.getHeader("CONTENT-TYPE")).isEqualTo("text/plain");
    }

    @Test
    public void testGetHeadersMapSupportsCaseInsensitiveLookup() throws Exception {
        // Given
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse(200, "OK");
        mockResponse.addHeader("content-type", "application/json");
        mockResponse.addHeader("x-content-type-options", "nosniff");

        // When
        HttpClientResponse<String> response = HttpClientResponse.textResponse(mockResponse);

        // Then
        assertThat(response.getheaders()).containsKey("Content-Type");
        assertThat(response.getheaders()).containsKey("X-Content-Type-Options");
        assertThat(response.getheaders()).hasSize(2);
    }

    @Test
    public void testGetHeaderReturnsNullForMissingHeader() throws Exception {
        // Given
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse(200, "OK");

        // When
        HttpClientResponse<String> response = HttpClientResponse.textResponse(mockResponse);

        // Then
        assertThat(response.getHeader("Content-Type")).isNull();
    }
}
