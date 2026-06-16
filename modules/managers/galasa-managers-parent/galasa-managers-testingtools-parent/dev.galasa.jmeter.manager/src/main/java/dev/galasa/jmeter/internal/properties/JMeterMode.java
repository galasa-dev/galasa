/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.jmeter.internal.properties;

import dev.galasa.jmeter.JMeterManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * JMeter Execution Mode CPS Property
 *
 * @galasa.cps.property
 *
 * @galasa.name jmeter.mode.execution
 *
 * @galasa.description Specifies the execution mode for JMeter tests
 *
 * @galasa.required No
 *
 * @galasa.default LOCAL
 *
 * @galasa.valid_values LOCAL, DOCKER
 *
 * @galasa.examples
 * <code>jmeter.mode.execution=LOCAL<br>
 * jmeter.mode.execution=DOCKER
 * </code>
 *
 * @galasa.extra
 * The JMeter Manager supports two execution modes:<br>
 * <ul>
 * <li><b>LOCAL</b> - Uses an external JMeter binary installation (requires jmeter.binary.path property). This is the default mode.</li>
 * <li><b>DOCKER</b> - Uses a Docker container with JMeter installed. Requires Docker Manager.</li>
 * </ul>
 * LOCAL mode is recommended for most use cases as it provides flexibility in JMeter version selection.
 */
public class JMeterMode extends CpsProperties {
    
    /**
     * Get the JMeter execution mode from CPS
     *
     * @return The execution mode (LOCAL or DOCKER), defaults to LOCAL if not set
     * @throws JMeterManagerException if there is an error accessing CPS
     */
    public static String get() throws JMeterManagerException {
        String result = "LOCAL";
        
        try {
            String mode = getStringNulled(JMeterPropertiesSingleton.cps(), "mode", "execution");
            
            if (mode != null && !mode.trim().isEmpty()) {
                // Validate the mode
                String upperMode = mode.trim().toUpperCase();
                if (!upperMode.equals("LOCAL") && !upperMode.equals("DOCKER")) {
                    throw new JMeterManagerException(
                        "Invalid jmeter.mode.execution value: " + mode + ". Must be LOCAL or DOCKER");
                }
                result = upperMode;
            }
        } catch (ConfigurationPropertyStoreException e) {
            throw new JMeterManagerException("Failed to retrieve jmeter.mode.execution property", e);
        }
        
        return result;
    }
}
