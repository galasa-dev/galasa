/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.http.internal;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.util.Timeout;

import com.google.gson.JsonObject;

import dev.galasa.common.SSLTLSContextNameSelector;
import dev.galasa.http.ContentType;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.HttpFileResponse;
import dev.galasa.http.IHttpClient;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.XmlType;

public class HttpClientImpl implements IHttpClient {

    

    private CloseableHttpClient httpClient;
    protected URI               host                 = null;

    private final List<Header>  commonHeaders        = new ArrayList<>();

    private final int           timeout;

    private SSLContext          sslContext;
    private HostnameVerifier    hostnameVerifier     = NoopHostnameVerifier.INSTANCE;
    private CredentialsProvider credentialsProvider  = new BasicCredentialsProvider();
    private HttpClientContext   httpContext          = null;
    private Set<Integer>        okResponseCodes      = new HashSet<>();

    private Log                 logger;

    private SSLTLSContextNameSelector nameSelector = new SSLTLSContextNameSelector();

    public HttpClientImpl(int timeout, Log log) {
        this.timeout = timeout;
        this.logger = log;
    }

    @Override
    public HttpClientResponse<Object> getJaxb(String url, Class<?>... responseTypes) throws HttpClientException {

        HttpClientRequest request = HttpClientRequest.newGetRequest(buildUri(url, null).toString(),
                new ContentType[] { ContentType.APPLICATION_XML });

        return executeJaxbRequest(request, responseTypes);
    }

    @Override
    public HttpClientResponse<Object> putJaxb(String url, Object jaxbObject, Class<?>... responseTypes)
            throws HttpClientException {

        HttpClientRequest request = HttpClientRequest.newPutRequest(buildUri(url,null).toString(),
                new ContentType[] { ContentType.APPLICATION_XML }, ContentType.APPLICATION_XML);
        request.setJAXBBody(jaxbObject);

        return executeJaxbRequest(request, responseTypes);
    }

    @Override
    public HttpClientResponse<Object> postJaxb(String url, Object jaxbObject, Class<?>... responseTypes)
            throws HttpClientException {

        HttpClientRequest request = HttpClientRequest.newPostRequest(buildUri(url, null).toString(),
                new ContentType[] { ContentType.APPLICATION_XML }, ContentType.APPLICATION_XML);
        request.setJAXBBody(jaxbObject);

        return executeJaxbRequest(request, responseTypes);
    }

    @Override
    public HttpClientResponse<Object> deleteJaxb(String url, Class<?>... responseTypes) throws HttpClientException {

        HttpClientRequest request = HttpClientRequest.newDeleteRequest(buildUri(url, null).toString(),
                new ContentType[] { ContentType.APPLICATION_XML });

        return executeJaxbRequest(request, responseTypes);
    }

    private HttpClientResponse<Object> executeJaxbRequest(HttpClientRequest request, Class<?>... responseTypes)
            throws HttpClientException {

        return HttpClientResponse.jaxbResponse(execute(request.buildRequest()), responseTypes);
    }

    @Override
    public HttpClientResponse<JsonObject> getJson(String url) throws HttpClientException {
        HttpClientRequest request = HttpClientRequest.newGetRequest(buildUri(url, null).toString(),
                new ContentType[] { ContentType.APPLICATION_JSON });
        return executeJsonRequest(request);
    }

    @Override
    public HttpClientResponse<JsonObject> putJson(String url, JsonObject json) throws HttpClientException {

        HttpClientRequest request = HttpClientRequest.newPutRequest(buildUri(url, null).toString(),
                new ContentType[] { ContentType.APPLICATION_JSON }, ContentType.APPLICATION_JSON);
        request.setJSONBody(json);

        return executeJsonRequest(request);
    }

    @Override
    public HttpClientResponse<JsonObject> postJson(String url, JsonObject json) throws HttpClientException {

        HttpClientRequest request = HttpClientRequest.newPostRequest(buildUri(url, null).toString(),
                new ContentType[] { ContentType.APPLICATION_JSON }, ContentType.APPLICATION_JSON);
        request.setJSONBody(json);

        return executeJsonRequest(request);
    }

