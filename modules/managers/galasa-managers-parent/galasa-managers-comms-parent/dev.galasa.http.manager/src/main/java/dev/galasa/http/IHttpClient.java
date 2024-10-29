/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.http;

import java.io.InputStream;
import java.net.URI;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.client.methods.CloseableHttpResponse;

import com.google.gson.JsonObject;

import dev.galasa.http.internal.HttpClientRequest;

public interface IHttpClient {

    /**
     * Issue an HTTP GET to the provided URL, receiving a JAXB Object in the
     * response. In order to unmarshal the response, an array of possible response
     * classes must be provided in responseTypes
     * 
     * @param url
     * @param responseTypes
     * @return - {@link HttpClientResponse} with a JAXB content type
     * @throws HttpClientException
     */
    HttpClientResponse<Object> getJaxb(String url, Class<?>... responseTypes) throws HttpClientException;

    /**
     * Issue an HTTP PUT to the provided URL, sending the provided jaxbObject and
     * receiving a JAXB Object in the response. In order to unmarshal the response,
     * an array of possible response classes must be provided in responseTypes
     * 
     * @param url
     * @param jaxbObject
     * @param responseTypes
     * @return - {@link HttpClientResponse} with a JAXB content type
     * @throws HttpClientException
     */
    HttpClientResponse<Object> putJaxb(String url, Object jaxbObject, Class<?>... responseTypes)
            throws HttpClientException;

    /**
     * Issue an HTTP POST to the provided URL, sending the provided jaxbObject and
     * receiving a JAXB Object in the response. In order to unmarshal the response,
     * an array of possible response classes must be provided in responseTypes
     * 
     * @param url
     * @param jaxbObject
     * @param responseTypes
     * @return - {@link HttpClientResponse} with a JAXB content type
     * @throws HttpClientException
     */
    HttpClientResponse<Object> postJaxb(String url, Object jaxbObject, Class<?>... responseTypes)
            throws HttpClientException;

    /**
     * Issue an HTTP DELETE to the provided URL, receiving a JAXB Object in the
     * response. In order to unmarshal the response, an array of possible response
     * classes must be provided in responseTypes
     * 
     * @param url
     * @param responseTypes
     * @return - {@link HttpClientResponse} with a JAXB content type
     * @throws HttpClientException
     */
    HttpClientResponse<Object> deleteJaxb(String url, Class<?>... responseTypes) 
            throws HttpClientException;

    /**
     * Issue an HTTP PUT to the provided URL, sending the provided XML as a String and
     * receiving a String in the response. 
     * @param url
     * @param xml
     * @return - {@link HttpClientResponse} with a String content type
     * @throws HttpClientException
     */
    HttpClientResponse<String> putXML(String url, String xml)
            throws HttpClientException;

    /**
     * Issue an HTTP POST to the provided URL, sending the provided XML as a String and
     * receiving a String in the response. Uses a content type of application/soap+xml
     * 
     * @param url
     * @param xml
     * @return - {@link HttpClientResponse} with a String content type
     * @throws HttpClientException
     */
    HttpClientResponse<String> postSOAP(String url, String xml)
            throws HttpClientException;

    /**
     * Issue an HTTP PUT to the provided URL, sending the provided XML as a String and
     * receiving a String in the response. Uses a content type of application/soap+xml
     * @param url
     * @param xml
     * @return - {@link HttpClientResponse} with a String content type
     * @throws HttpClientException
     */
    HttpClientResponse<String> putSOAP(String url, String xml)
            throws HttpClientException;

    /**
     * Issue an HTTP POST to the provided URL, sending the provided XML as a String and
     * receiving a String in the response. 
     * 
     * @param url
     * @param xml
     * @return - {@link HttpClientResponse} with a String content type
     * @throws HttpClientException
     */
    HttpClientResponse<String> postXML(String url, String xml)
            throws HttpClientException;
    
    /**
     * Issue an HTTP GET to the provided URL, receiving a com.google.gson.JsonObject in the
     * response.
     * 
     * @param url
     * @return - {@link HttpClientResponse} with a com.google.gson.JsonObject content type
     * @throws HttpClientException
     */
    HttpClientResponse<JsonObject> getJson(String url) throws HttpClientException;

