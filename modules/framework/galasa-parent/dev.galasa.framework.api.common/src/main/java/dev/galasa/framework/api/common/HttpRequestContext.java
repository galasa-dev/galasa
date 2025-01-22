/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import javax.servlet.http.HttpServletRequest;

/**
 * A HTTP request wrapper class that contains useful context related to HTTP requests,
 * like the username of the user sending the request
 */
public class HttpRequestContext {
    private HttpServletRequest request;
    private String username;

    public HttpRequestContext(HttpServletRequest request, Environment env) throws InternalServletException {
        this.request = request;

        // Some requests may not include an "Authorization" header,
        // so there is no username to set for such requests
        String bearerToken = JwtWrapper.getBearerTokenFromAuthHeader(request);
        if (bearerToken != null) {
            this.username = new JwtWrapper(bearerToken, env).getUsername();
        }
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public String getUsername() {
        return username;
    }
}
