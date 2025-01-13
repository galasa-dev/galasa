/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.auth.spi;

import javax.servlet.ServletException;

import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.auth.spi.internal.AuthService;
import dev.galasa.framework.auth.spi.internal.DexGrpcClient;
import dev.galasa.framework.spi.IFramework;

public class AuthServiceFactory implements IAuthServiceFactory {

    private IFramework framework;
    private Environment env;
    private IAuthService authService;

    public AuthServiceFactory(IFramework framework, Environment env) {
        this.framework = framework;
        this.env = env;
    }

    @Override
    public IAuthService getAuthService() throws ServletException {
        if (authService == null) {
            String dexIssuerHostname = getRequiredEnvVariable(EnvironmentVariables.GALASA_DEX_GRPC_HOSTNAME);
            String externalApiServerUrl = getRequiredEnvVariable(EnvironmentVariables.GALASA_EXTERNAL_API_URL);
            String externalWebUiUrl = externalApiServerUrl.replace("/api", "");

            IDexGrpcClient dexGrpcClient = new DexGrpcClient(dexIssuerHostname, externalWebUiUrl);
            this.authService = new AuthService(framework.getAuthStoreService(), dexGrpcClient);
        }
        return authService;
    }

    private String getRequiredEnvVariable(String envName) throws ServletException {
        String envValue = env.getenv(envName);

        if (envValue == null) {
            throw new ServletException("Required environment variable '" + envName + "' has not been set.");
        }
        return envValue;
    }
}
