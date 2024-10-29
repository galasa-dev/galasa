/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.cps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

/**
 * Utility methods for retrieving properties from the Configuration Property
 * Store. Mainly used for the Manager Property Classes pattern.
 */
public class CpsProperties {

    

    /**
     * Retrieve an int from the CPS or return a default if it the property is
     * missing or there is an issue
     * 
     * @param cps          The Configuration Property Store
     * @param defaultValue The default int if property is missing or there is an
     *                     issue
     * @param prefix       The property prefix
     * @param suffix       The property suffix
     * @param infixes      Options infixes
     * @return the found int or default
     */
    protected static int getIntWithDefault(@NotNull IConfigurationPropertyStoreService cps, @NotNull int defaultValue,
            @NotNull String prefix, @NotNull String suffix, String... infixes) {
        try {
            String sValue = cps.getProperty(prefix, suffix, infixes);
            if (sValue == null || sValue.trim().isEmpty()) {
                return defaultValue;
            }

            return Integer.parseInt(sValue.trim());
        } catch (Exception e) {

            Log logger = LogFactory.getLog(CpsProperties.class.getName());
            logger.warn("Invalid property, using default " + defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * Retrieve a String property or return null if missing or an empty string. No
     * default value is provided
     * 
     * @param cps     The Configuration Property Store
     * @param prefix  The property prefix
     * @param suffix  The property suffix
     * @param infixes Options infixes
     * @return the property value trimmed or null if missing
     * @throws ConfigurationPropertyStoreException
     */
    protected static String getStringNulled(@NotNull IConfigurationPropertyStoreService cps, @NotNull String prefix,
            @NotNull String suffix, String... infixes) throws ConfigurationPropertyStoreException {
        String sValue = cps.getProperty(prefix, suffix, infixes);
        if (sValue != null && sValue.trim().isEmpty()) {
            return null;
        }

        return sValue;
    }

    /**
     * Retrieve a string property. If the property is missing or there is an error,
     * the default value is returned.
     * 
     * @param cps          The Configuration Property Store
     * @param defaultValue The default string if property is missing or there is an
     *                     issue
     * @param prefix       The property prefix
     * @param suffix       The property suffix
     * @param infixes      Options infixes
     * @return the trimmed property value or default is missing or there is an error
     */
    @NotNull
    protected static String getStringWithDefault(@NotNull IConfigurationPropertyStoreService cps,
            @NotNull String defaultValue, @NotNull String prefix, @NotNull String suffix, String... infixes) {
        try {
            String sValue = cps.getProperty(prefix, suffix, infixes);
            if (sValue == null || sValue.trim().isEmpty()) {
                return defaultValue;
            }

            return sValue;
        } catch (Exception e) {
            Log logger = LogFactory.getLog(CpsProperties.class.getName());
            logger.warn("Invalid property, using default " + defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * Retrieve an comma separated list string or the default values if the property
     * is missing or there is an error
     * 
     * @param cps           The Configuration Property Store
     * @param defaultValues The default comma separated list if property is missing
     *                      or there is an issue
     * @param prefix        The property prefix
     * @param suffix        The property suffix
     * @param infixes       Options infixes
     * @return a list of properties or the defaults if missing or an error
     */
    protected static @NotNull List<String> getStringListWithDefault(@NotNull IConfigurationPropertyStoreService cps,
            @NotNull String defaultValues, @NotNull String prefix, @NotNull String suffix, String... infixes) {

        try {
            String sValue = cps.getProperty(prefix, suffix, infixes);
            if (sValue == null || sValue.trim().isEmpty()) {
                return splitToList(defaultValues);
            }

            List<String> result = splitToList(sValue);
            if (result.isEmpty()) {
                return splitToList(defaultValues);
            }
            return result;
        } catch (Exception e) {
            Log logger = LogFactory.getLog(CpsProperties.class.getName());
            logger.warn("Invalid property, using default " + defaultValues.toString(), e);
            return splitToList(defaultValues);
        }
    }

    /**
     * Retrieve a comma separated string property or an empty list if missing
     * 
     * @param cps     The Configuration Property Store
     * @param prefix  The property prefix
     * @param suffix  The property suffix
     * @param infixes Options infixes
     * @return a list of properties or Empty list if missing
     * @throws ConfigurationPropertyStoreException
     */
    protected static @NotNull List<String> getStringList(@NotNull IConfigurationPropertyStoreService cps,
            @NotNull String prefix, @NotNull String suffix, String... infixes)
            throws ConfigurationPropertyStoreException {

        String sValue = cps.getProperty(prefix, suffix, infixes);
        if (sValue == null || sValue.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<String> result = splitToList(sValue);
        return result;
    }

    /**
     * Convert a comma separated string into a List. null or empty strings are
     * removed from the list
     * 
     * @param values a comma separated string
     * @return a list of strings
     */
    private static @NotNull List<String> splitToList(@NotNull String values) {
        ArrayList<String> defaults = new ArrayList<>();
        String[] split = values.split(",");
        for (String v : split) {
            if (v != null && !v.trim().isEmpty()) {
                defaults.add(v.trim());
            }
        }
        return defaults;
    }

}
