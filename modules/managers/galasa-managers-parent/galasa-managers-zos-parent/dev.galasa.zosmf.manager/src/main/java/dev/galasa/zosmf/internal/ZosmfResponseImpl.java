/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosmf.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.client.methods.CloseableHttpResponse;

import dev.galasa.http.HttpClientResponse;
import dev.galasa.zosmf.IZosmfResponse;
import dev.galasa.zosmf.ZosmfException;

public class ZosmfResponseImpl implements IZosmfResponse {

    private URL requestUrl;
    private Object content;
    private int statusCode;
    private String statusLine;

    public ZosmfResponseImpl(String url, String path) throws MalformedURLException {
        this.requestUrl = new URL(url + path); 
    }

    @Override
    public JsonObject getJsonContent() throws ZosmfException {
        if (this.content instanceof String) {
            return new JsonParser().parse((String) this.content).getAsJsonObject();
        } else if (this.content instanceof byte[]) {
            return new JsonParser().parse(new String((byte[]) this.content)).getAsJsonObject();
        } else if (this.content instanceof InputStream) {
            return new JsonParser().parse(new InputStreamReader((InputStream) this.content)).getAsJsonObject();
        } else if (content instanceof JsonObject) {
            return (JsonObject) this.content;
        }
        
        throw new ZosmfException("Content not a JsonObject - " + content.getClass().getName());
    }

    @Override
    public JsonArray getJsonArrayContent() throws ZosmfException {
        if (this.content instanceof String) {
            return new JsonParser().parse((String) this.content).getAsJsonArray();
        } else if (this.content instanceof byte[]) {
            return new JsonParser().parse(new String((byte[]) this.content)).getAsJsonArray();
        } else if (this.content instanceof InputStream) {
            return new JsonParser().parse(new InputStreamReader((InputStream) this.content)).getAsJsonArray();
        }
        
        throw new ZosmfException("Content not a JsonArray Object - " + content.getClass().getName());
    }

    @Override
    public String getTextContent() throws ZosmfException {
        if (!(this.content instanceof String)) {
            throw new ZosmfException("Content not a String Object - " + content.getClass().getName());
        }
        return (String) content;
    }

    @Override
    public Object getContent() throws ZosmfException {
        return this.content;
    }

    @Override
    public int getStatusCode() {
        return this.statusCode;
    }

    @Override
    public String getStatusLine() {
        return this.statusLine;
    }

    @Override
    public URL getRequestUrl() {
        return this.requestUrl;
    }

    protected void setHttpClientresponse(HttpClientResponse<?> httpClientResponse) {
        this.content = httpClientResponse.getContent();
        this.statusCode = httpClientResponse.getStatusCode();
        this.statusLine = httpClientResponse.getStatusLine();
    }

    protected void setHttpClientresponse(CloseableHttpResponse httpClientResponse) throws ZosmfException{
        try{
            this.content = httpClientResponse.getEntity().getContent();
            this.statusCode = httpClientResponse.getStatusLine().getStatusCode();
            this.statusLine = httpClientResponse.getStatusLine().getReasonPhrase();
        } catch (IOException e) {
            throw new ZosmfException("Could not retrieve response", e);
        }
    }

}
