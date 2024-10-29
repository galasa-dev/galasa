/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosrseapi.internal;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import com.google.gson.JsonObject;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.IHttpClient;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosrseapi.IRseapi;
import dev.galasa.zosrseapi.IRseapiResponse;
import dev.galasa.zosrseapi.RseapiException;
import dev.galasa.zosrseapi.RseapiManagerException;
import dev.galasa.zosrseapi.internal.properties.Https;
import dev.galasa.zosrseapi.internal.properties.RequestRetry;
import dev.galasa.zosrseapi.internal.properties.ServerCreds;
import dev.galasa.zosrseapi.internal.properties.ServerImage;
import dev.galasa.zosrseapi.internal.properties.ServerPort;

/**
 * Implementation of {@link IRseapi}
 *
 */
public class RseapiImpl implements IRseapi {
    
    private static final String LOG_BODY = "body: \n";
    
    private static final Log logger = LogFactory.getLog(RseapiImpl.class);

	private final RseapiManagerImpl rseapiManager;
	private String serverId;
    private IZosImage image;
    private IHttpClient httpClient;
    private String rseapiUrl;
    protected int requestRetry;

    private HashMap<String, String> commonHeaders = new HashMap<>();

	private static final String PATH_SERVERDETAILS = "/rseapi/api/v1/info/serverdetails";

    public RseapiImpl(RseapiManagerImpl rseapiManager, String serverId) throws RseapiException {
    	this.rseapiManager = rseapiManager;
        this.serverId = serverId;
        
        String imageId = null;
        try {
            imageId = ServerImage.get(this.serverId);
            this.image = rseapiManager.getZosManager().getUnmanagedImage(imageId);
        
        } catch(RseapiManagerException e) {
            throw new RseapiException("Unable to initialise RSE API server " + serverId, e);
        } catch (ZosManagerException e) {
            throw new RseapiException("Unable to initialise RSE API server " + serverId + " as z/OS image '" + imageId + "' is not defined", e);
        }
        
        initialize();
    }

    @Override
    public void setHeader(String key, String value) {
        this.commonHeaders.put(key, value);
    }

    @Override
    public void clearHeaders() {
        this.commonHeaders.clear();
        this.httpClient.clearCommonHeaders();
    }

    @Override
    public @NotNull IRseapiResponse get(String path, List<Integer> validStatusCodes, boolean convert) throws RseapiException {
        String method = RseapiRequestType.GET.name();
        if (validStatusCodes == null) {
            validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
        }
        RseapiResponseImpl rseapiResponse;
        try {
            addCommonHeaders();
            rseapiResponse = new RseapiResponseImpl(this.rseapiUrl, validPath(path));
            logger.trace(logRequest(method, rseapiResponse.getRequestUrl()));
            if (convert) {
                rseapiResponse.setHttpClientresponse(this.httpClient.getJson(validPath(path)));
            } else {
                rseapiResponse.setHttpClientresponse(this.httpClient.getFile(validPath(path)));
            }
            
            logger.trace(logResponse(rseapiResponse.getStatusLine(), method, rseapiResponse.getRequestUrl()));
            if (!validStatusCodes.contains(rseapiResponse.getStatusCode())) {
                throw new RseapiException(logBadStatusCode(rseapiResponse.getStatusCode()));
            }
        } catch (MalformedURLException | HttpClientException  e) {
            logger.error(e);
            throw new RseapiException(logBadRequest(method), e);
        }
        
        return rseapiResponse;
    }

	@Override
	public @NotNull IRseapiResponse putJson(String path, JsonObject requestBody, List<Integer> validStatusCodes) throws RseapiException {
	    String method = RseapiRequestType.PUT_JSON.getRequestType();
	    if (validStatusCodes == null) {
	        validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
	    }
	    RseapiResponseImpl rseapiResponse;
	    try {
	        addCommonHeaders();
	        rseapiResponse = new RseapiResponseImpl(this.rseapiUrl, validPath(path));
	        logger.trace(logRequest(method, rseapiResponse.getRequestUrl()));
	        logger.trace(LOG_BODY + requestBody);
	        rseapiResponse.setHttpClientresponse(this.httpClient.putJson(validPath(path), requestBody));
	        logger.trace(logResponse(rseapiResponse.getStatusLine(), method, rseapiResponse.getRequestUrl()));
	        if (!validStatusCodes.contains(rseapiResponse.getStatusCode())) {
	            throw new RseapiException(logBadStatusCode(rseapiResponse.getStatusCode()));
	        }
	    } catch (MalformedURLException | HttpClientException  e) {
	        logger.error(e);
	        throw new RseapiException(logBadRequest(method), e);
	    }
	    
	    return rseapiResponse;
	}

