/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.cps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStore;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;

/**
 * This class is used to drive the registered CPS service, and retireve values
 * from the CPS Store, impletmenting the IConfiguration interface.
 * 
 *  
 *  
 */
public class FrameworkConfigurationPropertyService implements IConfigurationPropertyStoreService {
    private String                      namespace;
    private Properties                  record;
    private Properties                  overrides;
    private IConfigurationPropertyStore cpsStore;

    /**
     * <p>
     * This constructor ensures that the registered CPS service (fpf or etcd3) is
     * known, as wells as accepting the overrides and record properties. Namespace
     * of the manager which is using this class is also registered at this point.
     * </p>
     * 
     * @param framework  - not currently used.
     * @param cpsStore - the registered service for the CPS
     * @param overrides  - property values to be selected as preference from these
     *                   properties
     * @param record     - a properties object for recording the keys accessed and
     *                   where from '&lt;Location&gt;=&lt;KeyAccessed&gt;'
     * @param namespace  - The name space for keys for a specfic manager
     */
    public FrameworkConfigurationPropertyService(IFramework framework, IConfigurationPropertyStore cpsStore,
            Properties overrides, Properties record, String namespace) {
        this.namespace = namespace;
        this.record = record;
        this.overrides = overrides;
        this.cpsStore = cpsStore;
    }

    /**
     * <p>
     * This method returns a String value for a key requested. This looks for any
     * key that has a non null value through the hierachical structure of the keys
     * in the CPS: zos.image.PLEXMA.MVMA.credentialid zos.image.PLEXMA.credentialid
     * zos.image.credentialid With the infixes of PLEXMA and MVMA being preffered.
     * List in order of checking. All values checked are also checked in the
     * overrides properties, which as expceted should override any value in the CPS.
     * </p>
     * 
     * @param prefix  - in the above example is "image"
     * @param suffix  - in the above example is "credentialid"
     * @param infixes - the hierachal structure above, supplied in order, e.g
     *                "PLEXMA", "MVMA"
     * @return - string value for the key requested
     * @throws ConfigurationPropertyStoreException - throws the caught exception
     *                                             from the
     *                                             getValueAndMakeAccessRecord()
     *                                             method.
     */
    public String getProperty(@NotNull String prefix, @NotNull String suffix, String... infixes)
            throws ConfigurationPropertyStoreException {
        if (infixes == null) {
            infixes = new String[0];
        }
        String[] keys = createOrderedKeyList(prefix, suffix, infixes);
        String value = null;
        for (String key : keys) {
            value = getValueAndMakeAccessRecord(key);
            if (value != null) {
                return value.trim();
            }
        }
        return null;
    }

    /**
     * <p>
     * This method sets a cps property with a given name and value in the provided namespace
     * </p>
     * 
     * @return all properties from a given namespace
     * @throws ConfigurationPropertyStoreException 
     */
    public Map<String,String> getAllProperties() throws ConfigurationPropertyStoreException {
        return cpsStore.getPropertiesFromNamespace(namespace);
    }

    /**
     * <p>
     * This method sets a cps property with a given name and value in the provided namespace
     * </p>
     * 
     * @param name
     * @param value
     */
    public void setProperty(@NotNull String name, @NotNull String value)
            throws ConfigurationPropertyStoreException {
    	cpsStore.setProperty(namespace + "." + name, value);
    }
    
    /**
     * <p>
     * This method deletes a cps propetty with a given name within the provided namespace
     * </p>
     * 
     * @param name
     */
    public void deleteProperty(@NotNull String name) 
            throws ConfigurationPropertyStoreException {
        cpsStore.deleteProperty(namespace + "." + name);
        
    }

    /**
     * <p>
     * This method retrievs all the keys that would be serached in the order that
     * they would be searched. For example: zos.image.PLEXMA.MVMA.credentialid
     * zos.image.PLEXMA.credentialid zos.image.credentialid
     * </p>
     * 
     * @param prefix  - in the above example is "image"
     * @param suffix  - in the above example is "credentialid"
     * @param infixes - the hierachal structure above, supplied in order, e.g
     *                "PLEXMA", "MVMA"
     * @return - an array of the keys as strings in the order they would be serached
     */
    public String[] reportPropertyVariants(@NotNull String prefix, @NotNull String suffix, String... infixes) {
        if (infixes == null) {
            infixes = new String[0];
        }
        return createOrderedKeyList(prefix, suffix, infixes);
    }

