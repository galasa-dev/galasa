/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.mq.internal.properties;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.mq.MqManagerException;

/**
 * The Host for the queueManager
 * 
 * @galasa.cps.property
 * 
 * @galasa.name mq.server.[instanceid].host
 * 
 * @galasa.description The channel for the specified tag
 * 
 * @galasa.required Yes
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>mq.server.[instanceid].host=127.0.0.1</code><br>
 *
 */
public class InstanceHost extends CpsProperties {
    
    public static String get(@NotNull String instanceid) throws MqManagerException {
        try {
            return getStringNulled(MqPropertiesSingleton.cps(), "server", "host", instanceid);
        } catch (ConfigurationPropertyStoreException e) {
            throw new MqManagerException("Problem asking the CPS for the host address for tag '"  + instanceid + "'", e);
        }
    }

}
