/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.example.docker.internal.properties;

import dev.galasa.example.docker.DockerManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * Example Configuration Property
 * 
 * This is an example CPS property that demonstrates how to create configuration
 * properties for your manager. Replace this with actual properties your manager needs.
 * 
 * CPS properties allow administrators and test developers to configure manager
 * behavior without modifying code. Properties are stored in the Configuration
 * Property Store and can be set at different levels (framework, namespace, etc.).
 * 
 * Example property structure:
 * - Property name: dev.galasa.example.docker.example.property
 * - Can be overridden per tag: dev.galasa.example.docker.example.property.[tag]
 * 
 * TODO: Replace this example with actual properties your manager needs.
 * TODO: Update the Javadoc tags below with your property details.
 * TODO: Consider what configuration your manager needs (timeouts, URLs, limits, etc.)
 * 
 * @galasa.cps.property
 * 
 * @galasa.name dev.galasa.example.docker.example.property
 * 
 * @galasa.description An example configuration property for the Docker Manager.
 *                     Replace this with a description of what your property controls.
 * 
 * @galasa.required No
 * 
 * @galasa.default "default-value"
 * 
 * @galasa.valid_values Any string value
 * 
 * @galasa.examples
 * <code>dev.galasa.example.docker.example.property=my-value</code><br>
 * <code>dev.galasa.example.docker.example.property.PRIMARY=primary-value</code>
 */
public class DockerExampleProperty extends CpsProperties {

    /**
     * Retrieves the example property value from the CPS.
     * 
     * This method demonstrates how to retrieve a simple string property.
     * You can create similar methods for different property types (int, boolean, list, etc.)
     * 
     * @param tag the resource tag to use for property lookup (can be null for default)
     * @return the property value, or the default if not set
     * @throws DockerManagerException if there's an error accessing the CPS
     */
    public static String get(String tag) throws DockerManagerException {
        try {
            // Get the property value, with optional tag-specific override
            String value = getStringNulled(
                DockerPropertiesSingleton.cps(),
                "example",
                "property",
                tag
            );

            // Return default value if property not set
            if (value == null) {
                return "default-value";
            }

            return value;
        } catch (ConfigurationPropertyStoreException e) {
            throw new DockerManagerException(
                "Problem accessing the example property from CPS", e);
        }
    }

    /**
     * Retrieves the example property value without a tag.
     * 
     * @return the property value, or the default if not set
     * @throws DockerManagerException if there's an error accessing the CPS
     */
    public static String get() throws DockerManagerException {
        return get(null);
    }

    // TODO: Add more property methods as needed for your manager
    // Examples of other property types:
    //
    // For integer properties:
    // public static int getMaxRetries() throws DockerManagerException {
    //     try {
    //         String value = getStringNulled(DockerPropertiesSingleton.cps(), "max", "retries");
    //         return value == null ? 3 : Integer.parseInt(value);
    //     } catch (Exception e) {
    //         throw new DockerManagerException("Problem accessing max.retries property", e);
    //     }
    // }
    //
    // For boolean properties:
    // public static boolean isEnabled() throws DockerManagerException {
    //     try {
    //         String value = getStringNulled(DockerPropertiesSingleton.cps(), "enabled");
    //         return value == null ? true : Boolean.parseBoolean(value);
    //     } catch (Exception e) {
    //         throw new DockerManagerException("Problem accessing enabled property", e);
    //     }
    // }
}


