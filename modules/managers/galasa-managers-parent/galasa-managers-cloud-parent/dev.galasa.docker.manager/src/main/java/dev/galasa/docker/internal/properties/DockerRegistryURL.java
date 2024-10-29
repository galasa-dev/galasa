/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker.internal.properties;

import java.net.MalformedURLException;
import java.net.URL;

import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.internal.DockerRegistryImpl;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * Docker Registry URL CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name docker.registry.ID.url
 * 
 * @galasa.description Provides the URL of a Docker Registry that is used by the Docker Manager.
 * 
 * @galasa.required Yes if the Registry ID is used in the CPS Property <code>docker.default.registries</code>. However, 
 * the Docker Manager will default DOCKERHUB to <code>https://registry.hub.docker.com</code> if not provided.
 * 
 * @galasa.default None, except for DOCKERHUB where the default is <code>https://registry.hub.docker.com</code>
 * 
 * @galasa.valid_values A valid URL
 * 
 * @galasa.examples 
 * <code>docker.registry.LOCAL.url=https://registry.local.com</code>
 * 
 * @galasa.extra
 * If the Docker Registry requires credentials for authentication, then the id for the credentials must be provided using the CPS property 
 * <code>docker.registry.ID.credentials</code> or <code>docker.registry.credentials</code>
 * 
 */
public class DockerRegistryURL extends CpsProperties {

    public static URL get(DockerRegistryImpl dockerRegistry) throws DockerManagerException {
        String id = dockerRegistry.getId();
        String dockerRegistryURL = "";
        try {
            dockerRegistryURL = getStringNulled(DockerPropertiesSingleton.cps(), "registry", "url", id);
            // Default value
            if (dockerRegistryURL == null) {
                if("DOCKERHUB".equals(id)) {
                    return new URL("https://registry.hub.docker.com");
                }
                throw new DockerManagerException("Could not find a docker registry type in CPS for : " + id);
            }
        return new URL(dockerRegistryURL);
        } catch (ConfigurationPropertyStoreException e) {
            throw new DockerManagerException("Problem asking the CPS for the docker registry type", e);
        } catch (MalformedURLException e) {
            throw new DockerManagerException("Could not parse the url returned from CPS: " + dockerRegistryURL, e);
        }
    }
}
