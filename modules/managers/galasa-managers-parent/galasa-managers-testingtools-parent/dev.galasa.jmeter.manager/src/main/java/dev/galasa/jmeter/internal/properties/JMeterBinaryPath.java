/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.jmeter.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.jmeter.JMeterManagerException;

/**
 * JMeter Binary Path CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name jmeter.binary.path
 * 
 * @galasa.description Provides a path to the JMeter binary installation directory
 * 
 * @galasa.required Yes (when using binary execution mode)
 * 
 * @galasa.valid_values A valid String representation of a path to JMeter installation
 * 
 * @galasa.examples 
 * <code>jmeter.binary.path=/opt/apache-jmeter-5.6.3</code>
 * <code>jmeter.binary.path=/usr/local/jmeter</code>
 * 
 */
public class JMeterBinaryPath extends CpsProperties {

    public static String get() throws ConfigurationPropertyStoreException, JMeterManagerException {
        String path = getStringNulled(JMeterPropertiesSingleton.cps(), "binary", "path");
        if (path == null) {
            throw new JMeterManagerException("No path provided for JMeter binary. Set jmeter.binary.path property");
        }
        return path;
    }

}