    @Override
    public HttpClientResponse<JsonObject> patchJson(String url, JsonObject json) throws HttpClientException {

        HttpClientRequest request = HttpClientRequest.newPatchRequest(buildUri(url, null).toString(),
                new ContentType[] { ContentType.APPLICATION_JSON }, ContentType.APPLICATION_JSON);
        request.setJSONBody(json);

        return executeJsonRequest(request);
    }

    @Override
    public HttpClientResponse<JsonObject> deleteJson(String url) throws HttpClientException {

        HttpClientRequest request = HttpClientRequest.newDeleteRequest(buildUri(url, null).toString(),
                new ContentType[] { ContentType.APPLICATION_JSON });

        return executeJsonRequest(request);
    }

    @Override
    public HttpClientResponse<JsonObject> deleteJson(String url, JsonObject json) throws HttpClientException {

        HttpClientRequest request = HttpClientRequest.newDeleteRequest(buildUri(url, null).toString(),
                new ContentType[] { ContentType.APPLICATION_JSON }, ContentType.APPLICATION_JSON);
        request.setJSONBody(json);

        return executeJsonRequest(request);
    }

    private HttpClientResponse<JsonObject> executeJsonRequest(HttpClientRequest request) throws HttpClientException {

        return HttpClientResponse.jsonResponse(execute(request.buildRequest()));
    }


    @Override
    public HttpClientResponse<String> getText(String url) throws HttpClientException {

        HttpClientRequest request = HttpClientRequest.newGetRequest(buildUri(url, null).toString(),
                new ContentType[] { ContentType.TEXT_PLAIN });

        return executeTextRequest(request);
    }

    @Override
    public HttpClientResponse<String> putXML(String url, String xml) throws HttpClientException {

        HttpClientRequest request = HttpClientRequest.newPutRequest(buildUri(url, null).toString(),
                new ContentType[] { ContentType.APPLICATION_XML }, ContentType.APPLICATION_XML);
        request.setBody(xml);

        return executeTextRequest(request);
    }

    @Override
    public HttpClientResponse<String> postXML(String url, String xml) throws HttpClientException {

        HttpClientRequest request = HttpClientRequest.newPostRequest(buildUri(url, null).toString(),
                new ContentType[] { ContentType.APPLICATION_XML }, ContentType.APPLICATION_XML);
        request.setBody(xml);

        return executeTextRequest(request);
    }

    @Override
    public HttpClientResponse<String> putSOAP(String url, String xml) throws HttpClientException {

        HttpClientRequest request = HttpClientRequest.newPutRequest(buildUri(url, null).toString(),
                new ContentType[] { ContentType.SOAP_XML }, ContentType.SOAP_XML);
        request.setBody(xml);

        return executeTextRequest(request);
    }

    @Override
    public HttpClientResponse<String> postSOAP(String url, String xml) throws HttpClientException {

        HttpClientRequest request = HttpClientRequest.newPostRequest(buildUri(url, null).toString(),
                new ContentType[] { ContentType.SOAP_XML }, ContentType.SOAP_XML);
        request.setBody(xml);

        return executeTextRequest(request);
    }

    @Override
    public HttpClientResponse<String> putText(String url, String text) throws HttpClientException {

        HttpClientRequest request = HttpClientRequest.newPutRequest(buildUri(url, null).toString(),
                new ContentType[] { ContentType.TEXT_PLAIN }, ContentType.TEXT_PLAIN);
        request.setBody(text);

        return executeTextRequest(request);
    }

    @Override
    public HttpClientResponse<String> postText(String url, String text) throws HttpClientException {

        HttpClientRequest request = HttpClientRequest.newPostRequest(buildUri(url, null).toString(),
                new ContentType[] { ContentType.TEXT_PLAIN }, ContentType.TEXT_PLAIN);
        request.setBody(text);

        return executeTextRequest(request);
    }

