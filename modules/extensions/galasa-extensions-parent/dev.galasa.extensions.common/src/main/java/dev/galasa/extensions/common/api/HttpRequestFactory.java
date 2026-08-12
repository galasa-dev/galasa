/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common.api;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;

public interface HttpRequestFactory {

    public HttpGet getHttpGetRequest(String url);

    public HttpHead getHttpHeadRequest(String url);

    public HttpPost getHttpPostRequest(String url);

    public HttpPut getHttpPutRequest(String url);

    public HttpDelete getHttpDeleteRequest(String url);
}