    /**
     * Issue an HTTP POST to the provided URL, sending the provided
     * com.google.gson.JsonObject and receiving a com.google.gson.JsonObject in the response.
     * 
     * @param url
     * @param json
     * @return - {@link HttpClientResponse} with a com.google.gson.JsonObject content type
     * @throws HttpClientException
     */
    HttpClientResponse<JsonObject> postJson(String url, JsonObject json) throws HttpClientException;

    /**
     * Issue an HTTP PATCH to the provided URL, sending the provided
     * com.google.gson.JsonObject and receiving a com.google.gson.JsonObject in the response.
     *
     * @param url
     * @param json
     * @return - {@link HttpClientResponse} with a com.google.gson.JsonObject content type
     * @throws HttpClientException
     */
    HttpClientResponse<JsonObject> patchJson(String url, JsonObject json) throws HttpClientException;


    /**
     * Issue an HTTP PUT to the provided URL, sending the provided
     * <code>com.google.gson.JsonObject</code> and receiving a <code>com.google.gson.JsonObject</code> in the response.
     * 
     * @param url
     * @param json
     * @return - {@link HttpClientResponse} with a <code>com.google.gson.JsonObject</code> content type
     * @throws HttpClientException
     */
    HttpClientResponse<JsonObject> putJson(String url, JsonObject json) throws HttpClientException;

    /**
     * Issue an HTTP DELETE to the provided URL, receiving a com.google.gson.JsonObject in
     * the response.
     * 
     * @param url
     * @return - {@link HttpClientResponse} with a com.google.gson.JsonObject content type
     * @throws HttpClientException
     */
    HttpClientResponse<JsonObject> deleteJson(String url) throws HttpClientException;

    /**
     * Issue an HTTP DELETE to the provided URL, receiving a com.google.gson.JsonObject in
     * the response.
     * 
     * @param url
     * @param json
     * @return - {@link HttpClientResponse} with a com.google.gson.JsonObject content type
     * @throws HttpClientException
     */
    HttpClientResponse<JsonObject> deleteJson(String url, JsonObject json) throws HttpClientException;

    /**
     * Issue an HTTP GET to the provided URL, receiving a {@link String} in the
     * response.
     * 
     * @param url
     * @return - {@link HttpClientResponse} with a {@link String} content type
     * @throws HttpClientException
     */
    HttpClientResponse<String> getText(String url) throws HttpClientException;

    /**
     * Issue an HTTP POST to the provided URL, sending the provided {@link String}
     * and receiving a {@link String} in the response.
     * 
     * @param url
     * @param text
     * @return - {@link HttpClientResponse} with a {@link String} content type
     * @throws HttpClientException
     */
    HttpClientResponse<String> postText(String url, String text) throws HttpClientException;

    /**
     * Issue an HTTP PUT to the provided URL, sending the provided {@link String}
     * and receiving a {@link String} in the response.
     * 
     * @param url
     * @param text
     * @return - {@link HttpClientResponse} with a {@link String} content type
     * @throws HttpClientException
     */
    HttpClientResponse<String> putText(String url, String text) throws HttpClientException;

    /**
     * Issue an HTTP DELETE to the provided URL, receiving a {@link String} in the
     * response.
     * 
     * @param url
     * @return - {@link HttpClientResponse} with a {@link String} content type
     * @throws HttpClientException
     */
    HttpClientResponse<String> deleteText(String url) throws HttpClientException;

        /**
     * Issue an HTTP PUT to the provided URL, sending the provided
     * {@link byte[]} and receiving a {@link byte[]} in the response.
     * 
     * @param url
     * @param binary
     * @return - {@link HttpClientResponse} with a com.google.gson.JsonObject content type
     * @throws HttpClientException
     */
    HttpClientResponse<byte[]> putBinary(String url, byte[] binary) throws HttpClientException;

    /**
     * Issue an HTTP GET to the provided URL, sending the provided
     * {@link byte[]} and receiving a {@link byte[]} in the response.
     * 
     * @param url
     * @param binary
     * @return - {@link HttpClientResponse} with a com.google.gson.JsonObject content type
     * @throws HttpClientException
     */
    HttpClientResponse<byte[]> getBinary(String url, byte[] binary) throws HttpClientException;

    /**
     * Issue an HTTP POST to the provided URL, sending the provided
     * {@link byte[]} and receiving a {@link byte[]} in the response.
     * 
     * @param url
     * @param binary
     * @return - {@link HttpClientResponse} with a com.google.gson.JsonObject content type
     * @throws HttpClientException
     */
    HttpClientResponse<byte[]> postBinary(String url, byte[] binary) throws HttpClientException;