    @Override
	public @NotNull IRseapiResponse putText(String path, String requestBody, List<Integer> validStatusCodes) throws RseapiException {
	    String method = RseapiRequestType.PUT_TEXT.getRequestType();
	    if (validStatusCodes == null) {
	        validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
	    }
	    RseapiResponseImpl rseapiResponse;
	    try {
	        addCommonHeaders();
	        rseapiResponse = new RseapiResponseImpl(this.rseapiUrl, validPath(path));
	        logger.trace(logRequest(method, rseapiResponse.getRequestUrl()));
	        logger.trace(LOG_BODY + requestBody);
	        rseapiResponse.setHttpClientresponse(this.httpClient.putText(validPath(path), requestBody));
	        logger.trace(logResponse(rseapiResponse.getStatusLine(), method, rseapiResponse.getRequestUrl()));
	        if (!validStatusCodes.contains(rseapiResponse.getStatusCode())) {
	            throw new RseapiException(logBadStatusCode(rseapiResponse.getStatusCode()));
	        }
	    } catch (MalformedURLException | HttpClientException  e) {
	        logger.error(e);
	        throw new RseapiException(logBadRequest(method), e);
	    }
	    
	    return rseapiResponse;
	}

    @Override
	public @NotNull IRseapiResponse putBinary(String path, byte[] requestBody, List<Integer> validStatusCodes) throws RseapiException {
	    String method = RseapiRequestType.PUT_BINARY.getRequestType();
	    if (validStatusCodes == null) {
	        validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
	    }
	    RseapiResponseImpl rseapiResponse;
	    try {
	        addCommonHeaders();
	        rseapiResponse = new RseapiResponseImpl(this.rseapiUrl, validPath(path));
	        logger.trace(logRequest(method, rseapiResponse.getRequestUrl()));
	        rseapiResponse.setHttpClientresponse(this.httpClient.putBinary(validPath(path), requestBody));
	        logger.trace(logResponse(rseapiResponse.getStatusLine(), method, rseapiResponse.getRequestUrl()));
	        if (!validStatusCodes.contains(rseapiResponse.getStatusCode())) {
	            throw new RseapiException(logBadStatusCode(rseapiResponse.getStatusCode()));
	        }
	    } catch (MalformedURLException | HttpClientException  e) {
	        logger.error(e);
	        throw new RseapiException(logBadRequest(method), e);
	    }
	    
	    return rseapiResponse;
	}

	@Override
    public @NotNull IRseapiResponse postJson(String path, JsonObject requestBody, List<Integer> validStatusCodes) throws RseapiException {
        String method = RseapiRequestType.POST_JSON.getRequestType();
        if (validStatusCodes == null) {
            validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
        }
        RseapiResponseImpl rseapiResponse;
        try {
            addCommonHeaders();
            rseapiResponse = new RseapiResponseImpl(this.rseapiUrl, validPath(path));
            logger.trace(logRequest(method, rseapiResponse.getRequestUrl()));
            logger.trace(LOG_BODY + requestBody);
            rseapiResponse.setHttpClientresponse(this.httpClient.postJson(validPath(path), requestBody));
            logger.trace(logResponse(rseapiResponse.getStatusLine(), method, rseapiResponse.getRequestUrl()));
            if (!validStatusCodes.contains(rseapiResponse.getStatusCode())) {
                throw new RseapiException(logBadStatusCode(rseapiResponse.getStatusCode()));
            }
        } catch (MalformedURLException | HttpClientException  e) {
            logger.error(e);
            throw new RseapiException(logBadRequest(method), e);
        }
        
        return rseapiResponse;
    }

	@Override
    public @NotNull IRseapiResponse post(String path, List<Integer> validStatusCodes) throws RseapiException {
        String method = RseapiRequestType.POST_JSON.getRequestType();
        if (validStatusCodes == null) {
            validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
        }
        RseapiResponseImpl rseapiResponse;
        try {
            addCommonHeaders();
            rseapiResponse = new RseapiResponseImpl(this.rseapiUrl, validPath(path));
            logger.trace(logRequest(method, rseapiResponse.getRequestUrl()));
            rseapiResponse.setHttpClientresponse(this.httpClient.postJson(validPath(path), null));
            logger.trace(logResponse(rseapiResponse.getStatusLine(), method, rseapiResponse.getRequestUrl()));
            if (!validStatusCodes.contains(rseapiResponse.getStatusCode())) {
                throw new RseapiException(logBadStatusCode(rseapiResponse.getStatusCode()));
            }
        } catch (MalformedURLException | HttpClientException  e) {
            logger.error(e);
            throw new RseapiException(logBadRequest(method), e);
        }
        
        return rseapiResponse;
    }