    @Override
    public HttpClientResponse<String> deleteText(String url) throws HttpClientException {

        HttpClientRequest request = HttpClientRequest.newDeleteRequest(buildUri(url, null).toString(),
                new ContentType[] { ContentType.TEXT_PLAIN });

        return executeTextRequest(request);
    }

    private HttpClientResponse<String> executeTextRequest(HttpClientRequest request) throws HttpClientException {

        return HttpClientResponse.textResponse(execute(request.buildRequest()));
    }


    @Override
    public HttpClientResponse<byte[]> putBinary(String url, byte[] binary) throws HttpClientException {       
        HttpClientRequest request = HttpClientRequest.newPutRequest(buildUri(url, null).toString(),
                new ContentType[] { ContentType.TEXT_PLAIN }, ContentType.TEXT_PLAIN);
        request.setBody(binary);
        return executeByteRequest(request);
    }

    @Override
    public HttpClientResponse<byte[]> getBinary(String url, byte[] binary) throws HttpClientException {       
        HttpClientRequest request = HttpClientRequest.newGetRequest(buildUri(url, null).toString(),
                new ContentType[] { ContentType.TEXT_PLAIN });
        request.setBody(binary);
        return executeByteRequest(request);
    }

    @Override
    public HttpClientResponse<byte[]> postBinary(String url, byte[] binary) throws HttpClientException {       
        HttpClientRequest request = HttpClientRequest.newPostRequest(buildUri(url, null).toString(),
                new ContentType[] { ContentType.TEXT_PLAIN }, ContentType.TEXT_PLAIN);
        request.setBody(binary);
        return executeByteRequest(request);
    }

    @Override
    public HttpClientResponse<byte[]> deleteBinary(String url, byte[] binary) throws HttpClientException {       
        HttpClientRequest request = HttpClientRequest.newDeleteRequest(buildUri(url, null).toString(),
                new ContentType[] { ContentType.TEXT_PLAIN });
        request.setBody(binary);
        return executeByteRequest(request);
    }

    public HttpClientResponse<byte[]> executeByteRequest(HttpClientRequest request) throws HttpClientException {

        return HttpClientResponse.byteResponse(execute(request.buildRequest()));
    }

    @Override
    public HttpFileResponse getFileStream(String path) throws HttpClientException {
        return getFileStream(path, ContentType.APPLICATION_OCTET_STREAM, ContentType.APPLICATION_X_TAR);
    }

    @Override
    public HttpFileResponse getFileStream(String path, ContentType... contentTypes) throws HttpClientException {
        try {
            HttpClientRequest request = HttpClientRequest.newGetRequest(buildUri(path, null).toString(),
                    contentTypes);

            ClassicHttpResponse response = execute(request.buildRequest());
            return new HttpFileResponse(response);
        } catch (HttpClientException e) {
            logger.error("Could not download file from specified path: " + path, e);
            throw new HttpClientException("Failed to get file stream", e);
        }
    }

    @Override
    public org.apache.http.client.methods.CloseableHttpResponse getFile(String path) throws HttpClientException {
        try {
            HttpClientRequest request = HttpClientRequest.newGetRequest(buildUri(path, null).toString(),
                    new ContentType[] { ContentType.APPLICATION_OCTET_STREAM, ContentType.APPLICATION_X_TAR });

            ClassicHttpResponse httpclient5Response = execute(request.buildRequest());
            return new HttpClient4ResponseAdapter(httpclient5Response);
        } catch (HttpClientException e) {
            logger.error("Could not download file from specified path: " + path, e);
            throw new HttpClientException("Failed to get file", e);
        }
    }

    @Override
    public org.apache.http.client.methods.CloseableHttpResponse getFile(String path, ContentType... contentTypes) throws HttpClientException {
        try {
            HttpClientRequest request = HttpClientRequest.newGetRequest(buildUri(path, null).toString(),
                    contentTypes);

            ClassicHttpResponse httpclient5Response = execute(request.buildRequest());
            return new HttpClient4ResponseAdapter(httpclient5Response);
        } catch (HttpClientException e) {
            logger.error("Could not download file from specified path: " + path, e);
            throw new HttpClientException("Failed to get file", e);
        }
    }

