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
 * The instanceid  for the tag
 * 
 * @galasa.cps.property
 * 
 * @galasa.name mq.tag.[tag].instanceid
 * 
 * @galasa.description The instance for the specified tag
 * 
 * @galasa.required Yes
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>mq.tag.[tag].instanceid=QUEUEMGR1</code><br>
 *
 */
public class InstanceForTag extends CpsProperties {
    
    public static String get(@NotNull String tag) throws MqManagerException {
        try {
            return getStringNulled(MqPropertiesSingleton.cps(), "tag", "instanceid", tag);
        } catch (ConfigurationPropertyStoreException e) {
            throw new MqManagerException("Problem asking the CPS for the instance id for tag '"  + tag + "'", e);
        }
    }

}
