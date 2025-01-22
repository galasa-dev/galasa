/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.authentication.internal.OidcProvider;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.SystemEnvironment;
import dev.galasa.framework.api.common.JwtWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

@Component(service = Filter.class, scope = ServiceScope.PROTOTYPE, property = {
        "osgi.http.whiteboard.filter.pattern=/*" }, name = "Galasa JWT Auth")
public class JwtAuthFilter implements Filter {

    private final Log logger = LogFactory.getLog(getClass());

    private static final Map<String, List<String>> UNAUTHENTICATED_ROUTES = UnauthenticatedRoute.getRoutesAsMap();

    protected ResponseBuilder responseBuilder = new ResponseBuilder();

    private Environment env;

    protected IOidcProvider oidcProvider;

    public JwtAuthFilter() {
        this(new SystemEnvironment());
    }

    public JwtAuthFilter(Environment env) {
        this.env = env;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String dexIssuerUrl = env.getenv(EnvironmentVariables.GALASA_DEX_ISSUER);

        if (dexIssuerUrl != null) {
            oidcProvider = new OidcProvider(dexIssuerUrl, HttpClient.newHttpClient());
            logger.info("Galasa JWT Auth Filter initialised");
        } else {
            throw new ServletException("Unable to initialise JWT auth filter. Required environment variable '"
                + EnvironmentVariables.GALASA_DEX_ISSUER + "' has not been set.");
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest servletRequest = (HttpServletRequest) request;
        HttpServletResponse servletResponse = (HttpServletResponse) response;

        String errorString = "";
        int httpStatusCode = HttpServletResponse.SC_OK;

        // Apply the filter to API endpoints that require a valid JWT to access
        if (isRequestingAuthenticatedRoute(servletRequest)) {

            try {
                String sJwt = JwtWrapper.getBearerTokenFromAuthHeader(servletRequest);

                // Throw an unauthorized exception if the provided JWT isn't valid
                if (sJwt == null || !oidcProvider.isJwtValid(sJwt)) {
                    ServletError error = new ServletError(GAL5401_UNAUTHORIZED);
                    throw new InternalServletException(error, HttpServletResponse.SC_UNAUTHORIZED);
                }

            } catch (InternalServletException e) {
                errorString = e.getMessage();
                httpStatusCode = e.getHttpFailureCode();
            } catch (Exception e) {
                errorString = new ServletError(GAL5000_GENERIC_API_ERROR).toJsonString();
                httpStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                logger.error(errorString, e);
            }
        }

        if (httpStatusCode == HttpServletResponse.SC_OK) {
            // The provided JWT is valid or the request is not to a protected endpoint,
            // so allow the request through
            chain.doFilter(request, response);
        } else {
            // The JWT is not valid or something went wrong, so return the error response
            responseBuilder.buildResponse(servletRequest, servletResponse, "application/json", errorString, httpStatusCode);
        }
    }

    @Override
    public void destroy() {
    }

    /**
     * Checks if the given servlet request is going to an endpoint that requires a bearer token
     * to be accessed.
     * @param servletRequest the request being made to the API server
     * @return true if an endpoint requiring a bearer token was requested, false otherwise
     */
    private boolean isRequestingAuthenticatedRoute(HttpServletRequest servletRequest) {
        boolean routeRequiresJwt = true;
        String route = servletRequest.getRequestURI().substring(servletRequest.getContextPath().length());

        List<String> allowedRouteMethods = UNAUTHENTICATED_ROUTES.get(route);
        if (allowedRouteMethods != null) {
            routeRequiresJwt = !allowedRouteMethods.contains(servletRequest.getMethod());
        }

        return routeRequiresJwt;
    }
}