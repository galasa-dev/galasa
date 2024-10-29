/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosrseapi;

import java.util.List;

import javax.validation.constraints.NotNull;

import java.net.HttpURLConnection;

import org.apache.http.HttpStatus;

import com.google.gson.JsonObject;

import dev.galasa.zos.IZosImage;

/**
 * 
 * Represents a RSE API server
 */
public interface IRseapi {
    


    /**
     * Enumeration of RSE API request types
     */
    public enum RseapiRequestType {
        /**
         * GET method with no request body
         */
        GET("GET"),
        /**
         * PUT method with text request body
         */
        PUT_TEXT("PUT"),
        /**
         * PUT method with binary request body
         */
        PUT_BINARY("PUT"),
        /**
         * PUT method with JSON request body
         */
        PUT_JSON("PUT"),
        /**
         * POST method with JSON request body
         */
        POST_JSON("POST"),
        /**
         * DELETE method with no request body
         */
        DELETE("DELETE");
        
        private String type;
        
        RseapiRequestType(String type) {
            this.type = type;
        }
        
        public String getRequestType() {
            return type;
        }
    }
    
    /**
     * Set an HTTP Header for the pending RSE API request
     * 
     * @param name header name
     * @param value header value 
     */
    public void setHeader(String name, String value);
    
    /**
     * Clear the existing HTTP Headers ready for the next RSE API request
     *  
     */
    void clearHeaders();

    /**
     * Issue an HTTP PUT request to the RSE API server with no request body
     * 
     * @param path identifies the RSE API REST API server
     * @param validStatusCodes a {@link List} of acceptable HTTP Status codes. Default to {@link HttpStatus#SC_OK} when null
     * @param convert is a data conversion required. If true, data will be converted betwen EBCDIC to ISO8859-1. If false, no data conversion will take place.
     * @return the RSE API server response
     * @throws RseapiException
     */
    public @NotNull IRseapiResponse get(String path, List<Integer> validStatusCodes, boolean convert) throws RseapiException;

    /**
	 * Issue an HTTP PUT request to the RSE API server with text request body {@code text/plain}
	 * 
	 * @param path identifies the RSE API REST API server
	 * @param body the request body
	 * @param validStatusCodes a {@link List} of acceptable HTTP Status codes. Default to {@link HttpStatus#SC_OK} when null
	 * @return the RSE API server response
	 * @throws RseapiException
	 */
	public @NotNull IRseapiResponse putText(String path, String body, List<Integer> validStatusCodes) throws RseapiException;

	/**
	 * Issue an HTTP PUT request to the RSE API server with a request body of content type of {@code application/json}
	 * 
	 * @param path identifies the RSE API REST API server
	 * @param body the request body
	 * @param validStatusCodes a {@link List} of acceptable HTTP Status codes. Default to {@link HttpStatus#SC_OK} when null
	 * @return the RSE API server response
	 * @throws RseapiException
	 */
	public @NotNull IRseapiResponse putJson(String path, JsonObject body, List<Integer> validStatusCodes) throws RseapiException;

    /**
    * Issue an HTTP PUT request to the RSE API server with a request body of content type of {@code text/plain}
    * 
    * @param path identifies the RSE API REST API server
    * @param body the request body
    * @param validStatusCodes a {@link List} of acceptable HTTP Status codes. Default to {@link HttpStatus#SC_OK} when null
    * @return the RSE API server response
    * @throws RseapiException
    */
   public @NotNull IRseapiResponse putBinary(String path, byte[] body, List<Integer> validStatusCodes) throws RseapiException;

	/**
     * Issue an HTTP POST request to the RSE API server with no request body
     * 
     * @param path identifies the RSE API REST API server
     * @param validStatusCodes a {@link List} of acceptable HTTP Status codes. Default to {@link HttpStatus#SC_OK} when null
     * @return the RSE API server response
     * @throws RseapiException
     */
    public @NotNull IRseapiResponse post(String path, List<Integer> validStatusCodes) throws RseapiException;

	/**
     * Issue an HTTP POST request to the RSE API server with a request body of content type of {@code application/json}
     * 
     * @param path identifies the RSE API REST API server
     * @param requestBody the request body
     * @param validStatusCodes a {@link List} of acceptable HTTP Status codes. Default to {@link HttpStatus#SC_OK} when null
     * @return the RSE API server response
     * @throws RseapiException
     */
    public @NotNull IRseapiResponse postJson(String path, JsonObject requestBody, List<Integer> validStatusCodes) throws RseapiException;

    /**
     * Issue an HTTP DELETE request to the RSE API server with no request body
     * 
     * @param path identifies the RSE API REST API server
     * @param validStatusCodes a {@link List} of acceptable HTTP Status codes. Default to {@link HttpStatus#SC_OK} when null
     * @return the RSE API server response
     * @throws RseapiException
     */
    public @NotNull IRseapiResponse delete(String path, List<Integer> validStatusCodes) throws RseapiException;
    
    /**
     * Return the JSON response from the RSE API server Server Information request
     * 
	 * @return the RSE API server JSON response
     * @throws RseapiException
     */
    public @NotNull JsonObject serverInfo() throws RseapiException;
    
    /**
     * Get the zOS image associated with the RSE API server
     * @return the zOS image
     */
    public IZosImage getImage();
}
