/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker.internal.properties;

import dev.galasa.docker.DockerManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * Docker Registry Busybox Image CPS property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name docker.registry.[registryId].busybox.image
 * 
 * @galasa.description Provides fully qualified image name including repository and tag of a 'busybox' Docker image on a given registry.
 * 
 * @galasa.required No - if not provided it will default to 'library/busybox:latest' which is compatible with the default Docker registry, Dockerhub.
 * 
 * @galasa.default library/busybox:latest
 * 
 * @galasa.valid_values library/busybox:latest, myorg/busybox:1.1.1
 * 
 * @galasa.examples 
 * <code>docker.registry.[registryId].busybox.image=registryorg/busybox:latest</code>
 * 
 * @galasa.extra
 * This CPS property is required when you want to allow the Docker Manager to search for a 'busybox' image in the Docker Registries<br>
 * provided in the docker.default.registries CPS property, instead of in Dockerhub. If you do not specify this CPS property for any of<br>
 * the provided Docker Registries, it will default to 'library/busybox:latest' so either your Docker Registries or Dockerhub will be<br>
 * searched for the an image in 'library/busybox:latest'. This CPS property allows you to point the Docker Manager at a 'busybox' image that<br>
 * may not be in the 'library' namespace and may not be tagged with 'latest'.<br> 
 * */
public class DockerRegistryBusyboxImage extends CpsProperties {

    public static String get(String[] dockerRegistries) throws DockerManagerException {
		String busyboxImage = "";
        IConfigurationPropertyStoreService cps = DockerPropertiesSingleton.cps();
		if (dockerRegistries.length > 0) {
			for (String dockerRegistry : dockerRegistries) {
				try {
					busyboxImage = getStringNulled(cps, "registry", "busybox.image", dockerRegistry);
				} catch (ConfigurationPropertyStoreException e) {
					throw new DockerManagerException("Problem asking the CPS for the Busybox image name on registry ID: "  + dockerRegistry, e);
				}
				if (busyboxImage != null && !busyboxImage.equals("")) {
					break;
				}
			}
		}
		if (busyboxImage == null || busyboxImage.isBlank()) {
			busyboxImage = "library/busybox:latest";
		}
		return busyboxImage;
	}
}