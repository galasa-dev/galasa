/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

public class CPSNamespace {

    private String name;
    private String propertiesUrl;
    private Visibility visibility;
    private IConfigurationPropertyStoreService propertyStore;

    protected CPSNamespace(@NotNull String name, @NotNull Visibility visibility, @NotNull IFramework framework)
            throws ConfigurationPropertyStoreException {
        this.name = name;
        this.visibility = (visibility);
        this.propertiesUrl = "/"+this.name+"/properties";
        this.propertyStore = framework.getConfigurationPropertyService(name);
    }

    public String getName() {
        return this.name;
    }

    public String getPropertiesUrl() {
        return this.propertiesUrl;
    }

    public Visibility getVisibility() {     
        return this.visibility;
    }

    public boolean isSecure() {
        return this.visibility == Visibility.SECURE;
    }

    public boolean isHidden() {
        return this.visibility == Visibility.HIDDEN;
    }

    public Map<GalasaPropertyName,CPSProperty> getProperties() throws ConfigurationPropertyStoreException{
        Map<GalasaPropertyName,CPSProperty> properties = new HashMap<GalasaPropertyName,CPSProperty>();
        if (visibility != Visibility.HIDDEN){
            Map<String,String> cpsProperties = propertyStore.getAllProperties();
            for (Map.Entry<String,String> property : cpsProperties.entrySet()){

                String propNameWithNamespace = property.getKey();
                GalasaPropertyName propName = new GalasaPropertyName(propNameWithNamespace);

                CPSProperty prop = new CPSProperty(this.propertyStore, this, propName, property.getValue());
                   
                properties.put(propName, prop);
                
            }
        }
        return Collections.unmodifiableMap(properties);
    }

    /**
     * A method to create a CPSProperty object only if the property already exists in the store
     * If the property does not exist in the store a null object will be returned
     * @param propertyName
     * @return CPSProperty
     * @throws ConfigurationPropertyStoreException
     */
    public CPSProperty getProperty(String propertyName) throws ConfigurationPropertyStoreException {
        // 
        // without this the property update property API would always try to create properties that do not exist
        CPSProperty prop = getPropertyFromStore(propertyName);
            if (!prop.existsInStore()) {
                prop = null;
            }
            return prop;
        }

    /**
     * A method to create a CPSProperty within the namespace
     * If the namesapce is hidden, a null property is returned otherwise a property is created.
     * If the property already exists in the store, the property's value from the store will be loaded into the property as well
     * If the property does not exist in the store, the property's value will be null
     * @param propertyName
     * @return CPSProperty
     * @throws ConfigurationPropertyStoreException
     */
    public CPSProperty getPropertyFromStore(String propertyName) throws ConfigurationPropertyStoreException {
        CPSProperty prop = null;
        if (!isHidden()) {
            GalasaPropertyName propName = new GalasaPropertyName(this.name, propertyName);
            prop = new CPSProperty(this.propertyStore, this, propName);
            prop.loadValueFromStore();
        }
        return prop;
    }
}
