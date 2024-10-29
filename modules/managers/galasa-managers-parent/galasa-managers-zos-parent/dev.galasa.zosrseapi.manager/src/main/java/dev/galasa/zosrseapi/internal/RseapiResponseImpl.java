/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosrseapi.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;

import dev.galasa.http.HttpClientResponse;
import dev.galasa.zosrseapi.IRseapiResponse;
import dev.galasa.zosrseapi.RseapiException;

public class RseapiResponseImpl implements IRseapiResponse {

    private URL requestUrl;
    private Object content;
    private int statusCode;
    private String statusLine;

    public RseapiResponseImpl(String url, String path) throws MalformedURLException {
        this.requestUrl = new URL(url + path); 
    }

    @Override
    public JsonObject getJsonContent() throws RseapiException {
        if (this.content instanceof String) {
            return new JsonParser().parse((String) this.content).getAsJsonObject();
        } else if (this.content instanceof byte[]) {
            return new JsonParser().parse(new String((byte[]) this.content)).getAsJsonObject();
        } else if (this.content instanceof InputStream) {
            return new JsonParser().parse(new InputStreamReader((InputStream) this.content)).getAsJsonObject();
        } else if (content instanceof JsonObject) {
            return (JsonObject) this.content;
        }
        
        throw new RseapiException("Content not a JsonObject - " + content.getClass().getName());
    }

    @Override
    public JsonArray getJsonArrayContent() throws RseapiException {
        if (this.content instanceof String) {
            return new JsonParser().parse((String) this.content).getAsJsonArray();
        } else if (this.content instanceof byte[]) {
            return new JsonParser().parse(new String((byte[]) this.content)).getAsJsonArray();
        } else if (this.content instanceof InputStream) {
            return new JsonParser().parse(new InputStreamReader((InputStream) this.content)).getAsJsonArray();
        }
        
        throw new RseapiException("Content not a JsonArray Object - " + content.getClass().getName());
    }

    @Override
    public String getTextContent() throws RseapiException {
    	if (this.content instanceof InputStream) {
    		try {
				return IOUtils.toString(new InputStreamReader((InputStream) this.content));
			} catch (IOException e) {
				throw new RseapiException("Unable to convert content to String Object", e);
			}
    	} else if (!(this.content instanceof String)) {
            throw new RseapiException("Content not a String or InputStream Object - " + content.getClass().getName());
        }
        return (String) content;
    }

    @Override
    public Object getContent() throws RseapiException {
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

    protected void setHttpClientresponse(CloseableHttpResponse httpClientResponse) throws RseapiException{
        try{
            this.content = httpClientResponse.getEntity().getContent();
            this.statusCode = httpClientResponse.getStatusLine().getStatusCode();
            this.statusLine = httpClientResponse.getStatusLine().getReasonPhrase();
        } catch (IOException e) {
            throw new RseapiException("Could not retrieve response", e);
        }
    }

}