    /**
     * Issue an HTTP DELETE to the provided URL, sending the provided
     * {@link byte[]} and receiving a {@link byte[]} in the response.
     * 
     * @param url
     * @param binary
     * @return - {@link HttpClientResponse} with a com.google.gson.JsonObject content type
     * @throws HttpClientException
     */
    HttpClientResponse<byte[]> deleteBinary(String url, byte[] binary) throws HttpClientException;

    /**
     * Download a file from a specified location to a specified destination on local host.
     * 
     * @param path = URL path
     */
    CloseableHttpResponse getFile(String path) throws HttpClientException;
    
    /**
     * Download a file from a specified location to a specified destination on local host.
     * 
     * @param acceptTypes
     * @param path - URL path
     */
    CloseableHttpResponse getFile(String path, ContentType... acceptTypes) throws HttpClientException;

    /**
     * Send a compressed (tar) file from a local location to a specified destination on a host.
     * 
     * @param path - URL path
     * @param file - tar archive file
     */
    void putFile(String path, InputStream file);

    /**
     * Execute an {@link HttpClientRequest} returning a JAXB object available
     * through the returned {@link HttpClientResponse}. In order to unmarshal the
     * response, an array of possible response classes must be provided in
     * responseTypes *
     * 
     * @param url
     * @return - {@link HttpClientResponse}
     * @throws HttpClientException
     */
    HttpClientResponse<String> head(String url) throws HttpClientException;

    Object post(String path, Map<String, String> queryParams, ContentType contentType, Object data,
            ContentType[] acceptTypes, Class<?>[] jaxbClasses, boolean retry) throws HttpClientException;

    Object postForm(String path, Map<String, String> queryParams, HashMap<String, String> fields,
            ContentType[] acceptTypes, Class<?>[] jaxbClasses, boolean retry) throws HttpClientException;

    
    /**
     * Get the SSL context used by this client
     * 
     * @return the {@link SSLContext} or null if there is none
     */
    SSLContext getSSLContext();

    /**
     * Get the username set for this client
     * 
     * @return the username
     */
    String getUsername();

    /**
     * Get the username set for this client for a specific scope
     * 
     * @param scope
     * @return the username
     */
    String getUsername(URI scope);

    /**
     * Add a header that will be used on all http requests
     * 
     * @param name
     * @param value
     */
    void addCommonHeader(String name, String value);

    /**
     * Remove all headers for this http request
     */
    void clearCommonHeaders();

    /**
     * Add a response code for the execute to ignore and treat as OK
     * 
     * @param responseCode
     */
    void addOkResponseCode(int responseCode);

    /**
     * Build the client
     * 
     * @return the built client
     */
    IHttpClient build();

    /**
     * close the underlying HTTPClient
     */
    void close();
    /**
     * Set the username and password for all scopes
     * 
     * @param username
     * @param password
     * @return the updated client
     */
    IHttpClient setAuthorisation(String username, String password);

    /**
     * Set the username and password for a specific scope
     * 
     * @param username
     * @param password
     * @param scope
     * @return the updated client
     */
    IHttpClient setAuthorisation(String username, String password, URI scope);

    /**
     * Set the hostname verifier
     * 
     * 
     * @param hostnameVerifier
     * @return the updated client
     */
    IHttpClient setHostnameVerifier(HostnameVerifier hostnameVerifier);

    /**
     * Set the hostname verifier to a no-op verifier
     * 
     * @return the updated client
     */
    IHttpClient setNoopHostnameVerifier();

    /**
     * Set the SSL Context
     * 
     * @param sslContext
     * @return the updated client
     */
    IHttpClient setSSLContext(SSLContext sslContext);

    /**
     * Set the SSL Context to a Trust All context
     * 
     * @return the updated client
     * @throws HttpClientException
     */
    IHttpClient setTrustingSSLContext() throws HttpClientException;

    /**
     * Set up Client Authentication SSL Context and install
     * 
     * @param clientKeyStore
     * @param serverKeyStore
     * @param alias
     * @param password
     * @return the updated client
     * @throws HttpClientException
     */
    IHttpClient setupClientAuth(KeyStore clientKeyStore, KeyStore serverKeyStore, String alias, String password)
            throws HttpClientException;

    /**
     * Set the URI endpoint for this client
     * 
     * @param host
     */
    void setURI(URI host);

}
