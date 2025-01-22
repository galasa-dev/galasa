/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication;

import java.net.http.HttpClient;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import dev.galasa.framework.api.authentication.internal.OidcProvider;
import dev.galasa.framework.api.authentication.internal.routes.AuthCallbackRoute;
import dev.galasa.framework.api.authentication.internal.routes.AuthClientsRoute;
import dev.galasa.framework.api.authentication.internal.routes.AuthRoute;
import dev.galasa.framework.api.authentication.internal.routes.AuthTokensDetailsRoute;
import dev.galasa.framework.api.authentication.internal.routes.AuthTokensRoute;
import dev.galasa.framework.api.common.BaseServlet;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.SystemEnvironment;
import dev.galasa.framework.auth.spi.AuthServiceFactory;
import dev.galasa.framework.auth.spi.IAuthService;
import dev.galasa.framework.auth.spi.IAuthServiceFactory;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.utils.ITimeService;
import dev.galasa.framework.spi.utils.SystemTimeService;

/**
 * Authentication Servlet that acts as a proxy to send requests to Dex's /token
 * endpoint, returning the JWT received back from Dex.
 */
@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
        "osgi.http.whiteboard.servlet.pattern=/auth/*" }, name = "Galasa Authentication")
public class AuthenticationServlet extends BaseServlet {

    @Reference
    protected IFramework framework;

    protected RBACService rbacService;

    private static final String AUTH_DSS_NAMESPACE = "auth";
    private static final long serialVersionUID = 1L;

    private Log logger = LogFactory.getLog(getClass());

    protected IOidcProvider oidcProvider;

    private ITimeService timeService;

    private IAuthServiceFactory factory;

    public AuthenticationServlet() {
        this(new SystemEnvironment(), new SystemTimeService());
    }

    public AuthenticationServlet(Environment env, ITimeService timeService) {
        super(env);
        this.timeService = timeService;
    }

    @Override
    public void init() throws ServletException {
        logger.info("Galasa Authentication API initialising");

        // Make sure the relevant environment variables have been set, otherwise the servlet won't be able to talk to Dex
        String externalApiServerUrl = getRequiredEnvVariable(EnvironmentVariables.GALASA_EXTERNAL_API_URL);
        String dexIssuerUrl = getRequiredEnvVariable(EnvironmentVariables.GALASA_DEX_ISSUER);

        initialiseDexClients(dexIssuerUrl);

        if (factory == null) {
            factory = new AuthServiceFactory(framework, env);
        }

        IDynamicStatusStoreService dssService = null;
        try {
            dssService = framework.getDynamicStatusStoreService(AUTH_DSS_NAMESPACE);
        } catch (DynamicStatusStoreException e) {
            throw new ServletException("Failed to initialise authentication servlet");
        }

        IAuthService authService = factory.getAuthService();

        rbacService = getRBACService(framework);
        
        addRoute(new AuthRoute(getResponseBuilder(), oidcProvider, authService, env, dssService));
        addRoute(new AuthClientsRoute(getResponseBuilder(), authService, rbacService));
        addRoute(new AuthCallbackRoute(getResponseBuilder(), externalApiServerUrl, dssService));
        addRoute(new AuthTokensRoute(getResponseBuilder(), oidcProvider, authService, timeService, rbacService, env ));

        addRoute(new AuthTokensDetailsRoute(getResponseBuilder(), authService, rbacService));

        logger.info("Galasa Authentication API initialised");
    }

    private RBACService getRBACService(IFramework framework) throws ServletException {
        RBACService rbacService;
        try {
            rbacService= framework.getRBACService();
        } catch( RBACException ex) {
            throw new ServletException(ex);
        }
        return rbacService;
    }

    protected void setAuthServiceFactory(IAuthServiceFactory factory) {
        this.factory = factory;
    }

    /**
     * Initialises the OpenID Connect Provider and Dex gRPC client fields to allow
     * the authentication servlet to communicate with Dex.
     * @throws ServletException if there was an issue contacting Dex
     */
    protected void initialiseDexClients(String dexIssuerUrl) throws ServletException {
        this.oidcProvider = new OidcProvider(dexIssuerUrl, HttpClient.newHttpClient());
    }

    /**
     * Gets a given required environment variable, throwing a ServletException if a value has not been set.
     */
    private String getRequiredEnvVariable(String envName) throws ServletException {
        String envValue = env.getenv(envName);

        if (envValue == null) {
            throw new ServletException("Required environment variable '" + envName + "' has not been set.");
        }
        return envValue;
    }
}