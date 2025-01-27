/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.http;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

import dev.galasa.framework.spi.utils.GalasaGson;
/**
 * Parametrisable representation of a response to an HTTP request. The parameter
 * describes the content type of the response. Use the static methods to create
 * instances from an {@link HttpResponse}.
 * 
 *  
 *
 * @param <T> Class describing the content type of the response
 */
public class HttpClientResponse<T> {

    private int                       statusCode;
    private String                    statusMessage;
    private String                    protocolVersion;
    private T                         content;
    private final Map<String, String> headers = new HashMap<>();
    private static final Log logger = LogFactory.getLog(HttpClientResponse.class);

    private HttpClientResponse() {
    }

    private void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * 
     * @return - status code of the response
     */
    public int getStatusCode() {
        return statusCode;
    }

    private void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    /**
     * 
     * @return - reason phrase or status message of the response
     */
    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * 
     * @return - protocol version string of the response
     */
    public String getProtocolVersion() {
        return protocolVersion;
    }

    private void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    /**
     * 
     * @return - full status line
     */
    public String getStatusLine() {
        return protocolVersion + " " + statusCode + " " + statusMessage;
    }

    private void setContent(T content) {
        this.content = content;
    }

    /**
     * 
     * @return - the content of the response
     */
    public T getContent() {
        return content;
    }

    private void setHeader(String header, String value) {
        headers.put(header, value);
    }

    /**
     * Get the value of a specific header as a String. May be null
     * 
     * @param header
     * @return - the value of the header
     */
    public Object getHeader(String header) {
        return headers.get(header);
    }

    /**
     * Get all headers from the response
     * 
     * @return - map of headers and values
     */
    public Map<String, String> getheaders() {
        return headers;
    }

    private void populateGenericValues(HttpResponse httpResponse) {

        setStatusCode(httpResponse.getStatusLine().getStatusCode());
        setStatusMessage(httpResponse.getStatusLine().getReasonPhrase());
        setProtocolVersion(httpResponse.getStatusLine().getProtocolVersion().toString());

        for (Header header : httpResponse.getAllHeaders()) {
            setHeader(header.getName(), header.getValue());
        }
    }

    /**
     * Create an {@link HttpClientResponse} with a byte array content type from an
     * {@link HttpResponse}.
     * 
     * @param httpResponse
     * @return - {@link HttpClientResponse} with a byte array content type
     * @throws HttpClientException
     */
    public static HttpClientResponse<byte[]> byteResponse(CloseableHttpResponse httpResponse)
            throws HttpClientException {
        return byteResponse(httpResponse, true);
    }

    /**
     * Create an {@link HttpClientResponse} with a byte array content type from an
     * {@link HttpResponse}. If contentOnBadResponse is true, an attempt will be
     * made to retrieve the content even on a non 200 status code, otherwise the
     * content will be null in such an instance.
     * 
     * @param httpResponse
     * @param contentOnBadResponse
     * @return - {@link HttpClientResponse} with a byte array content type
     * @throws HttpClientException
     */
    public static HttpClientResponse<byte[]> byteResponse(CloseableHttpResponse httpResponse,
            boolean contentOnBadResponse) throws HttpClientException {

        HttpClientResponse<byte[]> response = new HttpClientResponse<>();
        try {

            response.populateGenericValues(httpResponse);
            if (httpResponse.getEntity() != null) {
                if (response.getStatusCode() == HttpStatus.SC_OK || contentOnBadResponse) {
                    byte[] data = IOUtils.toByteArray(httpResponse.getEntity().getContent());
                    response.setContent(data);
                } else {
                    EntityUtils.consume(httpResponse.getEntity());
                }
            }

            httpResponse.close();
        } catch (IOException e) {
            throw new HttpClientException("Unable to extract response body to byte array", e);
        }

        return response;
    }

