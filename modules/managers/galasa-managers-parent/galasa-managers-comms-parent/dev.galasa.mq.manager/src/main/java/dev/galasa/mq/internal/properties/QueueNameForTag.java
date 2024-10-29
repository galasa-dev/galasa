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
 * The queueName for the tag
 * 
 * @galasa.cps.property
 * 
 * @galasa.name mq.queue.[tag].queuename
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
 * <code>mq.queue.[tag].queuename=GALASA.INPUT.QUEUE</code><br>
 *
 */
public class QueueNameForTag extends CpsProperties {
    
    public static String get(@NotNull String tag) throws MqManagerException {
        try {
            return getStringNulled(MqPropertiesSingleton.cps(), "queue.tag", "queuename", tag);
        } catch (ConfigurationPropertyStoreException e) {
            throw new MqManagerException("Problem asking the CPS for the queuename for tag '"  + tag + "'", e);
        }
    }

}
