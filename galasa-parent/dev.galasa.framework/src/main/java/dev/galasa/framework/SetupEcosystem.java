/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;

@Component(service = { SetupEcosystem.class })
public class SetupEcosystem {

    private Log logger = LogFactory.getLog(this.getClass());

    private static final String DSS_ENV_VAR   = "GALASA_DYNAMICSTATUS_STORE";
    private static final String RAS_ENV_VAR   = "GALASA_RESULTARCHIVE_STORE";
    private static final String CREDS_ENV_VAR = "GALASA_CREDENTIALS_STORE";
    private static final String AUTH_ENV_VAR  = "GALASA_AUTH_STORE";

    private static final String DSS_CPS_NAME   = "dynamicstatus";
    private static final String RAS_CPS_NAME   = "resultarchive";
    private static final String CREDS_CPS_NAME = "credentials";
    private static final String AUTH_CPS_NAME  = "auth";

    private IFramework framework;

    /**
     * <p>Setup the Ecosystem for remote usage</p>
     *
     * @param bootstrapProperties
     * @param overrideProperties
     * @throws FrameworkException
     */
    public void setup(Properties bootstrapProperties, Properties overrideProperties) throws FrameworkException {

        logger.info("Initialising Setup Ecosystem Service");

        FrameworkInitialisation frameworkInitialisation = null;
        try {
            frameworkInitialisation = new FrameworkInitialisation(bootstrapProperties, overrideProperties);
        } catch (Exception e) {
            throw new FrameworkException("Unable to initialise the Framework Service", e);
        }

        framework = frameworkInitialisation.getFramework();
        IConfigurationPropertyStoreService cps = framework.getConfigurationPropertyService("framework");

        // Check for System Environment variables to see what we need to set in the CPS
        addStoreToCpsProperties(cps, DSS_CPS_NAME, DSS_ENV_VAR);
        addStoreToCpsProperties(cps, RAS_CPS_NAME, RAS_ENV_VAR);
        addStoreToCpsProperties(cps, CREDS_CPS_NAME, CREDS_ENV_VAR);
        addStoreToCpsProperties(cps, AUTH_CPS_NAME, AUTH_ENV_VAR);

        logger.info("Ending Setup Ecosystem Service");

        frameworkInitialisation.shutdownFramework();

    }

    private void addStoreToCpsProperties(IConfigurationPropertyStoreService cps, String storeName, String storeEnvVar) throws ConfigurationPropertyStoreException {
        String storeUri = System.getenv(storeEnvVar);
        String storeCpsProp = storeName + ".store";

        if (storeUri != null && !storeUri.trim().isEmpty()) {
            storeUri = storeUri.trim();

            cps.setProperty(storeCpsProp, storeUri);
            logger.info("framework." + storeCpsProp + " has been set to: " + storeUri);
        } else {
            logger.info("Not setting framework." + storeCpsProp);
        }
    }

}