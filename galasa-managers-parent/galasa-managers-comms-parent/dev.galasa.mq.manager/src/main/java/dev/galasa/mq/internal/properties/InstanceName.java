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
 * The Name of the Queue Manager
 * 
 * @galasa.cps.property
 * 
 * @galasa.name mq.server.[instanceid].name
 * 
 * @galasa.description The queue manager name for the specified tag
 * 
 * @galasa.required Yes
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>mq.server.[instanceid].name=QM1</code><br>
 *
 */
public class InstanceName extends CpsProperties {
    
    public static String get(@NotNull String instanceid) throws MqManagerException {
        try {
            return getStringNulled(MqPropertiesSingleton.cps(), "server", "name", instanceid);
        } catch (ConfigurationPropertyStoreException e) {
            throw new MqManagerException("Problem asking the CPS for the queue manager name for instance '"  + instanceid + "'", e);
        }
    }

}
