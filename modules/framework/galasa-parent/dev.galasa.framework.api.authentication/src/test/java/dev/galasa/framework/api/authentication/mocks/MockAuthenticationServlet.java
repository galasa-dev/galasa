/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.mocks;

import java.time.Instant;

import dev.galasa.framework.api.authentication.AuthenticationServlet;
import dev.galasa.framework.api.authentication.IOidcProvider;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.mocks.FilledMockEnvironment;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.auth.spi.IAuthService;
import dev.galasa.framework.auth.spi.IDexGrpcClient;
import dev.galasa.framework.auth.spi.internal.AuthService;
import dev.galasa.framework.auth.spi.mocks.MockAuthServiceFactory;
import dev.galasa.framework.auth.spi.mocks.MockDexGrpcClient;
import dev.galasa.framework.mocks.MockIDynamicStatusStoreService;
import dev.galasa.framework.mocks.MockTimeService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.rbac.RBACService;

public class MockAuthenticationServlet extends AuthenticationServlet {

    public MockAuthenticationServlet() throws Exception {
        this(new MockOidcProvider());
    }

    public MockAuthenticationServlet(IDexGrpcClient dexGrpcClient) throws Exception {
        this(new MockOidcProvider(), dexGrpcClient, new MockFramework(new MockIDynamicStatusStoreService()));
    }

    public MockAuthenticationServlet(IOidcProvider oidcProvider) throws Exception {
        this(oidcProvider, new MockDexGrpcClient("https://my-issuer/dex"));
    }

    public MockAuthenticationServlet(IFramework framework) throws Exception {
        this(new MockOidcProvider(), new MockDexGrpcClient("https://my-issuer/dex"), framework);
    }

    public MockAuthenticationServlet(IOidcProvider oidcProvider, IDexGrpcClient dexGrpcClient) throws Exception {
        this(oidcProvider, dexGrpcClient, new MockFramework(new MockIDynamicStatusStoreService()));
    }

    public MockAuthenticationServlet(IOidcProvider oidcProvider, IDexGrpcClient dexGrpcClient, IFramework framework) throws Exception {
        this(FilledMockEnvironment.createTestEnvironment(), oidcProvider, dexGrpcClient, framework, framework.getRBACService());
    }

    public MockAuthenticationServlet(Environment env, IOidcProvider oidcProvider, IDexGrpcClient dexGrpcClient, IFramework framework, RBACService rbacService) {
        super(env, new MockTimeService(Instant.now()));
        super.oidcProvider = oidcProvider;
        super.framework = framework;
        super.rbacService = rbacService;
        IAuthService authService = new AuthService(framework.getAuthStoreService(), dexGrpcClient);
        setAuthServiceFactory(new MockAuthServiceFactory(authService));
        setResponseBuilder(new ResponseBuilder(env));
    }

    public void setFramework(IFramework framework) {
        this.framework = framework;
    }

    @Override
    protected void initialiseDexClients(String dexIssuerUrl) {
        // Do nothing...
    }
}