    /**
     * <p>
     * This method retrievs all the keys that would be serached in the order that
     * they would be searched. For example: zos.image.PLEXMA.MVMA.credentialid
     * zos.image.PLEXMA.credentialid zos.image.credentialid
     * </p>
     * 
     * @param prefix  - in the above example is "image"
     * @param suffix  - in the above example is "credentialid"
     * @param infixes - the hierachal structure above, supplied in order, e.g
     *                "PLEXMA", "MVMA"
     * @return - comma separated property names in the order they would be serached
     */
    public String reportPropertyVariantsString(@NotNull String prefix, @NotNull String suffix, String... infixes) {
        String[] variants = reportPropertyVariants(prefix, suffix, infixes);

        StringBuilder sb = new StringBuilder();
        for (String variant : variants) {
            if (sb.length() == 0) {
                sb.append("[");
            } else {
                sb.append(",");
            }
            sb.append(variant);
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * <p>
     * This private method is used by the getProperty() method to return a String
     * value for a given key and record any key/value accessed. This method insures
     * it does the check of the overrides properties first before checking any
     * cpsService.
     * </p>
     * 
     * @param key - generated from the prefix, suffix and infixes provided to the
     *            getProprty().
     * @return - String value returned from either CPS store or the overrides.
     * @throws ConfigurationPropertyStoreException - throws an exception is the
     *                                             registered CPS service is not
     *                                             available.
     */
    private String getValueAndMakeAccessRecord(String key) throws ConfigurationPropertyStoreException {
        String value;

        value = overrides.getProperty(key);
        if (value != null) {
            recordPropertyAccessed(key,value,"overrides");
            return value;
        }

        value = cpsStore.getProperty(key);
        if (value != null) {
            recordPropertyAccessed(key,value,"cps");
            return value;
        }
        recordPropertyAccessed(key,"*** MISSING ***","missing");
        return null;
    }

    /**
     * <p>
     * This method creates an array filled in order of all the keys to try and find
     * in both the overrides properties or the CPS store. The order of this keyList
     * is important as it will be used by the getProprty() method.
     * </p>
     * 
     * @param prefix  - in the above example is "image"
     * @param suffix  - in the above example is "credentialid"
     * @param infixes - the hierachal structure above, supplied in order, e.g
     *                "PLEXMA", "MVMA"
     * @return - String array of the keys in the correct order to check from 0-x
     */
    private String[] createOrderedKeyList(String prefix, String suffix, String... infixes) {
        String[] keys = new String[(infixes.length + 1)];
        String[] infixOrder = getInfixOrder(infixes);

        for (int i = 0; i < infixOrder.length; i++) {
            keys[i] = namespace + "." + prefix + infixOrder[i] + suffix;
        }

        keys[infixes.length] = namespace + "." + prefix + "." + suffix;

        return keys;
    }

    /**
     * This method takes passed infixes and generates the list of infixes of
     * hierachy order, which is will be surrounded by the prefix and suffix to
     * genterate the kys to be searched.
     * 
     * @param infixes - all the heirachy levels that could be appended to the
     *                namespace and prefix.
     * @return - string array of the infixes that need to be checked.
     */
    private String[] getInfixOrder(String... infixes) {
        String[] infixOrderList = new String[infixes.length];
        for (int i = 0; i < infixes.length; i++) {
            StringBuilder majorInfix = new StringBuilder();
            for (int j = 0; j < (infixes.length - i); j++) {
                majorInfix.append("." + infixes[j]);
            }
            majorInfix.append(".");
            infixOrderList[i] = majorInfix.toString();
        }
        return infixOrderList;
    }

    public List<String> getCPSNamespaces() throws ConfigurationPropertyStoreException {
        return cpsStore.getNamespaces();
    }
    
    @Override
    public Map<String, String> getPrefixedProperties(@NotNull String prefix)
            throws ConfigurationPropertyStoreException {
        
        HashMap<String, String> returnValues = new HashMap<>();
        
        String fullPrefix = this.namespace + "." + prefix;        
        //*** First build from the overrides
        for(Entry<Object, Object> entry : this.overrides.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            
            if (key.startsWith(fullPrefix)) {
                recordPropertyAccessed(key,value,"overrides");
                
                key = key.substring(this.namespace.length() + 1);
                returnValues.put(key, value);
            }
        }
        
        //*** Now get them from the store
        Map<String, String> cpsEntries = this.cpsStore.getPrefixedProperties(fullPrefix);
        for(Entry<String, String> entry : cpsEntries.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            String unPrefixedKey = key.substring(this.namespace.length() + 1);
            
            if (!returnValues.containsKey(unPrefixedKey)) {
                recordPropertyAccessed(key,value,"cps");
                
                returnValues.put(unPrefixedKey, value);
            }
        }
        
        return returnValues;
    }

    private void recordPropertyAccessed(String key, String valueObtained , String whereValueCameFrom ) {
        this.record.setProperty(key, valueObtained);
        this.record.setProperty(key+"._source",whereValueCameFrom);
    }       

}