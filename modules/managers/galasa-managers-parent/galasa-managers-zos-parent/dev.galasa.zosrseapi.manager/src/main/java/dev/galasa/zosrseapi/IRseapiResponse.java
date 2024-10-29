/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosrseapi;

import java.net.URL;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Represents the response from a RSE API server request
 *
 */
public interface IRseapiResponse {

    /**
     * Return the text content from the RSE API request as text
     * <p>{@code Content-Type: text/plain}
     * @return the content
     * @throws RseapiException
     */
    public String getTextContent() throws RseapiException;

    /**
     * Return the text content from the RSE API request as a JSON object
     * <p>{@code Content-Type: application/json}
     * @return the content
     * @throws RseapiException
     */
    public JsonObject getJsonContent() throws RseapiException;

    /**
     * Return the text content from the RSE API request JSON array object
     * <p>{@code Content-Type: application/json}
     * @return the content
     * @throws RseapiException
     */
    public JsonArray getJsonArrayContent() throws RseapiException;

    /**
     * Return the content from the RSE API request as an object
     * @return the content
     * @throws RseapiException
     */
    public Object getContent() throws RseapiException;

    /**
     * Return the HTTP status code from the RSE API request
     * @return the HTTP code
     */
    public int getStatusCode();

    /**
     * Return the HTTP status code and status text from the RSE API request
     * @return
     */
    public String getStatusLine();

    /**
     * Return the URL for this RSE API request
     * @return the request URL
     */
    public URL getRequestUrl();
}