    @Override
    public void setURI(URI host) {
        this.host = host;
    }

    /**
     * Get the SSL context used by this client
     * 
     * @return the {@link SSLContext} or null if there is none
     */
    public SSLContext getSSLContext() {
        return sslContext;
    }

    /**
     * Add a response code for the execute to ignore and treat as OK
     * 
     * @param responseCode
     */
    public void addOkResponseCode(int responseCode) {
        okResponseCodes.add(responseCode);
    }

    /**
     * Set the SSL Context to a Trust All context
     * 
     * @return the updated client
     * @throws HttpClientException
     */
    public IHttpClient setTrustingSSLContext() throws HttpClientException {
        try {
            String contextName = nameSelector.getSelectedSSLContextName();
            SSLContext sslContext = SSLContext.getInstance(contextName);
            sslContext.init(null, new TrustManager[] { new VeryTrustingTrustManager() }, new SecureRandom());
            setSSLContext(sslContext);
        } catch (GeneralSecurityException e) {
            throw new HttpClientException("Error attempting to create SSL context", e);
        }
        return this;
    }


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
    public IHttpClient setupClientAuth(KeyStore clientKeyStore, KeyStore serverKeyStore, String alias, String password)
            throws HttpClientException {
        try {
            // Create the Key Manager Factory for client authentication
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(clientKeyStore, password.toCharArray());
            
            // Create the Trust Manager Factory for server certificate validation
            // This properly validates the certificate chain using the CA certificates in the KeyStore
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(serverKeyStore);
            TrustManager[] trustManagers = tmf.getTrustManagers();
            
            // Create the SSL Context
            String contextName = nameSelector.getSelectedSSLContextName();
            SSLContext sslContext = SSLContext.getInstance(contextName);
            sslContext.init(kmf.getKeyManagers(), trustManagers, null);
            setSSLContext(sslContext);
        } catch (GeneralSecurityException e) {
            throw new HttpClientException("Error attempting to create SSL context", e);
        }
        return this;
    }

    /**
     * Set up Client Authentication SSL Context using a single KeyStore
     * for both client certificates and server trust.
     *
     * This is a convenience method that delegates to the full setupClientAuth method,
     * using the same KeyStore for both client and server authentication. It automatically
     * finds the first certificate alias in the KeyStore to use for server verification.
     *
     * @param keyStore KeyStore containing both client cert and trusted CAs
     * @param password KeyStore password
     * @return the updated client
     * @throws HttpClientException if SSL setup fails
     */
    public IHttpClient setupClientAuth(KeyStore keyStore, String password)
            throws HttpClientException {
        // Use the same KeyStore for both client authentication and server trust
        // The TrustManagerFactory will automatically use all CA certificates in the KeyStore
        // No need to specify an alias - it validates the entire certificate chain
        return setupClientAuth(keyStore, keyStore, null, password);
    }

    /**
     * Set the SSL Context
     * 
     * @param sslContext
     * @return the updated client
     */
    public IHttpClient setSSLContext(SSLContext sslContext) {

        this.sslContext = sslContext;

        return this;
    }

    /**
     * Set the hostname verifier
     * 
     * 
     * @param hostnameVerifier
     * @return the updated client
     */
    public IHttpClient setHostnameVerifier(HostnameVerifier hostnameVerifier) {

        this.hostnameVerifier = hostnameVerifier;

        return this;
    }

    /**
     * Set the hostname verifier to a no-op verifier
     * 
     * @return the updated client
     */
    public IHttpClient setNoopHostnameVerifier() {

        this.hostnameVerifier = NoopHostnameVerifier.INSTANCE;

        return this;
    }

