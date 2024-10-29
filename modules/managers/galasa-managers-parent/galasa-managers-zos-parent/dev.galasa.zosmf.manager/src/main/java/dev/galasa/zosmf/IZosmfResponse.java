/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosmf;

import java.net.URL;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Represents the response from a zOSMF server request
 *
 */
public interface IZosmfResponse {

    /**
     * Return the text content from the zOSMF request as text
     * <p>{@code Content-Type: text/plain}
     * @return the content
     * @throws ZosmfException
     */
    public String getTextContent() throws ZosmfException;

    /**
     * Return the text content from the zOSMF request as a JSON object
     * <p>{@code Content-Type: application/json}
     * @return the content
     * @throws ZosmfException
     */
    public JsonObject getJsonContent() throws ZosmfException;

    /**
     * Return the text content from the zOSMF request JSON array object
     * <p>{@code Content-Type: application/json}
     * @return the content
     * @throws ZosmfException
     */
    public JsonArray getJsonArrayContent() throws ZosmfException;

    /**
     * Return the content from the zOSMF request as an object
     * @return the content
     * @throws ZosmfException
     */
    public Object getContent() throws ZosmfException;

    /**
     * Return the HTTP status code from the zOSMF request
     * @return the HTTP code
     */
    public int getStatusCode();

    /**
     * Return the HTTP status code and status text from the zOSMF request
     * @return
     */
    public String getStatusLine();

    /**
     * Return the URL for this zOSMF request
     * @return the request URL
     */
    public URL getRequestUrl();
}