    /**
     * Create an {@link HttpClientResponse} with a {@link String} content type from
     * an {@link HttpResponse}.
     * 
     * @param httpResponse
     * @return - {@link HttpClientResponse} with a {@link String} content type
     * @throws HttpClientException
     */
    public static HttpClientResponse<String> textResponse(CloseableHttpResponse httpResponse)
            throws HttpClientException {
        return textResponse(httpResponse, true);
    }

    /**
     * Create an {@link HttpClientResponse} with a {@link String} content type from
     * an {@link HttpResponse}. If contentOnBadResponse is true, an attempt will be
     * made to retrieve the content even on a non 200 status code, otherwise the
     * content will be null in such an instance.
     * 
     * @param httpResponse
     * @param contentOnBadResponse
     * @return - {@link HttpClientResponse} with a {@link String} content type
     * @throws HttpClientException
     */
    public static HttpClientResponse<String> textResponse(CloseableHttpResponse httpResponse,
            boolean contentOnBadResponse) throws HttpClientException {

        HttpClientResponse<String> response = new HttpClientResponse<>();
        try {

            response.populateGenericValues(httpResponse);

            if (httpResponse.getEntity() != null) {
                if (response.getStatusCode() == HttpStatus.SC_OK || contentOnBadResponse) {
                    String text = IOUtils.toString(httpResponse.getEntity().getContent());
                    response.setContent(text);
                } else {
                    EntityUtils.consume(httpResponse.getEntity());
                }
            }

            httpResponse.close();
        } catch (IOException e) {
            throw new HttpClientException("Unable to extract response body to string", e);
        }

        return response;
    }

    /**
     * Create an {@link HttpClientResponse} with a com.google.gson.JsonObject content type
     * from an {@link HttpResponse}.
     * 
     * @param httpResponse
     * @return - {@link HttpClientResponse} with a com.google.gson.JsonObject content type
     * @throws HttpClientException
     */
    public static HttpClientResponse<JsonObject> jsonResponse(CloseableHttpResponse httpResponse)
            throws HttpClientException {
        return jsonResponse(httpResponse, true);
    }

    /**
     * Create an {@link HttpClientResponse} with a com.google.gson.JsonObject content type
     * from an {@link HttpResponse}. If contentOnBadResponse is true, an attempt
     * will be made to retrieve the content even on a non 200 status code, otherwise
     * the content will be null in such an instance.
     * 
     * @param httpResponse
     * @param contentOnBadResponse
     * @return - {@link HttpClientResponse} with a com.google.gson.JsonObject content type
     * @throws HttpClientException
     */
    public static HttpClientResponse<JsonObject> jsonResponse(CloseableHttpResponse httpResponse,
            boolean contentOnBadResponse) throws HttpClientException {

        HttpClientResponse<JsonObject> response = new HttpClientResponse<>();
        try {
            response.populateGenericValues(httpResponse);

            try {
                if (httpResponse.getEntity() != null) {
                    if (response.getStatusCode() == HttpStatus.SC_OK || contentOnBadResponse) {
                        String sResponse = EntityUtils.toString(httpResponse.getEntity());
                        
                        if (sResponse.trim().startsWith("{")) {
    //                      JsonReader reader = new JsonReader(new InputStreamReader(httpResponse.getEntity().getContent()));
                            JsonElement jsonElement = null;
                            try{
                                jsonElement = new GalasaGson().fromJson(sResponse, JsonElement.class);
                            }catch(JsonSyntaxException jse){
                                logger.info("Unable to parse JSON from the following: " + sResponse);
                                throw jse;
                            }
                            if (jsonElement != null) {
                                JsonObject json = jsonElement.getAsJsonObject();
                                response.setContent(json);
                            }
                        }
                    } else {
                        EntityUtils.consume(httpResponse.getEntity());
                    }
                }
            } finally {
                httpResponse.close();
            }

        } catch (IOException e) {
            throw new HttpClientException("Unable to extract response body to JSON object", e);
        }

        return response;
    }

