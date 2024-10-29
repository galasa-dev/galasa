/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.http.internal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.w3c.dom.Document;

import com.google.gson.JsonObject;

import dev.galasa.http.ContentType;
import dev.galasa.http.HttpDelete;
import dev.galasa.http.HttpClientException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

/**
 * Representation of an HTTP Request as used in {@link HttpClientImpl}. This
 * class has a builder format, and after creation, at least
 * {@link #setUrl(String)} must be called before use, as well as one of the
 * setBody(...) methods if this is a PUT or POST request
 * 
 *  
 *
 */
public class HttpClientRequest {

    private enum RequestType {

        GET,
        DELETE,
        PUT,
        POST,
        PATCH,
        HEAD;
    }

    private final RequestType         type;
    private final Map<String, String> headers    = new HashMap<>();
    private final Map<String, String> parameters = new HashMap<>();
    private URIBuilder                uriBuilder;

    private HttpEntity                content;

    private HttpClientRequest(RequestType type) {
        this.type = type;
    }

    /**
     * Set the URL for the request
     * 
     * @param url
     * @return - the updated request
     */
    public HttpClientRequest setUrl(String url) {
        try {
            uriBuilder = new URIBuilder(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(url + " is not a valid url", e);
        }

        return this;
    }

    /**
     * Add a header to the request
     * 
     * @param header
     * @param value
     * @return - the updated request
     */
    public HttpClientRequest addHeader(String header, String value) {
        headers.put(header, value);

        return this;
    }

    /**
     * Set the content type of the request
     * 
     * @param contentType
     * @return - the updated request
     */
    public HttpClientRequest setContentType(ContentType contentType) {
        if (contentType != null) {
            return addHeader(HttpHeaders.CONTENT_TYPE, contentType.getMimeType());
        }

        return this;
    }

    /**
     * Set the accept type of the request
     * 
     * @param acceptTypes
     * @return - the updated request
     */
    public HttpClientRequest setAcceptTypes(ContentType[] acceptTypes) {

        if (acceptTypes != null && acceptTypes.length > 0) {

            StringBuilder sb = new StringBuilder();
            for (ContentType acceptType : acceptTypes) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(acceptType.getMimeType());
            }

            return addHeader(HttpHeaders.ACCEPT, sb.toString());
        }

        return this;
    }

    /**
     * Add a query parameter to the request
     * 
     * @param param
     * @param value
     * @return - the updated request
     */
    public HttpClientRequest addQueryParameter(String param, String value) {
        parameters.put(param, value);

        return this;
    }

    private HttpEntity getContent() {
        return content;
    }

    /**
     * Set the body of the request
     * 
     * @param data
     * @return - the updated request
     */
    public HttpClientRequest setBody(byte[] data) {
        this.content = new ByteArrayEntity(data);

        return this;
    }

    /**
     * Set the body of the request
     * 
     * @param data
     * @return - the updated request
     */
    public HttpClientRequest setBody(String data) {
        try {
            this.content = new StringEntity(data);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Body data '" + data + "' is invalid", e);
        }

        return this;
    }

    /**
     * Set the body of the request
     * 
     * @param data
     * @return - the updated request
     */
    public HttpClientRequest setBody(File data) {
        this.content = new FileEntity(data);

        return this;
    }

    /**
     * Set the body of the request
     * 
     * @param xml
     * @return - the updated request
     */
    public HttpClientRequest setXMLBody(Document xml) {
        return setBody(xml.getTextContent());
    }

    /**
     * Set the body of the request
     * 
     * @param json
     * @return - the updated request
     */
    public HttpClientRequest setJSONBody(JsonObject json) {
        return setBody(json.toString());
    }

