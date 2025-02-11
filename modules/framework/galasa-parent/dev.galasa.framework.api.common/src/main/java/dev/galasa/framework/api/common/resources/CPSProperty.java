/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.beans.GalasaProperty;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;


import static dev.galasa.framework.api.common.ServletErrorMessage.*;

public class CPSProperty {

    private static final String REDACTED_PROPERTY_VALUE = "********";

    private CPSNamespace namespace;
    private GalasaPropertyName name;
    private IConfigurationPropertyStoreService store;
    private String value;

    public CPSProperty(IConfigurationPropertyStoreService store, CPSNamespace namespace, GalasaPropertyName propertyName) {
        this.namespace = namespace ;
        this.store = store ;
        this.name = propertyName;
    }

    public CPSProperty(IConfigurationPropertyStoreService store, CPSNamespace namespace, GalasaPropertyName propertyName, String value) {
        this(store, namespace,propertyName);
        this.value = value;
    }

    public CPSProperty(GalasaProperty property){
        this(property.getNamespace()+"."+property.getName(),property.getValue());
    }

    public CPSProperty (String completeCPSname, String propertyValue) {
        String[] nameParts = completeCPSname.split("[.]", 2);
        String namespaceName = nameParts[0];
        String propertyName = nameParts[1];
        this.name = new GalasaPropertyName(namespaceName, propertyName);
        this.value = propertyValue;
    }

    public CPSProperty (Map.Entry<String, String> propertyEntry) {
        this(propertyEntry.getKey(),propertyEntry.getValue());
    }

    public CPSProperty (String namespace, String propertyName, String propertyValue) {
        this.name = new GalasaPropertyName(namespace, propertyName);
        this.value = propertyValue;
    }

    public String getNamespace() {
        return this.name.getNamespaceName();
    }

    public String getQualifiedName() {
        return this.getNamespace()+"."+this.getName();
    }

    public String getName() {
        return this.name.getSimpleName();
    }

    public String getValue() {
        return this.value;
    }

    public String getPossiblyRedactedValue() {
        String outputValue  = this.value;
        if (namespace.isSecure()){
            outputValue = REDACTED_PROPERTY_VALUE;
        }
        return outputValue;
    }

    protected void loadValueFromStore() throws ConfigurationPropertyStoreException {
        // load the value from the property store into this property object.
        // Will be null if the property isn't in the store yet.
        this.value = store.getProperty(name.getLongestPrefix(), name.getShortestSuffix());

        if (this.value != null) {
            if ( isSecure() ){
                this.value = REDACTED_PROPERTY_VALUE;
            }
        }
    }

    public void setPropertyToStore(GalasaProperty galasaProperty,boolean updateProperty) throws ConfigurationPropertyStoreException, InternalServletException {
        boolean propExists = existsInStore();
        /*
         * Logic Table to Determine actions
         * Create Property - The property must not already Exist i.e. propExists == false, updateProperty == false
         * Update Property - The property must exist i.e. propExists == true, updateProperty == true
         * Therefore setting updateProperty to false will force a create property path,
         * whilst setting updateProperty to true will force an update property path
         */
        if (propExists == updateProperty) {
            store.setProperty(this.getName(), galasaProperty.getValue());
        } else if (propExists){
            ServletError error = new ServletError(GAL5018_PROPERTY_ALREADY_EXISTS_ERROR, this.getName(), this.getNamespace());  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        } else {
            ServletError error = new ServletError(GAL5017_PROPERTY_DOES_NOT_EXIST_ERROR, this.getName());  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
    }

    public void deletePropertyFromStore() throws InternalServletException{
        try {
            store.deleteProperty(this.getName());
        } catch (ConfigurationPropertyStoreException e) {
            ServletError error = new ServletError(GAL5030_UNABLE_TO_DELETE_PROPERTY_ERROR, this.getName());  
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
    }

    public boolean existsInStore() {
        return this.value != null;
    }

    private boolean isSecure() {
        return this.namespace.isSecure();
    }

    private boolean isPropertyNameValid() {
        return this.name.simpleName != null && !this.name.simpleName.isBlank();
    }

    private boolean isPropertyNameSpaceValid() {
        return this.name.namespaceName != null && !this.name.namespaceName.isBlank();
    }

    private boolean isPropertyValueValid() {
        return this.value != null ;
    }

    public boolean isPropertyValid() throws InternalServletException {
        ServletError error = null;
        if (!this.isPropertyNameValid()) {
            error = new ServletError(GAL5024_INVALID_GALASAPROPERTY,"name",this.name.simpleName);
        }
        if (!this.isPropertyNameSpaceValid()) {
            error = new ServletError(GAL5024_INVALID_GALASAPROPERTY,"namespace",this.name.namespaceName);
        }
        if (!this.isPropertyValueValid()) {
            error = new ServletError(GAL5024_INVALID_GALASAPROPERTY,"value",this.value);
        }
        if (error != null) {
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return true;
    }
}
