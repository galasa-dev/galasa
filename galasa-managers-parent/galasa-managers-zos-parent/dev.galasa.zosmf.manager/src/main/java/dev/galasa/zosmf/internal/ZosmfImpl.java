/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosmf.internal;

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
import dev.galasa.zosmf.IZosmf;
import dev.galasa.zosmf.IZosmfResponse;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.ZosmfManagerException;
import dev.galasa.zosmf.internal.properties.Https;
import dev.galasa.zosmf.internal.properties.RequestRetry;
import dev.galasa.zosmf.internal.properties.ServerCreds;
import dev.galasa.zosmf.internal.properties.ServerImage;
import dev.galasa.zosmf.internal.properties.ServerPort;

/**
 * Implementation of {@link IZosmf}
 *
 */
public class ZosmfImpl implements IZosmf {
    
    private static final String LOG_BODY = "body: \n";
    
    private static final Log logger = LogFactory.getLog(ZosmfImpl.class);

    private final ZosmfManagerImpl zosmfManager;
    private final String serverId;
    private IZosImage image;
    private IHttpClient httpClient;
    private String zosmfUrl;
    protected int requestRetry;

    private HashMap<String, String> commonHeaders = new HashMap<>();

	private static final String PATH_SERVERDETAILS = "/zosmf/info";