    /**
     * Create an {@link HttpClientResponse} with a {@link Document} content type
     * from an {@link HttpResponse}.
     * 
     * @param httpResponse
     * @return - {@link HttpClientResponse} with a {@link Document} content type
     * @throws HttpClientException
     */
    public static HttpClientResponse<Document> xmlResponse(CloseableHttpResponse httpResponse)
            throws HttpClientException {
        return xmlResponse(httpResponse, true);
    }

    /**
     * Create an {@link HttpClientResponse} with a {@link Document} content type
     * from an {@link HttpResponse}. If contentOnBadResponse is true, an attempt
     * will be made to retrieve the content even on a non 200 status code, otherwise
     * the content will be null in such an instance.
     * 
     * @param httpResponse
     * @param contentOnBadResponse
     * @return - {@link HttpClientResponse} with a {@link Document} content type
     * @throws HttpClientException
     */
    public static HttpClientResponse<Document> xmlResponse(CloseableHttpResponse httpResponse,
            boolean contentOnBadResponse) throws HttpClientException {

        HttpClientResponse<Document> response = new HttpClientResponse<>();
        try {
            response.populateGenericValues(httpResponse);

            if (httpResponse.getEntity() != null) {
                if (response.getStatusCode() == HttpStatus.SC_OK || contentOnBadResponse) {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document document = builder
                            .parse(new InputSource(new InputStreamReader(httpResponse.getEntity().getContent())));
                    response.setContent(document);
                } else {
                    EntityUtils.consume(httpResponse.getEntity());
                }
            }

            httpResponse.close();
        } catch (IOException e) {
            throw new HttpClientException("Unable to extract response body to XML document", e);
        } catch (SAXException e) {
            throw new HttpClientException("Unable to parse response body", e);
        } catch (ParserConfigurationException e) {
            throw new HttpClientException("Unable to create xml parser", e);
        }

        return response;
    }

    /**
     * Create an {@link HttpClientResponse} with an {@link Object} content type from
     * an {@link HttpResponse}. The object returned will be an instance of one of
     * the JAXB classes provided in responseTypes. If the response did not contain a
     * status code 200 (OK), the content will be null.
     * 
     * @param httpResponse
     * @param responseTypes
     * @return - {@link HttpClientResponse} with an {@link Object} content type
     * @throws HttpClientException
     */
    public static HttpClientResponse<Object> jaxbResponse(CloseableHttpResponse httpResponse, Class<?>... responseTypes)
            throws HttpClientException {
        return jaxbResponse(httpResponse, false, responseTypes);
    }

    /**
     * Create an {@link HttpClientResponse} with an {@link Object} content type from
     * an {@link HttpResponse}. The object returned will be an instance of one of
     * the JAXB classes provided in responseTypes. If contentOnBadResponse is true,
     * an attempt will be made to retrieve the content even on a non 200 status
     * code, otherwise the content will be null in such an instance.
     * 
     * @param httpResponse
     * @param contentOnBadResponse
     * @param responseTypes
     * @return - {@link HttpClientResponse} with an {@link Object} content type
     * @throws HttpClientException
     */
    public static HttpClientResponse<Object> jaxbResponse(CloseableHttpResponse httpResponse,
            boolean contentOnBadResponse, Class<?>... responseTypes) throws HttpClientException {

        HttpClientResponse<Object> response = new HttpClientResponse<>();
        try {

            response.populateGenericValues(httpResponse);

            if (httpResponse.getEntity() != null) {
                if (response.getStatusCode() == HttpStatus.SC_OK || contentOnBadResponse) {
                    JAXBContext context = JAXBContext.newInstance(responseTypes);
                    Object data = context.createUnmarshaller().unmarshal(httpResponse.getEntity().getContent());
                    response.setContent(data);
                } else {
                    EntityUtils.consume(httpResponse.getEntity());
                }
            }

            httpResponse.close();
        } catch (IOException | JAXBException e) {
            throw new HttpClientException("Unable to extract response body to JSON object", e);
        }

        return response;
    }
}
