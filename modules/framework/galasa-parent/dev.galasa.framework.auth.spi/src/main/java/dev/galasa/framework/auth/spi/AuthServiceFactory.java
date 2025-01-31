/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.auth.spi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.ServletErrorMessage;
import dev.galasa.framework.auth.spi.internal.AuthService;
import dev.galasa.framework.auth.spi.internal.DexGrpcClient;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.rbac.RBACService;

public class AuthServiceFactory implements IAuthServiceFactory {

    public static final int HTTP_FORBIDDEN = 403 ;

    private IFramework framework;
    private Environment env;
    private IAuthService authService;
    private final Log logger = LogFactory.getLog(getClass());

    public AuthServiceFactory(IFramework framework, Environment env) {
        this.framework = framework;
        this.env = env;
    }

    @Override
    public IAuthService getAuthService() throws InternalServletException {
        if (authService == null) {
            String dexIssuerHostname = getRequiredEnvVariable(EnvironmentVariables.GALASA_DEX_GRPC_HOSTNAME);
            String externalApiServerUrl = getRequiredEnvVariable(EnvironmentVariables.GALASA_EXTERNAL_API_URL);
            String externalWebUiUrl = externalApiServerUrl.replace("/api", "");

            IDexGrpcClient dexGrpcClient = new DexGrpcClient(dexIssuerHostname, externalWebUiUrl);
            RBACService rbacService = getRBacService(framework);
            this.authService = new AuthService(framework.getAuthStoreService(), dexGrpcClient, rbacService);
        }
        return authService;
    }

    private RBACService getRBacService(IFramework framework) throws InternalServletException {
        RBACService rbacService ;
        try {
            rbacService = framework.getRBACService();
        } catch(RBACException rbacEx) {
            ServletError error = new ServletError(ServletErrorMessage.GAL5126_INTERNAL_RBAC_ERROR);
            throw new InternalServletException(error, HTTP_FORBIDDEN, rbacEx);
        }
        return rbacService;
    }

    private String getRequiredEnvVariable(String envName) throws InternalServletException {
        String envValue = env.getenv(envName);

        if (envValue == null) {
            logger.error("Required environment variable '" + envName + "' has not been set.");
            ServletError error = new ServletError(ServletErrorMessage.GAL5126_INTERNAL_RBAC_ERROR);
            throw new InternalServletException(error, HTTP_FORBIDDEN);
        }
        return envValue;
    }
}
