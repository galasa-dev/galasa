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
 *
 * Used to gain access to properties in the Configuration Property Store
 * 
 * 
 *
 * The framework will be configured with a single Configuration Property Store
 * where all the global properties are kept. However, for test runs, an override
 * property store will also be used to provide run specific properties.
 *  
 * 
 *
 * etcd3 is the preferred property store for Galasa
 *  
 * 
 *
 * An {@link IConfigurationPropertyStore} can be obtained from
 * {@link IFramework#getCertificateStoreService()}.
 *  
 *
 */
public interface IConfigurationPropertyStoreService {

    /**
     *
     * Retrieves a string property from the Configuration Property Store within the
     * namespace for this object.
     *  
     * 
     *
     * getProperty will search the Override Configuration Store first per property
     * iteration and then the standard Configuration Property Store.
     *  
     * 
     *
     * As an example, if we called getProperty("image", "credentialid", "PLEXMA",
     * "MVMA") within the zos namespace, then the following properties will be
     * searched for:-<br>
     * zos.image.PLEXMA.MVMA.credentialid in the OCPS <br>
     * zos.image.PLEXMA.MVMA.credentialid in the CPS<br>
     * zos.image.PLEXMA.credentialid in the OCPS<br>
     * zos.image.PLEXMA.credentialid in the CPS<br>
     * zos.image.credentialid in the OCPS<br>
     * zos.image.credentialid in the CPS
     *  
     * 
     *
     * If a property is not found, null will be returned.
     *  
     * 
     *
     * Retrieved properties and their values will be saved in the Result Archive for
     * diagnostic purposes to understand how the properties should be configured for
     * Managers
     *  
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
     *
     * Sets a string property from the Configuration Property Store within the
     * namespace for this object.
     *  
     * 
     *
     * setProperty will set the property in the standard Configuration Property Store.
     *  
     * 
     *
     * As an example, if we called setProperty("image.PLEXMA.credentialid", "PLEXMACREDS") 
     * within the zos namespace, then the following property will be set:-<br>
     * zos.image.PLEXMA.credentialid=PLEXMACREDS
     *  
     * 
     *
     * If a property is not set, a ConfigurationPropertyStoreException is thrown .
     *  
     * 
     *
     * Set properties and their values will be saved in the Result Archive for
     * diagnostic purposes to understand how the properties should be configured for
     * Managers
     *  
     * 
     * @param name The property name within the namespace.
     * @param value The value of the property.
     * @throws ConfigurationPropertyStoreException
     */
    @Null
    void setProperty(@NotNull String name, @NotNull String value) throws ConfigurationPropertyStoreException;
    
    /**
     * Sets a collection of given properties into the Configuration Property Store within the namespace
     * for this object.
     * 
     * @param propertiesToSet The collection of key-value pairs to set into this CPS' namespace
     * @throws ConfigurationPropertyStoreException if there was an issue accessing the CPS.
     */
    void setProperties(Map<String, String> propertiesToSet) throws ConfigurationPropertyStoreException;
    
    /**
     *
     * Removes a string property from the Configuration Property Store within the
     * namespace for this object.
     *  
     * 
     *
     * deleteProperty will delete the property from the standard Configuration Property Store.
     *  
     * 
     *
     * As an example, if we called deleteProperty("image.PLEXMA.credentialid") within the zos
     * namespace, then the following property will be deleted:-<br>
     * zos.image.PLEXMA.credentialid=VALUE
     *  
     * 
     *
     * If a property could not be deleted, a ConfigurationPropertyStoreException is thrown.
     *  
     * 
     * @param name The property name within the namespace.
     * @throws ConfigurationPropertyStoreException
     */
    void deleteProperty(@NotNull String name) throws ConfigurationPropertyStoreException;

    /**
     *
     * deletePrefixedProperties will delete all the properties from the standard Configuration Property Store starting the with provided prefix in an atomic operation.
     *  
     * 
     *
     * As an example, if we called deletePrefixedProperties("test.stream.mystream.") within the a
     * namespace, then all the properties starting with "test.stream.mystream." will be deleted
     *  
     * 
     *
     * If a property could not be deleted, a ConfigurationPropertyStoreException is thrown.
     *  
     * 
     * @param prefix The prefix for a property.
     * @throws ConfigurationPropertyStoreException
     */
    void deletePrefixedProperties(@NotNull String prefix) throws ConfigurationPropertyStoreException;

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
     *
     * Retrieves all possible different property variations that would be searched,
     * in the search order.
     *  
     * 
     *
     * If a manager cant get a property, it can report all the properties you could
     * set to get a resolve the problem
     *  
     * 
     * @param prefix  - The prefix of the property name within the namespace.
     * @param suffix  - The suffix of the property name.
     * @param infixes - Any optional infixes of the property name.
     * @return comma separated property names
     */
    String reportPropertyVariantsString(@NotNull String prefix, @NotNull String suffix, String... infixes);

    /**
     *
     * Return all namespaces which have properties set
     *  
     * 
     * @return List all namespaces with properties set
     * @throws ConfigurationPropertyStoreException - Something went wrong accessing the persistent property store
     */
    List<String> getCPSNamespaces() throws ConfigurationPropertyStoreException;

}
