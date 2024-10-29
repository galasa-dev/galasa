/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.internal.properties;

import java.util.List;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.ZosManagerException;

/**
 * The images for a zOS Cluster
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zos.cluster.[clusterId].images
 * 
 * @galasa.description The zOS Images for the specified cluster
 * 
 * @galasa.required No
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>zos.cluster.[clusterId].images=SYSA,SYSB,SYSC</code><br>
 *
 */
public class ClusterImages extends CpsProperties {
    
    public static List<String> get(String clusterId) throws ZosManagerException {
        try {
            List<String> images = getStringList(ZosPropertiesSingleton.cps(), "cluster", clusterId + ".images");
            if (images.isEmpty()) {
                throw new ZosManagerException("Unable to locate zOS images for cluster " + clusterId + ", see property zos.cluster.*.images");
            }
            return images;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosManagerException("Problem asking the CPS for the cluster images for cluster '"  + clusterId + "'", e);
        }
    }

}
