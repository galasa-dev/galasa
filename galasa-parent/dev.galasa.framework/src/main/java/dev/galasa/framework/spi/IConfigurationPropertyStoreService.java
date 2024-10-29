/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

/**
 * <p>
 * Used to gain access to properties in the Configuration Property Store
 * </p>
 * 
 * <p>
 * The framework will be configured with a single Configuration Property Store
 * where all the global properties are kept. However, for test runs, an override
 * property store will also be used to provide run specific properties.
 * </p>
 * 
 * <p>
 * etcd3 is the preferred property store for Galasa
 * </p>
 * 
 * <p>
 * An {@link IConfigurationPropertyStore} can be obtained from
 * {@link IFramework#getCertificateStoreService()}.
 * </p>
 *
 */
public interface IConfigurationPropertyStoreService {

    /**
     * <p>
     * Retrieves a string property from the Configuration Property Store within the
     * namespace for this object.
     * </p>
     * 
     * <p>
     * getProperty will search the Override Configuration Store first per property
     * iteration and then the standard Configuration Property Store.
     * </p>
     * 
     * <p>
     * As an example, if we called getProperty("image", "credentialid", "PLEXMA",
     * "MVMA") within the zos namespace, then the following properties will be
     * searched for:-<br>
     * zos.image.PLEXMA.MVMA.credentialid in the OCPS <br>
     * zos.image.PLEXMA.MVMA.credentialid in the CPS<br>
     * zos.image.PLEXMA.credentialid in the OCPS<br>
     * zos.image.PLEXMA.credentialid in the CPS<br>
     * zos.image.credentialid in the OCPS<br>
     * zos.image.credentialid in the CPS
     * </p>
     * 
     * <p>
     * If a property is not found, null will be returned.
     * </p>
     * 
     * <p>
     * Retrieved properties and their values will be saved in the Result Archive for
     * diagnostic purposes to understand how the properties should be configured for
     * Managers
     * </p>
     * 
     * @param prefix  The prefix of the property name within the namespace.
     * @param suffix  The suffix of the property name.
     * @param infixes Any optional infixes of the property name.
     * @return
     * @throws ConfigurationPropertyStoreException
     */
    @Null
    String getProperty(@NotNull String prefix, @NotNull String suffix, String... infixes)
            throws ConfigurationPropertyStoreException;
    
    /**
     * Retrieves the properties for the namespace using the supplied prefix.
     * 
     * getProperty will search the Override Configuration Store first.
     * 
     * @param prefix - the prefix to use
     * @return A map of the properties and the values
     * @throws ConfigurationPropertyStoreException - If there is a problem with the fetch
     */
    @NotNull 
    Map<String, String> getPrefixedProperties(@NotNull String prefix) throws ConfigurationPropertyStoreException;

    /**
     * <p>
     * Sets a string property from the Configuration Property Store within the
     * namespace for this object.
     * </p>
     * 
     * <p>
     * setProperty will set the property in the standard Configuration Property Store.
     * </p>
     * 
     * <p>
     * As an example, if we called setProperty("image.PLEXMA.credentialid", "PLEXMACREDS") 
     * within the zos namespace, then the following property will be set:-<br>
     * zos.image.PLEXMA.credentialid=PLEXMACREDS
     * </p>
     * 
     * <p>
     * If a property is not set, a ConfigurationPropertyStoreException is thrown .
     * </p>
     * 
     * <p>
     * Set properties and their values will be saved in the Result Archive for
     * diagnostic purposes to understand how the properties should be configured for
     * Managers
     * </p>
     * 
     * @param name The property name within the namespace.
     * @param value The value of the property.
     * @throws ConfigurationPropertyStoreException
     */
    @Null
    void setProperty(@NotNull String name, @NotNull String value) throws ConfigurationPropertyStoreException;
    
    /**
     * <p>
     * Removes a string property from the Configuration Property Store within the
     * namespace for this object.
     * </p>
     * 
     * <p>
     * deleteProperty will delete the property from the standard Configuration Property Store.
     * </p>
     * 
     * <p>
     * As an example, if we called deleteProperty("image.PLEXMA.credentialid") within the zos
     * namespace, then the following property will be deleted:-<br>
     * zos.image.PLEXMA.credentialid=VALUE
     * </p>
     * 
     * <p>
     * If a property could not be deleted, a ConfigurationPropertyStoreException is thrown.
     * </p>
     * 
     * @param name The property name within the namespace.
     * @throws ConfigurationPropertyStoreException
     */
    void deleteProperty(@NotNull String name) throws ConfigurationPropertyStoreException;

    /**
     * Retrieves all possible different properties set from a namespace
     * 
     * @return Map of names and values of all properties
     * @throws ConfigurationPropertyStoreException - Something went wrong accessing the persistent property store
     */
    Map<String,String> getAllProperties() throws ConfigurationPropertyStoreException;

    /**
     * Retrieves all possible different property variations that would be searched,
     * in the search order.
     * 
     * If a manager cant get a property, it can report all the properties you could
     * set to get a resolve the problem
     * 
     * @param prefix  - The prefix of the property name within the namespace.
     * @param suffix  - The suffix of the property name.
     * @param infixes - Any optional infixes of the property name.
     * @return array of property names
     */
    String[] reportPropertyVariants(@NotNull String prefix, @NotNull String suffix, String... infixes);

    /**
     * <p>
     * Retrieves all possible different property variations that would be searched,
     * in the search order.
     * </p>
     * 
     * <p>
     * If a manager cant get a property, it can report all the properties you could
     * set to get a resolve the problem
     * </p>
     * 
     * @param prefix  - The prefix of the property name within the namespace.
     * @param suffix  - The suffix of the property name.
     * @param infixes - Any optional infixes of the property name.
     * @return comma separated property names
     */
    String reportPropertyVariantsString(@NotNull String prefix, @NotNull String suffix, String... infixes);

    /**
     * <p>
     * Return all namespaces which have properties set
     * </p>
     * 
     * @return List all namespaces with properties set
     * @throws ConfigurationPropertyStoreException - Something went wrong accessing the persistent property store
     */
    List<String> getCPSNamespaces() throws ConfigurationPropertyStoreException;

}
