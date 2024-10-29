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
 * The credentials for the queueManager
 * 
 * @galasa.cps.property
 * 
 * @galasa.name mq.server.[instanceid].credentials.id
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
 * <code>mq.server.[instanceid].credentials.id=CRED1</code><br>
 *
 */
public class InstanceCredentials extends CpsProperties {
    
    public static String get(@NotNull String instanceid) throws MqManagerException {
        try {
            return getStringNulled(MqPropertiesSingleton.cps(), "server", "credentials.id", instanceid);
        } catch (ConfigurationPropertyStoreException e) {
            throw new MqManagerException("Problem asking the CPS for the credentials ID for instance: '"  + instanceid + "'", e);
        }
    }

}
