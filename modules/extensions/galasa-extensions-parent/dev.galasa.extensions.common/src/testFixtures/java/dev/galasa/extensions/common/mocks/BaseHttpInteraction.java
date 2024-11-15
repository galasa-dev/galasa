/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common.mocks;

import static org.assertj.core.api.Assertions.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;

import dev.galasa.framework.spi.utils.GalasaGson;

public abstract class BaseHttpInteraction implements HttpInteraction {

    private GalasaGson gson = new GalasaGson();

    private String expectedBaseUri ;

    private String responsePayload = "";
    private int responseStatusCode = HttpStatus.SC_OK;

    public BaseHttpInteraction(String expectedBaseUri, Object responsePayload) {
        this(expectedBaseUri, responsePayload, HttpStatus.SC_OK);
    }

    public BaseHttpInteraction(String expectedBaseUri, Object responsePayload, int responseStatusCode) {
        this.expectedBaseUri = expectedBaseUri;
        this.responseStatusCode = responseStatusCode;
        setResponsePayload(responsePayload);
    }

    public BaseHttpInteraction(String expectedBaseUri, int responseStatusCode) {
        this(expectedBaseUri, null, responseStatusCode);
    }

    public String getExpectedBaseUri() {
        return this.expectedBaseUri;
    }

    public String getExpectedHttpContentType() {
        return "application/json";
    }

    public void setResponsePayload(Object responsePayload) {
        this.responsePayload = gson.toJson(responsePayload);
    }

    @Override
    public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {

        String uri = request.getRequestLine().getUri();
        assertThat(uri).isEqualTo(this.expectedBaseUri);

        validateRequestContentType(request);
    }

    public void validateRequestContentType(HttpRequest request) {
        assertThat(request.containsHeader("Content-Type")).as("Missing Content-Type header!").isTrue();
        assertThat(request.getHeaders("Content-Type")[0].getValue()).isEqualTo(getExpectedHttpContentType());
    }

    @Override
    public MockCloseableHttpResponse getResponse() {
        HttpEntity entity = new MockHttpEntity(responsePayload);

        MockCloseableHttpResponse response = new MockCloseableHttpResponse();

        MockStatusLine statusLine = new MockStatusLine();
        statusLine.setStatusCode(responseStatusCode);
        response.setStatusLine(statusLine);
        response.setEntity(entity);

        return response;
    }
}