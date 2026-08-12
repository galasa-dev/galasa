/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common.impl;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

import dev.galasa.extensions.common.api.HttpRequestFactory;

public class HttpRequestFactoryTest {

    @Test
    public void testGETRequestReturnsRequestWithGETMethod() throws Exception {
        //Given ...
        String token = "myvalue";
        String authType = "Basic";

        HttpRequestFactory requestFactory = new HttpRequestFactoryImpl(authType, token);
        String url = "http://example.com/get";

        //When ...
        HttpGet request = requestFactory.getHttpGetRequest(url);

        //Then ...
        assertThat(request.getUri().toString()).isEqualTo(url);
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getFirstHeader("Authorization").getValue()).isEqualTo("Basic "+token);
        assertThat(request.getFirstHeader("Content-Type").getValue()).isEqualTo("application/json");
        assertThat(request.getFirstHeader("Accept").getValue()).isEqualTo("application/json");
    }

    @Test
    public void testHEADRequestReturnsRequestWithHEADMethod() throws Exception {
        //Given ...
        String token = "iamnottryingtogetahead";
        String authType = "Basic";

        HttpRequestFactory requestFactory = new HttpRequestFactoryImpl(authType, token);
        String url = "http://example.com/head";

        //When ...
        HttpHead request = requestFactory.getHttpHeadRequest(url);

        //Then ...
        assertThat(request.getUri().toString()).isEqualTo(url);
        assertThat(request.getMethod()).isEqualTo("HEAD");
        assertThat(request.getFirstHeader("Authorization").getValue()).isEqualTo("Basic "+token);
        assertThat(request.getFirstHeader("Content-Type").getValue()).isEqualTo("application/json");
        assertThat(request.getFirstHeader("Accept").getValue()).isEqualTo("application/json");
    }

    @Test
    public void testPOSTRequestReturnsRequestWithPOSTMethod() throws Exception {
        //Given ...
        String token = "mysecretPOSTtoken";
        String authType = "Basic";

        HttpRequestFactory requestFactory = new HttpRequestFactoryImpl(authType, token);
        String url = "http://example.com/post";

        //When ...
        HttpPost request = requestFactory.getHttpPostRequest(url);

        //Then ...
        assertThat(request.getUri().toString()).isEqualTo(url);
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getFirstHeader("Authorization").getValue()).isEqualTo("Basic "+token);
        assertThat(request.getFirstHeader("Content-Type").getValue()).isEqualTo("application/json");
        assertThat(request.getFirstHeader("Accept").getValue()).isEqualTo("application/json");
    }

    @Test
    public void testPUTRequestReturnsRequestWithPUTMethod() throws Exception {
        //Given ...
        String token = "iPut";
        String authType = "Basic";

        HttpRequestFactory requestFactory = new HttpRequestFactoryImpl(authType, token);
        String url = "http://example.com/put";

        //When ...
        HttpPut request = requestFactory.getHttpPutRequest(url);

        //Then ...
        assertThat(request.getUri().toString()).isEqualTo(url);
        assertThat(request.getMethod()).isEqualTo("PUT");
        assertThat(request.getFirstHeader("Authorization").getValue()).isEqualTo("Basic "+token);
        assertThat(request.getFirstHeader("Content-Type").getValue()).isEqualTo("application/json");
        assertThat(request.getFirstHeader("Accept").getValue()).isEqualTo("application/json");
    }

    @Test
    public void testDELETERequestReturnsRequestWithDELETEMethod() throws Exception {
        //Given ...
        String token = "idontneedthisanymore";
        String authType = "Basic";

        HttpRequestFactory requestFactory = new HttpRequestFactoryImpl(authType, token);
        String url = "http://example.com/delete";

        //When ...
        HttpDelete request = requestFactory.getHttpDeleteRequest(url);

        //Then ...
        assertThat(request.getUri().toString()).isEqualTo(url);
        assertThat(request.getMethod()).isEqualTo("DELETE");
        assertThat(request.getFirstHeader("Authorization").getValue()).isEqualTo("Basic "+token);
        assertThat(request.getFirstHeader("Content-Type").getValue()).isEqualTo("application/json");
        assertThat(request.getFirstHeader("Accept").getValue()).isEqualTo("application/json");
    }

    @Test
    public void testGETRequestwithExtraHeadersReturnsRequestWithExtraHeaders() throws Exception {
        //Given ...
        String token = "getwithextraheaders";
        String authType = "Basic";

        HttpRequestFactory requestFactory = new HttpRequestFactoryImpl(authType, token);
        String url = "http://example.com/get";
        String referer = "Galasa";
        String encoding = "gzip, deflate, br";

        //When ...
        HttpGet request = requestFactory.getHttpGetRequest(url);
        request.setHeader("Referer", referer);
        request.setHeader("Accept-Encoding", encoding);

        //Then ...
        assertThat(request.getUri().toString()).isEqualTo(url);
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getFirstHeader("Authorization").getValue()).isEqualTo("Basic "+token);
        assertThat(request.getFirstHeader("Content-Type").getValue()).isEqualTo("application/json");
        assertThat(request.getFirstHeader("Accept").getValue()).isEqualTo("application/json");
        assertThat(request.getFirstHeader("Referer").getValue()).isEqualTo(referer);
        assertThat(request.getFirstHeader("Accept-Encoding").getValue()).isEqualTo(encoding);
    }

    @Test
    public void testPOSTRequestwithUpdatedHeadersReturnsRequestWithUpdatedHeaders() throws Exception {
        //Given ...
        String token = "getwithextraheaders";
        String authType = "Basic";

        HttpRequestFactory requestFactory = new HttpRequestFactoryImpl(authType, token);
        String url = "http://example.com/post";
        String accept = "application/xml";
        String contentType = "text/html";

        //When ...
        HttpPost request = requestFactory.getHttpPostRequest(url);
        request.setHeader("Accept", accept);
        request.setHeader("Content-Type", contentType);

        //Then ...
        assertThat(request.getUri().toString()).isEqualTo(url);
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getFirstHeader("Authorization").getValue()).isEqualTo("Basic "+token);
        assertThat(request.getHeaders("Content-Type").length).isEqualTo(1);
        assertThat(request.getFirstHeader("Content-Type").getValue()).isEqualTo(contentType);
        assertThat(request.getHeaders("Accept").length).isEqualTo(1);
        assertThat(request.getFirstHeader("Accept").getValue()).isEqualTo(accept);

    }
}
