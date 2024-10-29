/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.internal.properties;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.ZosManagerException;

/**
 * The zOS Cluster ID
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zos.tag.[tag].clusterid
 * 
 * @galasa.description The zOS Cluster ID for the specified tag
 * 
 * @galasa.required No
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>zos.tag.[tag].clusterid=plex1</code><br>
 *
 */
public class ClusterIdForTag extends CpsProperties {
    
    public static String get(@NotNull String tag) throws ZosManagerException {
        try {
            return getStringNulled(ZosPropertiesSingleton.cps(), "tag", "clusterid", tag);
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosManagerException("Problem asking the CPS for the cluster id for tag '"  + tag + "'", e);
        }
    }
}