    /**
     * Get the username set for this client
     *
     * @return the username
     */
    public String getUsername() {
        org.apache.hc.client5.http.auth.Credentials creds =
            ((BasicCredentialsProvider)credentialsProvider).getCredentials(new AuthScope(null, -1), null);
        if (creds != null && creds.getUserPrincipal() != null) {
            return creds.getUserPrincipal().getName();
        }
        return null;
    }

    /**
     * Get the username set for this client for a specific scope
     *
     * @param scope
     * @return the username
     */
    public String getUsername(URI scope) {
        org.apache.hc.client5.http.auth.Credentials creds =
            ((BasicCredentialsProvider)credentialsProvider).getCredentials(
                new AuthScope(scope.getHost(), scope.getPort()), null);
        if (creds != null) {
            return creds.getUserPrincipal().getName();
        }
        return null;
    }

    /**
     * Set the username and password for all scopes
     *
     * @param username
     * @param password
     * @return the updated client
     */
    public IHttpClient setAuthorisation(String username, String password) {
        // Create a new credentials provider to clear existing credentials
        credentialsProvider = new BasicCredentialsProvider();
        ((BasicCredentialsProvider)credentialsProvider).setCredentials(new AuthScope(null, -1),
                new UsernamePasswordCredentials(username, password.toCharArray()));
        return this;
    }

    /**
     * Set the username and password for a specific scope
     *
     * @param username
     * @param password
     * @param scope
     * @return the updated client
     */
    public IHttpClient setAuthorisation(String username, String password, URI scope) {
        ((BasicCredentialsProvider)credentialsProvider).setCredentials(
                new AuthScope(scope.getHost(), scope.getPort()),
                new UsernamePasswordCredentials(username, password.toCharArray()));
        return this;
    }

    /**
     * Build the client
     * 
     * @return the built client
     */
    public IHttpClient build() {
        RequestConfig.Builder requestBuilder = RequestConfig.custom();
        HttpClientBuilder builder = HttpClientBuilder.create();
        requestBuilder.setCookieSpec(StandardCookieSpec.STRICT);
        builder.setDefaultCredentialsProvider(credentialsProvider);
        builder.setDefaultHeaders(commonHeaders);

        if (timeout > 0) {
            Timeout timeoutValue = Timeout.ofMilliseconds(timeout);
            requestBuilder.setConnectionRequestTimeout(timeoutValue)
                          .setResponseTimeout(timeoutValue);
        }

        if (sslContext != null) {
            TlsSocketStrategy tlsStrategy = new DefaultClientTlsStrategy(sslContext, hostnameVerifier);
            HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                    .setTlsSocketStrategy(tlsStrategy)
                    .build();
            builder.setConnectionManager(cm);
        }
        builder.setDefaultRequestConfig(requestBuilder.build());
        httpClient = builder.build();

        return this;
    }

    private void addHeaders(ClassicHttpRequest message, ContentType contentType, ContentType[] acceptTypes) {

        if (contentType != null) {
            if(contentType.getC() != null) {
                message.addHeader(HttpHeaders.CONTENT_TYPE, contentType.getC().getMimeType());
            } else {
                message.addHeader(HttpHeaders.CONTENT_TYPE, contentType.getMimeType());
            }

        }

        if (acceptTypes.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (ContentType acceptType : acceptTypes) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(acceptType.getMimeType());
            }

            message.addHeader(HttpHeaders.ACCEPT, sb.toString());
        }

