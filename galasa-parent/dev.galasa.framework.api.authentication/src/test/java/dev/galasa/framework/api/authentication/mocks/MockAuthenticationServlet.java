/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.mocks;

import dev.galasa.framework.api.authentication.AuthenticationServlet;
import dev.galasa.framework.api.authentication.IOidcProvider;
import dev.galasa.framework.api.authentication.internal.DexGrpcClient;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.spi.IFramework;

public class MockAuthenticationServlet extends AuthenticationServlet {

    public MockAuthenticationServlet() {
        this(new MockOidcProvider());
    }

    public MockAuthenticationServlet(DexGrpcClient dexGrpcClient) {
        this(new MockOidcProvider(), dexGrpcClient, new MockFramework());
    }

    public MockAuthenticationServlet(IOidcProvider oidcProvider) {
        this(oidcProvider, new MockDexGrpcClient("https://my-issuer/dex"));
    }

    public MockAuthenticationServlet(IFramework framework) {
        this(new MockOidcProvider(), new MockDexGrpcClient("https://my-issuer/dex"), framework);
    }

    public MockAuthenticationServlet(IOidcProvider oidcProvider, DexGrpcClient dexGrpcClient) {
        this(oidcProvider, dexGrpcClient, new MockFramework());
    }

    public MockAuthenticationServlet(IOidcProvider oidcProvider, DexGrpcClient dexGrpcClient, IFramework framework) {
        this(getEnvironmentWithRequiredEnvVariablesSet(), oidcProvider, dexGrpcClient, framework);
    }

    public MockAuthenticationServlet(Environment env, IOidcProvider oidcProvider, DexGrpcClient dexGrpcClient, IFramework framework) {
        this.env = env;
        this.oidcProvider = oidcProvider;
        this.dexGrpcClient = dexGrpcClient;
        this.framework = framework;
        this.timeService = timeService;
        setResponseBuilder(new ResponseBuilder(env));
    }

    @Override
    protected void initialiseDexClients(String dexIssuerUrl, String dexGrpcHostname, String externalWebUiUrl) {
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
