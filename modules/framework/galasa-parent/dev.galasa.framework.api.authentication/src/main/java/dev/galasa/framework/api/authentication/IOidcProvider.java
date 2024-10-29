package dev.galasa.framework.api.authentication;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.servlet.http.HttpSession;

import com.google.gson.JsonObject;

/**
 * An interface that defines the methods used to communicate with an OpenID Connect (OIDC)
 * provider via standard OpenID Connect REST API endpoints.
 */
public interface IOidcProvider {

    /**
     * Sends a GET request to an OpenID Connect provider's /.well-known/openid-configuration
     * endpoint and return the JSON response.
     */
    public JsonObject getOpenIdConfiguration() throws IOException, InterruptedException;

    /**
     * Gets the URL of the upstream connector to authenticate with (e.g. a
     * github.com URL to authenticate with an OAuth application in GitHub).
     */
    public String getConnectorRedirectUrl(String clientId, String callbackUrl, HttpSession session) throws IOException, InterruptedException;

    /**
     * Sends a GET request to an OpenID Connect authorization endpoint, returning
     * the received response.
     */
    public HttpResponse<String> sendAuthorizationGet(String clientId, String callbackUrl, HttpSession session) throws IOException, InterruptedException;

    /**
     * Sends a POST request with a given request body to an OpenID Connect /token endpoint, returning the received response.
     */
    public HttpResponse<String> sendTokenPost(String clientId, String clientSecret, String refreshToken) throws IOException, InterruptedException;

    /**
     * Sends a POST request with a given request body to an OpenID Connect /token endpoint, returning the received response.
     */
    public HttpResponse<String> sendTokenPost(String clientId, String clientSecret, String authCode, String redirectUri) throws IOException, InterruptedException;

    /**
     * Checks if a given JWT is valid or not
     */
    public boolean isJwtValid(String jwt) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InterruptedException;
}
