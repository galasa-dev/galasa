/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.routes;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.beans.GalasaProperty;
import dev.galasa.framework.api.beans.GalasaPropertyData;
import dev.galasa.framework.api.beans.GalasaPropertyMetadata;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ProtectedRoute;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.SupportedQueryParameterNames;
import dev.galasa.framework.api.common.resources.CPSFacade;
import dev.galasa.framework.api.common.resources.CPSNamespace;
import dev.galasa.framework.api.common.resources.CPSProperty;
import dev.galasa.framework.api.common.resources.GalasaPropertyName;
import dev.galasa.framework.api.common.resources.ResourceNameValidator;
import dev.galasa.framework.api.common.resources.beans.GalasaBeanSerialiser;
import dev.galasa.framework.api.cps.internal.common.PropertyComparator;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.utils.GalasaGson;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

/**
 * An abstract route used by all the Run-related routes.
 */
public abstract class CPSRoute extends ProtectedRoute {

    public static final String QUERY_PARAMETER_PREFIX = "prefix";
    public static final String QUERY_PARAMETER_INFIX = "infix";
    public static final String QUERY_PARAMETER_SUFFIX = "suffix";
    public static final SupportedQueryParameterNames SUPPORTED_QUERY_PARAMETER_NAMES = new SupportedQueryParameterNames(
        QUERY_PARAMETER_PREFIX,
        QUERY_PARAMETER_INFIX,
        QUERY_PARAMETER_SUFFIX
    );
    
    static final ResourceNameValidator nameValidator = new ResourceNameValidator();
    static final GalasaGson gson = new GalasaGson();

    static final GalasaBeanSerialiser beanSerialiser = new GalasaBeanSerialiser();

    // Define a default filter to accept everything
    static DirectoryStream.Filter<Path> defaultFilter = path -> { return true; };

    protected IFramework framework;
    CPSFacade cps;

    public CPSRoute(ResponseBuilder responseBuilder, String path, IFramework framework) throws RBACException {
        super(responseBuilder, path, framework.getRBACService());
        this.framework = framework;
    }

    protected  boolean checkPropertyNamespaceMatchesURLNamespace(@NotNull CPSProperty property , @NotNull String namespace){
        return namespace.toLowerCase().trim().equals(property.getNamespace().toLowerCase().trim());
    }

     /**
     * Returns a boolean value of whether the property has been located in the given namespace.
     * Hidden namespaces will return a false value as they should not be accessed via the API endpoints
     * 
     * @param namespace
     * @param propertyName
     * @return boolean
     * @throws InternalServletException
     */
    public boolean checkPropertyExists (String namespace, String propertyName) throws InternalServletException{
        return retrieveSingleProperty(namespace, propertyName) != null;
    }

    /**
     * Returns a single property from a given namespace.
     * If the namespace provided is hidden, does not exist or has no matching property, it returns null
     * If the namespace provided does not match any existing namepsaces an exception will be thrown
     * @param namespaceName
     * @param propertyName
     * @return Map.Entry of String, String
     * @throws InternalServletException
     */
    public CPSProperty retrieveSingleProperty(String namespaceName, String propertyName) throws  InternalServletException {
        CPSProperty property;
        try {
            cps = new CPSFacade(this.framework);
        } catch( ConfigurationPropertyStoreException ex ) {
            ServletError error = new ServletError(GAL5000_GENERIC_API_ERROR);  
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,ex);
        }

