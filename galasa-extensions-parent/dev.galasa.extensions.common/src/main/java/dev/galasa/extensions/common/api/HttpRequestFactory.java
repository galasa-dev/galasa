/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common.api;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;

public interface HttpRequestFactory {

    public HttpGet getHttpGetRequest(String url);

    public HttpHead getHttpHeadRequest(String url);

    public HttpPost getHttpPostRequest(String url);

    public HttpPut getHttpPutRequest(String url);

    public HttpDelete getHttpDeleteRequest(String url);
}
