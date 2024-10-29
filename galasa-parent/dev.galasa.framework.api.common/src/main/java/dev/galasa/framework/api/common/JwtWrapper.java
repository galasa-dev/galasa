/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.framework.api.common;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;


public class JwtWrapper {

    protected DecodedJWT decodedJwt;
    private Log logger = LogFactory.getLog(this.getClass());

    // Stores a comma-separated string of JWT claims that can map to a username
    private final String usernameClaimsStr;

    public JwtWrapper(HttpServletRequest req) {
        this(req, new SystemEnvironment());
    }

    public JwtWrapper(HttpServletRequest req, Environment env) {
        this.decodedJwt = decodeJwt(getBearerTokenFromAuthHeader(req));
        usernameClaimsStr = env.getenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS);
    }

    public JwtWrapper(String jwt, Environment env) {
        this.decodedJwt = decodeJwt(jwt);
        usernameClaimsStr = env.getenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS);
    }

    public DecodedJWT decodeJwt(@NotNull String jwt) {
        return JWT.decode(jwt);
    }

    /**
     * Get the value of the "sub" claim from the decoded JWT
     * 
     * @return the value associated with the decoded JWT's "sub" claim
     */
    public String getSubject() {
        return decodedJwt.getSubject();
    }

    /**
     * Gets a JSON Web Token (JWT) from a given request's Authorization header,
     * returning null if it does not have one.
     *
     * @param servletRequest the request to retrieve a JWT from
     * @return the JWT stored in the request's "Authorization" header, or null
     */
    public static String getBearerTokenFromAuthHeader(HttpServletRequest servletRequest) {
        String sJwt = null;
        String authorization = servletRequest.getHeader("Authorization");
        if (authorization != null) {
            StringTokenizer st = new StringTokenizer(authorization);
            if (st.hasMoreTokens()) {
                String bearer = st.nextToken();
                if (bearer.equalsIgnoreCase("bearer") && st.hasMoreTokens()) {
                    sJwt = st.nextToken();
                }
            }
        }
        return sJwt;
    }

    /**
     * Gets the username from a request's JWT, throwing an exception if a username
     * could not be retrieved from the supplied JWT claims.
     *
     * @return the username retrieved from a claim in the JWT
     * @throws InternalServletException if no JWT claims could be used to get a
     *                                  username or no JWT was set
     */
    public String getUsername() throws InternalServletException {
        // Use an environment variable to set the list of username claims
        List<String> usernameClaims = new ArrayList<>();
        if (usernameClaimsStr != null && !usernameClaimsStr.isEmpty()) {
            logger.info("Environment variable '" + EnvironmentVariables.GALASA_USERNAME_CLAIMS
                    + "' used to provide the JWT claims that map to a username");
            usernameClaims = getUsernameClaimOverridesAsList();
        } else {
            ServletError error = new ServletError(GAL5058_NO_USERNAME_JWT_CLAIMS_PROVIDED);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }

        // Go through the preference list of JWT claims to get a username from the decoded JWT
        String userName = getUsernameFromClaims(usernameClaims);
        if (userName == null) {
            ServletError error = new ServletError(GAL5057_FAILED_TO_RETRIEVE_USERNAME_FROM_JWT, usernameClaimsStr);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
        return userName;
    }

    private String getUsernameFromClaims(List<String> usernameClaims) {
        String username = null;
        for (String claim : usernameClaims) {
            username = decodedJwt.getClaim(claim).asString();
            if (username != null) {
                break;
            }
        }
        return username;
    }

    private List<String> getUsernameClaimOverridesAsList() {
        String[] overrideClaims = usernameClaimsStr.split(",");
        List<String> usernameClaims = new ArrayList<>();

        for (String claim : overrideClaims) {
            usernameClaims.add(claim.trim());
        }
        return usernameClaims;
    }
}