        CPSNamespace namespace ;
        try {
            namespace = cps.getNamespace(namespaceName);
        }catch (Exception e){
            ServletError error = new ServletError(GAL5016_INVALID_NAMESPACE_ERROR,namespaceName);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND,e);
        }
        
        if (namespace.isHidden()){
            ServletError error = new ServletError(GAL5016_INVALID_NAMESPACE_ERROR,namespaceName);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        } 

        
        try{
            property = namespace.getPropertyFromStore(propertyName);
        }catch (Exception e){
            ServletError error = new ServletError(GAL5016_INVALID_NAMESPACE_ERROR,namespaceName);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND,e);
        }
        
        return property;
    }


    protected boolean checkNameMatchesRequest(String name, String jsonString ) throws ConfigurationPropertyStoreException, InternalServletException {
        boolean valid = false;
        String propertyName = "";
        try {
            GalasaProperty galasaProperty = beanSerialiser.getPropertyFromJsonString(jsonString);
            propertyName = galasaProperty.getName();
            if (propertyName.equals(name)) {
                valid = true;
            }
        } catch (Exception e ) {
            //Catch the Exception can not convert to GalasaProperty (due to missing data or bad name)
        }  
        if (!valid) {
            ServletError error = new ServletError(GAL5029_PROPERTY_NAME_DOES_NOT_MATCH_ERROR,propertyName,name );  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
        return valid;
    }

    protected CPSProperty applyPropertyToStore (String jsonString, String namespaceName , boolean isUpdateAction) throws IOException, FrameworkException{
        GalasaProperty galasaProperty = beanSerialiser.getPropertyFromJsonString(jsonString);
        CPSFacade cps = new CPSFacade(framework);
        CPSNamespace namespace = cps.getNamespace(galasaProperty.getNamespace());
        nameValidator.assertNamespaceCharPatternIsValid(galasaProperty.getNamespace());
        if (namespace.isHidden()) {
            ServletError error = new ServletError(GAL5016_INVALID_NAMESPACE_ERROR,namespaceName);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
        nameValidator.assertPropertyNameCharPatternIsValid(galasaProperty.getName());
        CPSProperty property = namespace.getPropertyFromStore(galasaProperty.getName());
        if(!checkPropertyNamespaceMatchesURLNamespace(property, namespaceName)){
            ServletError error = new ServletError(GAL5028_PROPERTY_NAMESPACE_DOES_NOT_MATCH_ERROR,property.getNamespace(), namespaceName);  
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
        property.setPropertyToStore(galasaProperty, isUpdateAction);
        return property;
    }

    protected  boolean checkNamespaceExists(String namespaceName) throws ConfigurationPropertyStoreException, InternalServletException {
        boolean valid = false;
        try {
            CPSFacade cps = new CPSFacade(framework);
            CPSNamespace namespace = cps.getNamespace(namespaceName);
            if (namespace.getProperties().size() > 0) {
                valid = true;
            }
        } catch (Exception e ) {
            //Catch the Exception (namespace is invalid) to throw error in if 
        }  
        if (!valid) {
            ServletError error = new ServletError(GAL5016_INVALID_NAMESPACE_ERROR,namespaceName);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
        return valid;
    }

    /**
     * Sort the properties provided by key 
     * @param properties
     * @return Sorted Map of properties
     */
    protected Map<GalasaPropertyName, CPSProperty> sortPropertiesByPropertyName(Map<GalasaPropertyName, CPSProperty> properties){
        Collection<GalasaPropertyName> unsortedKeys = properties.keySet();
        PropertyComparator comparator = new PropertyComparator();
        Map<GalasaPropertyName, CPSProperty> sorted = new TreeMap<GalasaPropertyName, CPSProperty>(comparator);

        for( GalasaPropertyName key : unsortedKeys ) {
            sorted.put(key, properties.get(key));
        }

        return sorted;
    }
    
    protected String getPropertyNameFromURL(String pathInfo) throws InternalServletException {
        /*
         * This expects a pathInfo from the cps property endpoint, i.e
         * /cps/<namespace>/properties/<propertyName>
         * This means that the minimum length is going to be 4
         * meaning we should not have an IndexOutOfBoundsException 
         */
        try {
            String[] namespace = pathInfo.split("/");
            return namespace[3];
        } catch (Exception e) {
            ServletError error = new ServletError(GAL5000_GENERIC_API_ERROR);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND, e);
        }
    }

    protected String getNamespaceFromURL(String pathInfo) throws InternalServletException {
        /*
         * This expects a pathInfo from the cps endpoints, i.e.
         * /cps/<namespace>
         * /cps/<namespace>/properties
         * /cps/<namespace>/properties?prefix=<prefix>&suffix=<suffix>
         * /cps/<namespace>/properties/<propertyName>
         * This means that the minimum length is going to be 2
         * meaning we should not have an IndexOutOfBoundsException 
         */
        try {
            String[] namespace = pathInfo.split("/");
            return namespace[1];
        } catch (Exception e) {
            ServletError error = new ServletError(GAL5000_GENERIC_API_ERROR);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND, e);
        }
    }

    protected Map<GalasaPropertyName, CPSProperty> getProperties(CPSNamespace namespace, String prefix, String suffix, List<String> infixes) throws ConfigurationPropertyStoreException {
        Map<GalasaPropertyName, CPSProperty> properties = namespace.getProperties();
        
        if (prefix != null){
            properties = filterPropertiesByPrefix(namespace.getName(), properties,prefix);
        }
        if (suffix != null){
            properties = filterPropertiesBySuffix(properties,suffix);
        }
        if (infixes != null){
            properties = filterPropertiesByInfix(properties, infixes);
        }
        Map<GalasaPropertyName, CPSProperty> sortedProperties = sortPropertiesByPropertyName(properties);
        return sortedProperties;
    }
    
    /**
     * Filter a map of provided properties by checking that the properties start with namespace.prefix 
     * using the supplied paramenters
     * @param namespace
     * @param properties
     * @param prefix
     * @return Map of Properties starting with the provided prefix
     */
    protected  Map<GalasaPropertyName, CPSProperty> filterPropertiesByPrefix(String namespace, Map<GalasaPropertyName, CPSProperty> properties , String prefix){
        Map<GalasaPropertyName, CPSProperty> filteredProperties = new HashMap<GalasaPropertyName, CPSProperty>();
        for (Map.Entry<GalasaPropertyName, CPSProperty> entry : properties.entrySet()) {
            if (entry.getKey().getFullyQualifiedName().startsWith(namespace + "."+prefix)){
                filteredProperties.put(entry.getKey(), entry.getValue());
            }
        }
        return filteredProperties;
    }

    /**
     * Filter a map of provided properties by checking that the properties end with the supplied prefix
     * @param properties
     * @param suffix
     * @return Map of Properties ending with the provided suffix
     */
    protected  Map<GalasaPropertyName, CPSProperty> filterPropertiesBySuffix( Map<GalasaPropertyName, CPSProperty> properties , String suffix){
       Map<GalasaPropertyName, CPSProperty> filteredProperties = new HashMap<GalasaPropertyName, CPSProperty>();
        for (Map.Entry<GalasaPropertyName, CPSProperty> entry : properties.entrySet()) {
            if (entry.getKey().getFullyQualifiedName().endsWith(suffix)){
                filteredProperties.put(entry.getKey(), entry.getValue());
            }
        }
        return filteredProperties;
    }

    /**
     * Filter a map of provided properties by checking that the properties contain and match at least of the
     * supplied infixes 
     * @param properties
     * @param infixes
     * @return Map of Properties containing the at least one of the infixes
     */
    protected  Map<GalasaPropertyName, CPSProperty> filterPropertiesByInfix(Map<GalasaPropertyName, CPSProperty>properties, List<String> infixes){
        Map<GalasaPropertyName, CPSProperty> filteredProperties = new HashMap<GalasaPropertyName, CPSProperty>();
        for (Map.Entry<GalasaPropertyName, CPSProperty> entry : properties.entrySet()) {
            GalasaPropertyName key = entry.getKey();
            for (String infix : infixes){
				if (key.getFullyQualifiedName().contains(infix)&& !filteredProperties.containsKey(key)){
                    filteredProperties.put(entry.getKey(), entry.getValue());
	            }
			}
        }
        return filteredProperties;
    }

    protected String buildResponseBody(Map<GalasaPropertyName, CPSProperty> properties) {
        /*
         * Builds a json array object from a Map of properties
         */
        List<GalasaProperty> results = new ArrayList<GalasaProperty>();
        for (Map.Entry<GalasaPropertyName, CPSProperty> entry : properties.entrySet()) {
            CPSProperty property = entry.getValue();

            GalasaPropertyMetadata metadata = new GalasaPropertyMetadata( property.getNamespace(), property.getName());
            GalasaPropertyData data = new GalasaPropertyData( property.getPossiblyRedactedValue());
            GalasaProperty galasaProperty = new GalasaProperty(metadata,data);

            results.add(galasaProperty);
        }
        return gson.toJson(results);
    }

    protected String buildPropertiesResponseBody(Map<GalasaPropertyName, CPSProperty> properties) {
        /*
         * Builds a json array object containing the legacy format of the Properties
         */
        JsonArray results = new JsonArray();
        for (Map.Entry<GalasaPropertyName, CPSProperty> entry : properties.entrySet()) {
            CPSProperty property = entry.getValue();

            JsonObject responseProperty = new JsonObject();
            responseProperty.addProperty("name", property.getName());
            responseProperty.addProperty("value", property.getValue());

            results.add(responseProperty);
        }
        return gson.toJson(results);
    }

    protected String buildResponseBody(CPSProperty property) {
        /*
         * Builds a json array object from a single GalasaProperty containing a property
         */
        List<GalasaProperty> results = new ArrayList<GalasaProperty>();
        if (property != null){

            GalasaPropertyMetadata metadata = new GalasaPropertyMetadata( property.getNamespace(), property.getName());
            GalasaPropertyData data = new GalasaPropertyData( property.getPossiblyRedactedValue());
            GalasaProperty galasaProperty = new GalasaProperty(metadata,data);

            results.add(galasaProperty);
        }
        return gson.toJson(results);
    }
}