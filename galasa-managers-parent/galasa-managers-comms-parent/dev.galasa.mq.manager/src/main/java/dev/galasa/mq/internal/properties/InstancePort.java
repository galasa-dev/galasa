/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.mq.internal.properties;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.mq.MqManagerException;

/**
 * The Port of the Queue Manager
 * 
 * @galasa.cps.property
 * 
 * @galasa.name mq.server.[instanceid].port
 * 
 * @galasa.description The queue manager port for the specified tag
 * 
 * @galasa.required Yes
 * 
 * @galasa.default 1414
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>mq.server.[instanceid].port=1414</code><br>
 *
 */
public class InstancePort extends CpsProperties {
    
    public static int get(@NotNull String instanceid) throws MqManagerException {
    	return getIntWithDefault(MqPropertiesSingleton.cps(), 1414, "server", "port", instanceid);
    }

}
