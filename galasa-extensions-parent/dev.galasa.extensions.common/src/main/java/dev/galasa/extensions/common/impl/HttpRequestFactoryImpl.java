/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;

import dev.galasa.extensions.common.api.HttpRequestFactory;

public class HttpRequestFactoryImpl implements HttpRequestFactory {

    private Map<String, String> headers = new HashMap<String,String>();

    public HttpRequestFactoryImpl() {
        headers.put(HttpHeaders.ACCEPT, "application/json");
        headers.put(HttpHeaders.CONTENT_TYPE, "application/json");
    }

    public HttpRequestFactoryImpl(String authType, String authToken) {
        this();
        headers.put(HttpHeaders.AUTHORIZATION, authType + " " + authToken);
    }

    private HttpRequest addDefaultHeaders(HttpRequest request) {
        for (Map.Entry<String,String> header : headers.entrySet()) {
            request.addHeader(header.getKey(), header.getValue());
        }
        return request;
    }

    public HttpGet getHttpGetRequest(String url) {
        return (HttpGet) addDefaultHeaders(new HttpGet(url));
    }

    public HttpHead getHttpHeadRequest(String url) {
        return (HttpHead) addDefaultHeaders(new HttpHead(url));
    }

    public HttpPost getHttpPostRequest(String url) {
        return (HttpPost) addDefaultHeaders(new HttpPost(url));
    }

    public HttpPut getHttpPutRequest(String url) {
        return (HttpPut) addDefaultHeaders(new HttpPut(url));
    }

    public HttpDelete getHttpDeleteRequest(String url) {
        return (HttpDelete) addDefaultHeaders(new HttpDelete(url));
    }
}