    public ZosmfImpl(ZosmfManagerImpl zosmfManager, String serverId) throws ZosmfException {
        this.zosmfManager = zosmfManager;
        this.serverId     = serverId;
        
        String imageId = null;
        try {
            imageId = ServerImage.get(this.serverId);
            this.image = zosmfManager.getZosManager().getUnmanagedImage(imageId);
        
        } catch(ZosmfManagerException e) {
            throw new ZosmfException("Unable to initialise zOS/MF server " + serverId, e);
        } catch (ZosManagerException e) {
            throw new ZosmfException("Unable to initialise zOS/MF server " + serverId + " as z/OS image '" + imageId + "' is not defined", e);
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
    public @NotNull IZosmfResponse get(String path, List<Integer> validStatusCodes, boolean convert) throws ZosmfException {
        String method = ZosmfRequestType.GET.name();
        if (validStatusCodes == null) {
            validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
        }
        ZosmfResponseImpl zosmfResponse;
        try {
            setHeader(ZosmfCustomHeaders.X_IBM_REQUESTED_METHOD.toString(), method);
            addCommonHeaders();
            zosmfResponse = new ZosmfResponseImpl(this.zosmfUrl, validPath(path));
            logger.trace(logRequest(method, zosmfResponse.getRequestUrl()));
            if (convert) {
                zosmfResponse.setHttpClientresponse(this.httpClient.getText(validPath(path)));
            } else {
                zosmfResponse.setHttpClientresponse(this.httpClient.getFile(validPath(path)));
            }
            
            logger.trace(logResponse(zosmfResponse.getStatusLine(), method, zosmfResponse.getRequestUrl()));
            if (!validStatusCodes.contains(zosmfResponse.getStatusCode())) {
                throw new ZosmfException(logBadStatusCode(zosmfResponse.getStatusCode()));
            }
        } catch (MalformedURLException | HttpClientException | IllegalArgumentException e) {
            logger.error(e);
            throw new ZosmfException(logBadRequest(method), e);
        }
        
        return zosmfResponse;
    }

    @Override
    public @NotNull IZosmfResponse postJson(String path, JsonObject requestBody, List<Integer> validStatusCodes) throws ZosmfException {
        String method = ZosmfRequestType.POST.name();
        if (validStatusCodes == null) {
            validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
        }
        ZosmfResponseImpl zosmfResponse;
        try {
            setHeader(ZosmfCustomHeaders.X_IBM_REQUESTED_METHOD.toString(), method);
            addCommonHeaders();
            zosmfResponse = new ZosmfResponseImpl(this.zosmfUrl, validPath(path));
            logger.trace(logRequest(method, zosmfResponse.getRequestUrl()));
            logger.trace(LOG_BODY + requestBody);
            zosmfResponse.setHttpClientresponse(this.httpClient.postJson(validPath(path), requestBody));
            logger.trace(logResponse(zosmfResponse.getStatusLine(), method, zosmfResponse.getRequestUrl()));
            if (!validStatusCodes.contains(zosmfResponse.getStatusCode())) {
                throw new ZosmfException(logBadStatusCode(zosmfResponse.getStatusCode()));
            }
        } catch (MalformedURLException | HttpClientException  e) {
            logger.error(e);
            throw new ZosmfException(logBadRequest(method), e);
        }
        
        return zosmfResponse;
    }

    @Override
    public @NotNull IZosmfResponse putText(String path, String requestBody, List<Integer> validStatusCodes) throws ZosmfException {
        String method = ZosmfRequestType.PUT.name();
        if (validStatusCodes == null) {
            validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
        }
        ZosmfResponseImpl zosmfResponse;
        try {
            setHeader(ZosmfCustomHeaders.X_IBM_REQUESTED_METHOD.toString(), method);
            addCommonHeaders();
            zosmfResponse = new ZosmfResponseImpl(this.zosmfUrl, validPath(path));
            logger.trace(logRequest(method, zosmfResponse.getRequestUrl()));
            logger.trace(LOG_BODY + requestBody);
            zosmfResponse.setHttpClientresponse(this.httpClient.putText(validPath(path), requestBody));
            logger.trace(logResponse(zosmfResponse.getStatusLine(), method, zosmfResponse.getRequestUrl()));
            if (!validStatusCodes.contains(zosmfResponse.getStatusCode())) {
                throw new ZosmfException(logBadStatusCode(zosmfResponse.getStatusCode()));
            }
        } catch (MalformedURLException | HttpClientException  e) {
            logger.error(e);
            throw new ZosmfException(logBadRequest(method), e);
        }
        
        return zosmfResponse;
    }

    @Override
    public @NotNull IZosmfResponse putJson(String path, JsonObject requestBody, List<Integer> validStatusCodes) throws ZosmfException {
        String method = ZosmfRequestType.PUT.name();
        if (validStatusCodes == null) {
            validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
        }
        ZosmfResponseImpl zosmfResponse;
        try {
            setHeader(ZosmfCustomHeaders.X_IBM_REQUESTED_METHOD.toString(), method);
            addCommonHeaders();
            zosmfResponse = new ZosmfResponseImpl(this.zosmfUrl, validPath(path));
            logger.trace(logRequest(method, zosmfResponse.getRequestUrl()));
            logger.trace(LOG_BODY + requestBody);
            zosmfResponse.setHttpClientresponse(this.httpClient.putJson(validPath(path), requestBody));
            logger.trace(logResponse(zosmfResponse.getStatusLine(), method, zosmfResponse.getRequestUrl()));
            if (!validStatusCodes.contains(zosmfResponse.getStatusCode())) {
                throw new ZosmfException(logBadStatusCode(zosmfResponse.getStatusCode()));
            }
        } catch (MalformedURLException | HttpClientException  e) {
            logger.error(e);
            throw new ZosmfException(logBadRequest(method), e);
        }
        
        return zosmfResponse;
    }

    @Override
    public @NotNull IZosmfResponse putBinary(String path, byte[] requestBody, List<Integer> validStatusCodes) throws ZosmfException  {
        String method = ZosmfRequestType.PUT.name();
        if (validStatusCodes == null) {
            validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
        }
        ZosmfResponseImpl zosmfResponse;

        try {
            setHeader(ZosmfCustomHeaders.X_IBM_REQUESTED_METHOD.toString(), method);
            addCommonHeaders();
            zosmfResponse = new ZosmfResponseImpl(this.zosmfUrl, validPath(path));
            logger.trace(logRequest(method, zosmfResponse.getRequestUrl()));
            logger.trace(LOG_BODY + requestBody);
            zosmfResponse.setHttpClientresponse(this.httpClient.putBinary(path, requestBody));
            logger.trace(logResponse(zosmfResponse.getStatusLine(), method, zosmfResponse.getRequestUrl()));
            if (!validStatusCodes.contains(zosmfResponse.getStatusCode())) {
                throw new ZosmfException(logBadStatusCode(zosmfResponse.getStatusCode()));
            }
        } catch (MalformedURLException | HttpClientException  e) {
            logger.error(e);
            throw new ZosmfException(logBadRequest(method), e);
        }

        return zosmfResponse;
    }

    @Override
    public @NotNull IZosmfResponse delete(String path, List<Integer> validStatusCodes) throws ZosmfException {
        String method = ZosmfRequestType.DELETE.name();
        if (validStatusCodes == null) {
            validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
        }
        ZosmfResponseImpl zosmfResponse;
        try {
            setHeader(ZosmfCustomHeaders.X_IBM_REQUESTED_METHOD.toString(), method);
            addCommonHeaders();
            zosmfResponse = new ZosmfResponseImpl(this.zosmfUrl, validPath(path));
            logger.trace(logRequest(method, zosmfResponse.getRequestUrl()));
            zosmfResponse.setHttpClientresponse(this.httpClient.deleteJson(validPath(path)));
            logger.trace(logResponse(zosmfResponse.getStatusLine(), method, zosmfResponse.getRequestUrl()));
            if (!validStatusCodes.contains(zosmfResponse.getStatusCode())) {
                throw new ZosmfException(logBadStatusCode(zosmfResponse.getStatusCode()));
            }
        } catch (MalformedURLException | HttpClientException  e) {
            throw new ZosmfException(logBadRequest(method), e);
        }
        
        return zosmfResponse;
    }

    @Override
	public @NotNull JsonObject serverInfo() throws ZosmfException {
        return get(PATH_SERVERDETAILS, null, false).getJsonContent();
	}

	@Override
    public IZosImage getImage() {
        return this.image;
    }

    @Override
    public String toString() {
        return this.image.getImageID() + " " + this.zosmfUrl;
    }

    protected String validPath(String path) {
        return path.startsWith("/") ? path : "/" + path;
    }

    protected void initialize() throws ZosmfException {
        
        String zosmfHostname;
        try {
            zosmfHostname = image.getDefaultHostname();
        } catch (ZosManagerException e) {
            throw new ZosmfException(e);
        }
        int zosmfPort;
        try {
            zosmfPort = ServerPort.get(image.getImageID());
        } catch (ZosmfManagerException e) {
            throw new ZosmfException(e);
        }
        String scheme = "http";
        try {
            if (Https.get(image.getImageID())) {
                scheme = "https";
            }
        } catch (ZosmfManagerException e) {
            throw new ZosmfException(e);
        }
        
        this.zosmfUrl = scheme + "://" + zosmfHostname + ":" + zosmfPort;

        this.httpClient = this.zosmfManager.getHttpManager().newHttpClient();
        
        try {
            ICredentials creds = null;
            String credsId = ServerCreds.get(this.serverId);
            if (credsId != null) {
                try {
                    creds = this.zosmfManager.getFramework().getCredentialsService().getCredentials(credsId);
                } catch (CredentialsException e) {
                    throw new ZosmfException("Problem accessing credentials store", e);
                }
            }
            
            if (creds == null) {
                creds = image.getDefaultCredentials();
            }

            this.httpClient.setURI(new URI(this.zosmfUrl));
            if (creds instanceof ICredentialsUsernamePassword) {
                this.httpClient.setAuthorisation(((ICredentialsUsernamePassword) creds).getUsername(), ((ICredentialsUsernamePassword) creds).getPassword());
            }
            if (scheme.equals("https")) {
                this.httpClient.setTrustingSSLContext();
            }
            this.httpClient.build();
        } catch (HttpClientException | ZosManagerException | URISyntaxException e) {
            throw new ZosmfException("Unable to create HTTP Client", e);
        }
        
        try {
            this.requestRetry = RequestRetry.get(this.serverId);
        } catch (ZosManagerException e) {
            throw new ZosmfException(e);
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
        return "Problem with " + method + " to zOSMF server";
    }

    protected int getRequestRetry() {
        return this.requestRetry;
    }    
}