        for (Header header : commonHeaders) {
            message.addHeader(header);
        }

    }

    private byte[] execute(ClassicHttpRequest request, boolean retry) throws HttpClientException {

        while (true) {
            ClassicHttpResponse response = null;
            try {
                this.build();
                response = httpClient.executeOpen(null, request, httpContext);
                int statusCode = response.getCode();
                if (statusCode != HttpStatus.SC_OK
                        && statusCode != HttpStatus.SC_CREATED
                        && statusCode != HttpStatus.SC_MOVED_TEMPORARILY
                        && !okResponseCodes.contains(statusCode)) {
                    String message = "HTTP " + request.getMethod() + " to " + request.getUri().toASCIIString()
                            + " failed with " + statusCode + ": '" + response.getReasonPhrase() + "'";

                    if (retry && statusCode != HttpStatus.SC_UNAUTHORIZED) {
                        logger.warn(message + ", retrying");
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e1) {
                            Thread.currentThread().interrupt();
                            throw new HttpClientException("Galasa HTTP Client retry failed due to interruption", e1);
                        }
                        continue;
                    }
                    throw new HttpClientException(message);
                }

                HttpEntity entity = response.getEntity();

                return IOUtils.toByteArray(entity.getContent());

            } catch (Exception e) {
                throw new HttpClientException(e);
            } finally {

                if (response != null) {
                    try {
                        response.close();
                    } catch (IOException e) {
                        logger.error("Exception received when trying to close an http response from "
                                + request.getRequestUri(), e);
                    }
                }
            }
        }
    }

    private URI buildUri(String path, Map<String, String> queryParams) throws HttpClientException {

        if (queryParams == null) {
            queryParams = new HashMap<>();
        }

        // Create a multi-valued map since we can have more than one value for each
        // param in the path
        Map<String, List<String>> multiMap = new HashMap<>();
        for (Entry<String, String> entry : queryParams.entrySet()) {
            List<String> list = new ArrayList<>();
            list.add(entry.getValue());
            multiMap.put(entry.getKey(), list);
        }

        Pattern p = Pattern.compile("^(.*)\\?((?:.+=.*&?)+)$", Pattern.MULTILINE);
        Matcher m = p.matcher(path);
        if (m.find()) {
            path = m.group(1);

            String[] pairs = m.group(2).split("&");
            for (String pair : pairs) {
                String[] parts = pair.split("=");
                if (parts.length != 2) {
                    throw new HttpClientException("Illegal query parameter found: '" + pair + "'");
                }

                String param = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                if (multiMap.containsKey(param)) {
                    multiMap.get(param).add(value);
                } else {
                    List<String> list = new ArrayList<>();
                    list.add(value);
                    multiMap.put(param, list);
                }
            }
        }

        URIBuilder ub = new URIBuilder(host);
        appendPath(ub, path);

        // Iterate through the multi-value map to add all the parameters
        for (Entry<String, List<String>> entry : multiMap.entrySet()) {
            for (String value : entry.getValue()) {
                ub.addParameter(entry.getKey(), value);
            }
        }

        try {
            return ub.build();
        } catch (URISyntaxException e) {
            throw new HttpClientException("Cannot construct URI using path: '" + path + "'", e);
        }
    }

    private void appendPath(URIBuilder ub, String path) {

        if (path.isEmpty()) {
            return;
        }

        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        String commonPath = ub.getPath();

        if (commonPath != null && !path.toLowerCase().startsWith(commonPath.toLowerCase())) {
            path = commonPath + path;
        }
        ub.setPath(path);
    }

    private Object unmarshall(byte[] content, Class<?>[] jaxbClasses) throws HttpClientException {

        try {
            if (jaxbClasses != null && jaxbClasses.length > 0) {
                JAXBContext ctx = JAXBContext.newInstance(jaxbClasses);
                return ctx.createUnmarshaller().unmarshal(new ByteArrayInputStream(content));
            }
        } catch (JAXBException e) {
            throw new HttpClientException("Issue unmarshalling response", e);
        }

        return new String(content);

    }

    private byte[] marshall(Object object, Class<?>[] jaxbClasses) throws HttpClientException {

        if (object == null) {
            return new byte[0];
        }

        if (object.getClass().isAnnotationPresent(XmlType.class) && jaxbClasses != null && jaxbClasses.length > 0) {
            try {
                JAXBContext ctx = JAXBContext.newInstance(jaxbClasses);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ctx.createMarshaller().marshal(object, os);

                return os.toByteArray();
            } catch (JAXBException e) {
                throw new HttpClientException(e);
            }
        } else {
            return ((String) object).getBytes();
        }
    }

    @Override
    public Object post(String path, Map<String, String> queryParams, ContentType contentType, Object data,
            ContentType[] acceptTypes, Class<?>[] jaxbClasses, boolean retry) throws HttpClientException {

        byte[] dataBytes = marshall(data, jaxbClasses);

        HttpPost post = new HttpPost(buildUri(path, queryParams));
        post.setEntity(new ByteArrayEntity(dataBytes, null));
        addHeaders(post, contentType, acceptTypes);

        byte[] response = execute(post, retry);

        return unmarshall(response, jaxbClasses);

    }

    @Override
    public Object postForm(String path, Map<String, String> queryParams, HashMap<String, String> fields,
            ContentType[] acceptTypes, Class<?>[] jaxbClasses, boolean retry) throws HttpClientException {

        HttpPost post = new HttpPost(buildUri(path, queryParams));
        addHeaders(post, ContentType.APPLICATION_FORM_URLENCODED, acceptTypes);

        List<NameValuePair> nvps = new ArrayList<>();
        for (Entry<String, String> field : fields.entrySet()) {
            nvps.add(new BasicNameValuePair(field.getKey(), field.getValue()));
        }
        post.setEntity(new UrlEncodedFormEntity(nvps, StandardCharsets.UTF_8));

        byte[] response = execute(post, retry);

        return unmarshall(response, null);
    }

    public void putFile(String path, InputStream file) {    
        try {
            BufferedInputStream in = new BufferedInputStream(file);
            ClassicHttpResponse response = putStream(path, null, ContentType.APPLICATION_X_TAR, in, new ContentType[] {
                    ContentType.APPLICATION_XML, ContentType.APPLICATION_JSON, ContentType.TEXT_PLAIN }, null, false);
            in.close();
            response.close();
            file.close();
        } catch (HttpClientException | IOException e) {
            logger.error("Failed to stream file.", e);
        }
    }

    public ClassicHttpResponse putStream(String path, Map<String, String> queryParams, ContentType contentType, Object data,
            ContentType[] acceptTypes, Class<?>[] jaxbClasses, boolean retry) throws HttpClientException {

        HttpPut put = new HttpPut(buildUri(path, queryParams));

        HttpClientContext context = HttpClientContext.create();
        RequestConfig config = RequestConfig.custom().setExpectContinueEnabled(true).build();
        context.setRequestConfig(config);

        if(data instanceof InputStream) {
            InputStreamEntity entity;
            try {
                entity = new InputStreamEntity((InputStream) data, org.apache.hc.core5.http.ContentType.APPLICATION_OCTET_STREAM);
                put.setEntity(entity);
                addHeaders(put, contentType, acceptTypes);
                return httpClient.executeOpen(null, put, context);
            } catch (IOException e) {
                logger.error("IO error with input stream", e);
                throw new HttpClientException(e);
            }
        } else {
            throw new HttpClientException("Data was not an expected object type");
        }
    }

    @Override
    public void addCommonHeader(String name, String value) {
        List<Header> toRemove = new ArrayList<>();
        commonHeaders.forEach(header -> {
            if (header.getName().equals(name)) {
                toRemove.add(header);
            }
        });
        commonHeaders.removeAll(toRemove);
        commonHeaders.add(new BasicHeader(name, value));
    }

    @Override
    public void clearCommonHeaders() {
        commonHeaders.clear();
    }

    @Override
    public HttpClientResponse<String> head(String url) throws HttpClientException {
        HttpClientRequest request = HttpClientRequest.newHeadRequest(buildUri(url, null).toString());
        return HttpClientResponse.textResponse(execute(request.buildRequest()));
    }

    private ClassicHttpResponse execute(ClassicHttpRequest request) throws HttpClientException {
        this.build();
        try {
            return httpClient.executeOpen(null, request, httpContext);
        } catch (IOException e) {
            throw new HttpClientException("Error executing http request", e);
        }
    }

    @Override
    public void close() {
        if (this.httpClient == null) {
            return;
        }

        try {
            httpClient.close();
        } catch (IOException e) {
        }

    }

}
