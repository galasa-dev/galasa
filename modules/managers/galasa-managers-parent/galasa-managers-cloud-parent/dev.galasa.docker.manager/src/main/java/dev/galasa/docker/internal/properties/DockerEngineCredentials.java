/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker.internal.properties;

import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.internal.DockerEngineImpl;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * Docker Engine Credentials CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name docker.engine.[engineId].credentials.id
 * 
 * @galasa.description Provides the credentials ID for HTTPS client authentication to the Docker Engine
 * 
 * @galasa.required No
 * 
 * @galasa.default None - HTTP will be used if not specified
 * 
 * @galasa.valid_values A valid credentials ID from the Galasa Credentials Store
 * 
 * @galasa.examples 
 * <code>docker.engine.LOCAL.credentials.id=DOCKER_TLS_CERTS</code>
 * 
 * @galasa.extra
 * When connecting to a Docker Engine with TLS enabled, this property specifies the credentials ID
 * that contains the KeyStore with client certificates and CA certificates for mutual TLS authentication.
 * The credentials must be of type KeyStore and should contain:
 * <ul>
 * <li>Client certificate and private key for client authentication</li>
 * <li>CA certificate(s) to verify the Docker Engine's certificate</li>
 * </ul>
 * If this property is not set, the Docker Manager will use HTTP (not HTTPS) to connect to the engine.
 * 
 */
public class DockerEngineCredentials extends CpsProperties {

    /**
     * Get the credentials ID for the specified Docker Engine
     * 
     * @param dockerEngineImpl The Docker Engine implementation
     * @return The credentials ID, or null if not configured
     * @throws DockerManagerException if there's a problem accessing CPS
     */
    public static String get(DockerEngineImpl dockerEngineImpl) throws DockerManagerException {
        try {
            String credentialsId = getStringNulled(DockerPropertiesSingleton.cps(), "engine", "credentials.id", dockerEngineImpl.getEngineId());
            return credentialsId;
        } catch (ConfigurationPropertyStoreException e) {
            throw new DockerManagerException(
                "Problem retrieving docker engine credentials property", e);
        }
    }
}
