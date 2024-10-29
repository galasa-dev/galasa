/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cps.rest;

import java.util.*;
import java.util.Map.Entry;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import dev.galasa.extensions.common.api.LogFactory;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStore;

import org.apache.commons.logging.Log;

/**
 * This class is a CPS implementation that delegates calls to the child CPS it gets passed.
 * But it caches responses.
 * 
 * Up-front the implementation reads the entire contents from the CPS and caches it.
 * 
 * Set and Delete of properties are deleted, and the cache state is maintained.
 * 
 * The cache is turned on using the 'framework.cps.rest.cache.is.enabled' property.
 * - true : The cacheing is turned on.
 * - false : Calls pass directly through to the child CPS implementation.
 * Default value: false.
 */
public class CacheCPS implements IConfigurationPropertyStore {

    // The key map key is the fully qualified property name
    // The value is the value of the property.
    private Map<String,String> propertyCache ;

    // We use this flag so that we don't try to prime the cache twice.
    private boolean isCachePrimed = false ;

    private IConfigurationPropertyStore childCPS ;

    private Log log ;

    private boolean isCacheEnabled = false;

    /**
     * The CPS property which this extension draws from to control whether the cache is enabled or not.
     */
    public static final String FEATURE_FLAG_CPS_PROP_CACHED_CPS_ENABLED = "framework.cps.rest.cache.is.enabled";


    public CacheCPS( IConfigurationPropertyStore childCPS , LogFactory logFactory) throws ConfigurationPropertyStoreException {

        this.log = logFactory.getLog(this.getClass());
        this.propertyCache = new HashMap<String,String>();
        this.childCPS = childCPS ;
    }


    private synchronized void primeCaches(IConfigurationPropertyStore childCPS ) throws ConfigurationPropertyStoreException {
        
        // Don't re-prime the caches if they are not primed already.
        if (this.isCachePrimed==false) {

            // Only prime the cache once
            this.isCachePrimed = true ;

            String isEnabledPropValue = childCPS.getProperty(FEATURE_FLAG_CPS_PROP_CACHED_CPS_ENABLED);
            if ((isEnabledPropValue==null)||(isEnabledPropValue.isBlank())) {
                log.info("CPS Cache property "+FEATURE_FLAG_CPS_PROP_CACHED_CPS_ENABLED+" not found in child CPS.");
                this.isCacheEnabled = false ;
            } else {
                log.info("CPS Cache property "+FEATURE_FLAG_CPS_PROP_CACHED_CPS_ENABLED+" has a value of "+isEnabledPropValue);
                this.isCacheEnabled = Boolean.parseBoolean(isEnabledPropValue);
            }

            if (!this.isCacheEnabled) {
                log.info("CPS Cache is not enabled...");
            } else {

                log.info("CPS Cache is enabled, and being primed...");
                List<String> namespaces = childCPS.getNamespaces();

                for( String namespace : namespaces ) {

                    if (!namespace.equals("secure")) {
                        Map<String, String> propertiesFromNamespace = childCPS.getPropertiesFromNamespace(namespace);

                        for( Entry<String,String> propertyInNamespace : propertiesFromNamespace.entrySet()){
                            String propertyValue = propertyInNamespace.getValue();
                            String longPropertyName = propertyInNamespace.getKey();

                            propertyCache.put(longPropertyName,propertyValue);
                        }
                    }
                }
            }
            log.info("CPS Cache primed with "+Integer.toString(propertyCache.size())+" properties.");

        }
    }

    @Override
    public List<String> getNamespaces() throws ConfigurationPropertyStoreException {
        primeCaches(childCPS);
        List<String> results ;
        if (isCacheEnabled) {
            results = new ArrayList<String>();

            // Gather a set of the namespaces, so there are no duplicates.
            Set<String> namespacesSet = new HashSet<>();
            for (Map.Entry<String,String> entry : propertyCache.entrySet()){
                String propertyName = entry.getKey();
                String[] parts = propertyName.split("\\.");
                if (parts.length > 1) {
                    String namespace = parts[0];
                    namespacesSet.add(namespace);
                }
            }

            results.addAll(namespacesSet);
        } else {
            results = this.childCPS.getNamespaces();
        }
        return results;
    }

    @Override
    public @Null String getProperty(@NotNull String fullyQualifiedPropertyName) throws ConfigurationPropertyStoreException {
        primeCaches(childCPS);
        String result ;
        if (isCacheEnabled) {
            result = propertyCache.get(fullyQualifiedPropertyName);
        } else {
            result = this.childCPS.getProperty(fullyQualifiedPropertyName);
        }
        return result ;
    }

    @Override
    public void setProperty(@NotNull String key, @NotNull String value) throws ConfigurationPropertyStoreException {
        primeCaches(childCPS);

        // Delegate the set of the property to the child CPS
        childCPS.setProperty(key, value);

        if (isCacheEnabled) {
            // The child changed the property value ok, so we should change the cache version also.
            propertyCache.put(key,value);
        }
    }

    @Override
    public @NotNull Map<String, String> getPrefixedProperties(@NotNull String prefix)
            throws ConfigurationPropertyStoreException {
        
        primeCaches(childCPS);

        Map<String, String> results;
        if (isCacheEnabled) {
            results = new HashMap<String, String>();
            for( Entry<String,String> property : this.propertyCache.entrySet() ){
                String propName = property.getKey();
                if( propName.startsWith(prefix)) {
                    String propValue = property.getValue();
                    results.put(propName, propValue);
                }
            }
        } else {
            results = this.childCPS.getPrefixedProperties(prefix);
        }
        return results;
    }

    @Override
    public void deleteProperty(@NotNull String key) throws ConfigurationPropertyStoreException {

        primeCaches(childCPS);

        // Delegate the delete to the underlying CPS.
        this.childCPS.deleteProperty(key);

        if (this.isCacheEnabled) {
            // Keep our cache in step.
            propertyCache.remove(key);
        }
    }

    @Override
    public Map<String, String> getPropertiesFromNamespace(String namespace) throws ConfigurationPropertyStoreException {

        primeCaches(childCPS);

        Map<String, String> results ;
        if (this.isCacheEnabled) {
            results = getPrefixedProperties(namespace);
        } else {
            results = childCPS.getPropertiesFromNamespace(namespace);
        }
        return results;
    }

    @Override
    public void shutdown() throws ConfigurationPropertyStoreException {

        // Delegate this stimulus to the child, to give that a chance of closing resources.
        childCPS.shutdown();
    }
    
}
