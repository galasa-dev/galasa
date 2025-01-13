/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.mocks;

import dev.galasa.framework.api.authentication.AuthenticationServlet;
import dev.galasa.framework.api.authentication.IOidcProvider;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.auth.spi.IAuthService;
import dev.galasa.framework.auth.spi.IDexGrpcClient;
import dev.galasa.framework.auth.spi.internal.AuthService;
import dev.galasa.framework.auth.spi.mocks.MockAuthServiceFactory;
import dev.galasa.framework.auth.spi.mocks.MockDexGrpcClient;
import dev.galasa.framework.mocks.FilledMockRBACService;
import dev.galasa.framework.mocks.MockIDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.rbac.RBACService;

public class MockAuthenticationServlet extends AuthenticationServlet {

    public MockAuthenticationServlet() {
        this(new MockOidcProvider());
    }

    public MockAuthenticationServlet(IDexGrpcClient dexGrpcClient) {
        this(new MockOidcProvider(), dexGrpcClient, new MockFramework(new MockIDynamicStatusStoreService()));
    }

    public MockAuthenticationServlet(IOidcProvider oidcProvider) {
        this(oidcProvider, new MockDexGrpcClient("https://my-issuer/dex"));
    }

    public MockAuthenticationServlet(IFramework framework) {
        this(new MockOidcProvider(), new MockDexGrpcClient("https://my-issuer/dex"), framework);
    }

    public MockAuthenticationServlet(IOidcProvider oidcProvider, IDexGrpcClient dexGrpcClient) {
        this(oidcProvider, dexGrpcClient, new MockFramework(new MockIDynamicStatusStoreService()));
    }

    public MockAuthenticationServlet(IOidcProvider oidcProvider, IDexGrpcClient dexGrpcClient, IFramework framework) {
        this(getEnvironmentWithRequiredEnvVariablesSet(), oidcProvider, dexGrpcClient, framework, FilledMockRBACService.createTestRBACService() );
    }

    public MockAuthenticationServlet(Environment env, IOidcProvider oidcProvider, IDexGrpcClient dexGrpcClient, IFramework framework, RBACService rbacService) {
        super.env = env;
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

    private static MockEnvironment getEnvironmentWithRequiredEnvVariablesSet() {
        MockEnvironment mockEnv = new MockEnvironment();
        mockEnv.setenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL, "http://my-api.server/api");
        mockEnv.setenv(EnvironmentVariables.GALASA_DEX_ISSUER, "http://my-dex.issuer/dex");
        mockEnv.setenv(EnvironmentVariables.GALASA_DEX_GRPC_HOSTNAME, "dex-grpc:1234");
        mockEnv.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, "name,sub");
        return mockEnv;
    }
}