    /**
     * Set the body of the request
     * 
     * <p>
     * The body must be a JAXB object
     * 
     * @param jaxbObject
     * @return - the updated request
     */
    public HttpClientRequest setJAXBBody(Object jaxbObject) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            JAXBContext context = JAXBContext.newInstance(jaxbObject.getClass());
            context.createMarshaller().marshal(jaxbObject, baos);
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Body is an instance of " + jaxbObject.getClass().getSimpleName()
                    + " which appears not to be a valid JAXB class", e);
        }
        this.content = new ByteArrayEntity(baos.toByteArray());

        return this;
    }

    /**
     * Build the {@link HttpUriRequest} that underlies this object. Only for use by
     * the {@link HttpClientImpl}
     * 
     * @return An {@link HttpUriRequest}
     * @throws HttpClientException
     */
    HttpUriRequest buildRequest() throws HttpClientException {

        if (uriBuilder == null) {
            throw new HttpClientException("URL has not been set for this request");
        }

        for (Entry<String, String> parameter : parameters.entrySet()) {
            uriBuilder.addParameter(parameter.getKey(), parameter.getValue());
        }

        URI uri;
        try {
            uri = uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Cannot construct URI from given url and parameters", e);
        }

        HttpUriRequest request;

        switch (type) {
            case DELETE:
                request = new HttpDelete(uri);
                break;
            case POST:
                request = new HttpPost(uri);
                break;
            case PUT:
                request = new HttpPut(uri);
                break;
            case HEAD:
                request = new HttpHead(uri);
                break;
            case PATCH:
                request = new HttpPatch(uri);
                break;
            case GET:
            default:
                request = new HttpGet(uri);
                break;
        }

        for (Entry<String, String> header : headers.entrySet()) {
            request.setHeader(header.getKey(), header.getValue());
        }

        if (request instanceof HttpEntityEnclosingRequestBase) {
            ((HttpEntityEnclosingRequestBase) request).setEntity(getContent());
        }

        return request;
    }

    /**
     * Create a new GET request
     * 
     * @param url
     * @param acceptTypes
     * @return new GET request
     */
    public static HttpClientRequest newGetRequest(String url, ContentType[] acceptTypes) {

        HttpClientRequest request = new HttpClientRequest(RequestType.GET);
        request.setUrl(url);
        request.setAcceptTypes(acceptTypes);

        return request;
    }

    /**
     * Create a new DELETE request
     * 
     * @param url
     * @param acceptTypes
     * @return new DELETE request
     */
    public static HttpClientRequest newDeleteRequest(String url, ContentType[] acceptTypes) {

        HttpClientRequest request = new HttpClientRequest(RequestType.DELETE);
        request.setUrl(url);
        request.setAcceptTypes(acceptTypes);

        return request;
    }

    /**
     * Create a new DELETE request
     * 
     * @param url
     * @param acceptTypes
     * @param contentType
     * @return new DELETE request
     */
    public static HttpClientRequest newDeleteRequest(String url, ContentType[] acceptTypes, ContentType contentType) {

      HttpClientRequest request = new HttpClientRequest(RequestType.DELETE);
      request.setUrl(url);
      request.setAcceptTypes(acceptTypes);
      request.setContentType(contentType);

      return request;
  }

    /**
     * Create a new PUT request
     * 
     * @param url
     * @param acceptTypes
     * @param contentType
     * @return new PUT request
     */
    public static HttpClientRequest newPutRequest(String url, ContentType[] acceptTypes, ContentType contentType) {

        HttpClientRequest request = new HttpClientRequest(RequestType.PUT);
        request.setUrl(url);
        request.setAcceptTypes(acceptTypes);
        request.setContentType(contentType);

        return request;
    }

    /**
     * Create a new POST request
     * 
     * @param url
     * @param acceptTypes
     * @param contentType
     * @return new POST request
     */
    public static HttpClientRequest newPostRequest(String url, ContentType[] acceptTypes, ContentType contentType) {

        HttpClientRequest request = new HttpClientRequest(RequestType.POST);
        request.setUrl(url);
        request.setAcceptTypes(acceptTypes);
        request.setContentType(contentType);

        return request;
    }
    /**
     * Create a new PATCH request
     *
     * @param url
     * @param acceptTypes
     * @param contentType
     * @return new POST request
     */
    public static HttpClientRequest newPatchRequest(String url, ContentType[] acceptTypes, ContentType contentType) {

        HttpClientRequest request = new HttpClientRequest(RequestType.PATCH);
        request.setUrl(url);
        request.setAcceptTypes(acceptTypes);
        request.setContentType(contentType);

        return request;
    }

    /**
     * Create a new HEAD request
     * 
     * @param url
     * @return new HEAD request
     */
    public static HttpClientRequest newHeadRequest(String url) {

        HttpClientRequest request = new HttpClientRequest(RequestType.HEAD);
        request.setUrl(url);

        return request;
    }
}
