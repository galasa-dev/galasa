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
 * @galasa.description Provides the path to the JMeter binary file
 *
 * @galasa.required Yes (when using LOCAL execution mode)
 *
 * @galasa.valid_values A valid file path to the JMeter binary executable
 *
 * @galasa.examples
 * <code>jmeter.binary.path=/opt/apache-jmeter-5.6.3/bin/jmeter</code><br>
 * <code>jmeter.binary.path=C:\apache-jmeter-5.6.3\bin\jmeter.bat</code>
 *
 * @galasa.extra
 * The path must point directly to the JMeter binary file, not to an installation directory:<br>
 * <ul>
 * <li>Unix/Linux/Mac: <code>/path/to/apache-jmeter-x.x.x/bin/jmeter</code></li>
 * <li>Windows: <code>C:\path\to\apache-jmeter-x.x.x\bin\jmeter.bat</code></li>
 * </ul>
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
