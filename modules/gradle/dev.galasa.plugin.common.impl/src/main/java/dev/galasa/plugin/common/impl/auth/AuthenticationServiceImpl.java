/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.plugin.common.impl.auth;

import java.net.*;
import java.text.MessageFormat;
import org.apache.http.client.*;
import org.apache.http.*;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import com.google.gson.*;

import dev.galasa.plugin.common.impl.GsonFactory;
import dev.galasa.plugin.common.AuthenticationException;
import dev.galasa.plugin.common.AuthenticationService;
import dev.galasa.plugin.common.impl.auth.beans.*;

/**
 * A class which can contact the remote galasa server and get a JWT to use.
 */
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final String GALASA_TOKEN_PART_SEPARATOR = ":";

    // This is the version of the API which the plugin is coded to talk to.
    // We use it to announce to the galasa server which version of the api this client understands.
    // Bump this version up when this plugin code is able to understand later versions of the REST api, and is re-tested.
    // not every time it changes in the galasa framework.
    private static final String GALASA_API_VERSION = "0.33.0";

    private URL apiServerUrl ;

    private String galasaRefreshToken ;
    private String galasaClientId ;

    private HttpClient httpClient;

    /**
     * @param apiServerUrl The url of the galasa server
     * @param galasaAccessToken
     * @throws AuthenticationException
     */
    public AuthenticationServiceImpl(URL apiServerUrl, String galasaAccessToken, HttpClient httpClient) throws AuthenticationException {
        validateAndStoreApiServerUrl(apiServerUrl);
        validateAndStoreGalasaAccessToken(galasaAccessToken);
        validateAndStoreHttpClient(httpClient);
    }

    private void validateAndStoreHttpClient(HttpClient httpClient) throws AuthenticationException {
        if (httpClient == null) {
            throw new AuthenticationException("Error: Program logic error. No http client supplied to AuthenticationService.");
        }
        this.httpClient = httpClient ;
    }

    private void validateAndStoreApiServerUrl(URL apiServerUrl) throws AuthenticationException {
        if (apiServerUrl == null) {
            throw new AuthenticationException("Error: galasa rest api endpoint has not supplied. It is derived from the galasa bootstrap.");
        }
        this.apiServerUrl = apiServerUrl ;
    }

    private void validateAndStoreGalasaAccessToken(String galasaAccessToken) throws AuthenticationException {
        if (galasaAccessToken == null) {
            throw new AuthenticationException("Error: galasa-token has not supplied. Get a galasa access token from your Galasa server.");
        }

        // Split the token into the refresh token and client-id (in that order)
        String[] parts = galasaAccessToken.split(GALASA_TOKEN_PART_SEPARATOR);
        if (parts.length!=2) {
            String msg = "Error: galasa-token value supplied is not a valid galasa authentication token. It should have exactly two parts, separated by a single '"
                +GALASA_TOKEN_PART_SEPARATOR+"' but it does not.";
            throw new AuthenticationException(msg);
        }

        // Remember these pieces so we can use them later.
        this.galasaRefreshToken = parts[0];
        this.galasaClientId = parts[1];
    }


    /** 
     * Exchanges the GALASA_TOKEN for a Java Web Token from the Galasa server, so we can talk to the server
     * freely after that.
     */
    @Override
    public String getJWT() throws AuthenticationException {

        Gson gson = new GsonFactory().getGson();

        String authEndpointUrl = this.apiServerUrl.toString() + "/auth";

        HttpPost postRequest = createAuthHttpPostRequest(gson, authEndpointUrl);

        HttpResponse response = null;
        try {
            response = httpClient.execute(postRequest);
        } catch (Exception ex) {
            String msg = MessageFormat.format("Error: Failed to turn the Galasa token into a Java Web Token (JWT) using URL ''{0}''. Cause: {1}",
                authEndpointUrl,ex.getMessage());
            throw new AuthenticationException(msg,ex);
        }

        // Check the auth service returned OK.
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == HttpStatus.SC_BAD_REQUEST) {
            AuthError authErrorDetail = null ;
            try {
                HttpEntity entity = response.getEntity();
                String responseBodyString = EntityUtils.toString(entity);
                authErrorDetail = gson.fromJson(responseBodyString,AuthError.class);
            } catch(Exception ex) {
                String msg = MessageFormat.format("Error: Failed to turn the Galasa token into a Java Web Token (JWT) using URL ''{0}''. Response from server (''{1}'') was not OK. Could not parse the returned payload.",
                    authEndpointUrl, statusCode);
                throw new AuthenticationException(msg);
            }
            // If we got here, we just parsed the error structure coming back from the server...
            String msg = MessageFormat.format("Error: Failed to turn the Galasa token into a Java Web Token (JWT) using URL ''{0}''. Response from server (''{1}'') was not OK. Error details: code: {2}, message: {3}",
                authEndpointUrl, statusCode, authErrorDetail.error_code, authErrorDetail.error_message);
            throw new AuthenticationException(msg);

        }

        if (statusCode != HttpStatus.SC_OK) {
            String msg = MessageFormat.format("Error: Failed to turn the Galasa token into a Java Web Token (JWT) using URL ''{0}''. Response from server (''{1}'') was not OK.",
                authEndpointUrl, statusCode);
            throw new AuthenticationException(msg);
        }

        String responseBodyString;
        try {
            HttpEntity entity = response.getEntity();
            responseBodyString = EntityUtils.toString(entity);
        } catch (Exception ex) {
            String msg = MessageFormat.format("Error: Failed to turn the Galasa token into a Java Web Token (JWT) using URL ''{0}''. Response body parsing issue: Cause: {1}",authEndpointUrl,ex.getMessage());
            throw new AuthenticationException(msg,ex);
        }

        AuthResponsePayload responsePayload = gson.fromJson(responseBodyString,AuthResponsePayload.class);
        String jwt = responsePayload.jwt ;

        return jwt;
    }
    

    private HttpPost createAuthHttpPostRequest(Gson gson, String authEndpointUrl) throws AuthenticationException {

        // Create the post request.
        HttpPost postRequest = new HttpPost(authEndpointUrl);
        
        // Set headers..
        postRequest.setHeader("Content-Type","application/json");
        postRequest.setHeader("ClientApiVersion", GALASA_API_VERSION);

        // Set the payload..
        setAuthHttpPostRequestPayload(gson, postRequest, authEndpointUrl);

        return postRequest;
    }


    private void setAuthHttpPostRequestPayload(Gson gson, HttpPost postRequest, String authEndpointUrl) throws AuthenticationException {
        AuthRequestPayload authRequestPayload = new AuthRequestPayload();
        authRequestPayload.client_id = this.galasaClientId;
        authRequestPayload.refresh_token = this.galasaRefreshToken;

        String payloadString = gson.toJson(authRequestPayload);

        try {
            StringEntity payload = new StringEntity(payloadString);
            postRequest.setEntity(payload);
        } catch (Exception ex ) {
            String msg = MessageFormat.format("Error: Failed to turn the Galasa token into a Java Web Token (JWT) using URL ''{0}''. Cause: ",authEndpointUrl,ex.getMessage());
            throw new AuthenticationException(msg,ex);
        }
    }
}