    @Override
    public @NotNull IRseapiResponse delete(String path, List<Integer> validStatusCodes) throws RseapiException {
        String method = RseapiRequestType.DELETE.name();
        if (validStatusCodes == null) {
            validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
        }
        RseapiResponseImpl rseapiResponse;
        try {
            addCommonHeaders();
            rseapiResponse = new RseapiResponseImpl(this.rseapiUrl, validPath(path));
            logger.trace(logRequest(method, rseapiResponse.getRequestUrl()));
            rseapiResponse.setHttpClientresponse(this.httpClient.deleteJson(validPath(path)));
            logger.trace(logResponse(rseapiResponse.getStatusLine(), method, rseapiResponse.getRequestUrl()));
            if (!validStatusCodes.contains(rseapiResponse.getStatusCode())) {
                throw new RseapiException(logBadStatusCode(rseapiResponse.getStatusCode()));
            }
        } catch (MalformedURLException | HttpClientException  e) {
            throw new RseapiException(logBadRequest(method), e);
        }
        
        return rseapiResponse;
    }

    @Override
	public @NotNull JsonObject serverInfo() throws RseapiException {
        return get(PATH_SERVERDETAILS, null, true).getJsonContent();
	}

	@Override
    public IZosImage getImage() {
        return this.image;
    }

    @Override
    public String toString() {
        return this.image.getImageID() + " " + this.rseapiUrl;
    }

    protected String validPath(String path) {
        return path.startsWith("/") ? path : "/" + path;
    }

    protected void initialize() throws RseapiException {
        
    	String rseapiHostname;
        try {
            rseapiHostname = image.getDefaultHostname();
        } catch (ZosManagerException e) {
            throw new RseapiException(e);
        }
        int rseapiPort;
        try {
            rseapiPort = ServerPort.get(image.getImageID());
        } catch (RseapiManagerException e) {
            throw new RseapiException(e);
        }
        String scheme = "http";
        try {
            if (Https.get(image.getImageID())) {
                scheme = "https";
            }
        } catch (RseapiManagerException e) {
            throw new RseapiException(e);
        }
        this.rseapiUrl = scheme + "://" + rseapiHostname + ":" + rseapiPort;

        this.httpClient = this.rseapiManager.getHttpManager().newHttpClient();
        
        try {
            ICredentials creds = null;
            String credsId = ServerCreds.get(this.serverId);
            if (credsId != null) {
                try {
                    creds = this.rseapiManager.getFramework().getCredentialsService().getCredentials(credsId);
                } catch (CredentialsException e) {
                    throw new RseapiException("Problem accessing credentials store", e);
                }
            }
            
            if (creds == null) {
                creds = image.getDefaultCredentials();
            }
            this.httpClient.setURI(new URI(this.rseapiUrl));
            if (creds instanceof ICredentialsUsernamePassword) {
                this.httpClient.setAuthorisation(((ICredentialsUsernamePassword) creds).getUsername(), ((ICredentialsUsernamePassword) creds).getPassword());
            }
            this.httpClient.setTrustingSSLContext();
            this.httpClient.build();
        } catch (HttpClientException | ZosManagerException | URISyntaxException e) {
            throw new RseapiException("Unable to create HTTP Client", e);
        }
        
        try {
            this.requestRetry = RequestRetry.get(this.serverId);
        } catch (ZosManagerException e) {
            throw new RseapiException(e);
        }
    }

    protected void addCommonHeaders() {
        for (Entry<String, String> entry : this.commonHeaders.entrySet()) {
            logger.trace("Adding HTTP header: " + entry.getKey() + ": " + entry.getValue());
            this.httpClient.addCommonHeader(entry.getKey(), entry.getValue());
        }
        
    }

    protected String logRequest(String method, URL requestUrl) {
        return "Request: " + method + " " + requestUrl;
    }

    protected String logResponse(String statusLine, String method, URL requestUrl) {
        return "Response: " + statusLine + " - " + method + " " + requestUrl;
    }

    protected String logBadStatusCode(int statusCode) {
        return "Unexpected HTTP status code: " + statusCode;
    }

    protected String logBadRequest(String method) {
        return "Problem with " + method + " to RSE API server";
    }

    protected int getRequestRetry() {
        return this.requestRetry;
    }    
}
