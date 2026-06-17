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
 * @galasa.name jmeter.execution.mode
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
 * <code>jmeter.execution.mode=LOCAL<br>
 * jmeter.execution.mode=DOCKER
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

    private static final String MODE_LOCAL = "LOCAL";
    private static final String MODE_DOCKER = "DOCKER";
    
    /**
     * Get the JMeter execution mode from CPS
     *
     * @return The execution mode (LOCAL or DOCKER), defaults to LOCAL if not set
     * @throws JMeterManagerException if there is an error accessing CPS
     */
    public static String get() throws JMeterManagerException {
        String result = MODE_LOCAL;
        
        try {
            String mode = getStringNulled(JMeterPropertiesSingleton.cps(), "execution", "mode");
            
            if (mode != null && !mode.trim().isEmpty()) {
                // Validate the mode
                String upperMode = mode.trim().toUpperCase();
                if (!upperMode.equals(MODE_LOCAL) && !upperMode.equals(MODE_DOCKER)) {
                    throw new JMeterManagerException(
                        "Invalid jmeter.execution.mode value: " + mode + ". Must be LOCAL or DOCKER");
                }
                result = upperMode;
            }
        } catch (ConfigurationPropertyStoreException e) {
            throw new JMeterManagerException("Failed to retrieve jmeter.execution.mode property", e);
        }
        
        return result;
    }
}
