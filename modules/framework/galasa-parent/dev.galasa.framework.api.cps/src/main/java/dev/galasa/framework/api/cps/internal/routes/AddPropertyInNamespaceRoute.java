/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;

import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

import dev.galasa.framework.api.beans.GalasaProperty;
import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.resources.CPSFacade;
import dev.galasa.framework.api.common.resources.CPSNamespace;
import dev.galasa.framework.api.common.resources.CPSProperty;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.rbac.BuiltInAction;
import dev.galasa.framework.spi.rbac.RBACException;

public class AddPropertyInNamespaceRoute extends CPSRoute {

    protected static final String path = "\\/namespace\\/([a-z][a-z0-9]+)\\/property\\/([a-zA-Z0-9\\.\\-\\_@]+)/?";
    
    private String propertyName;
    private String namespaceName;
    
    public AddPropertyInNamespaceRoute(ResponseBuilder responseBuilder, IFramework framework) throws RBACException {
        /* Regex to match endpoints: 
		*  -> /cps/namespace/namespaceName//property/propertyName
		*  -> /cps/namespace/namespaceName//property/propertyName/
		*/
        super(responseBuilder, path, framework);
    }
    
    @Override
    public HttpServletResponse handlePutRequest(String pathInfo, HttpRequestContext requestContext, HttpServletResponse response)
            throws ServletException, FrameworkException, IOException {

        HttpServletRequest request = requestContext.getRequest();
        validateActionPermitted(BuiltInAction.CPS_PROPERTIES_SET, requestContext.getUsername());

        getPropertyDetailsFromURL(pathInfo);
        checkNamespaceExists(namespaceName);
        checkRequestHasContent(request);
        ServletInputStream body = request.getInputStream();
        String jsonString = new String (body.readAllBytes(),StandardCharsets.UTF_8);
        body.close();
        JsonObject reqJson = gson.fromJson(jsonString,JsonObject.class);
        String properties = setNamespaceProperty(reqJson);
        return getResponseBuilder().buildResponse(request, response, "application/json", properties, HttpServletResponse.SC_OK); 
    }

    private void getPropertyDetailsFromURL(String pathInfo) throws InternalServletException {
        // Set the values for the suffix prefix and namespaceName in order to use the values
        try {
            String[] path = pathInfo.split("/");
            namespaceName = path[2];
            propertyName = path[4];
        } catch (Exception e) {
            ServletError error = new ServletError(GAL5000_GENERIC_API_ERROR);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND, e);
        }
    }

    private String setNamespaceProperty(JsonObject reqJson) throws InternalServletException, IOException{
        String properties = "";
         try {
            nameValidator.assertNamespaceCharPatternIsValid(namespaceName);
            CPSFacade cps = new CPSFacade(framework);
            CPSNamespace namespace = cps.getNamespace(namespaceName);
            if (namespace.isHidden()) {
                ServletError error = new ServletError(GAL5016_INVALID_NAMESPACE_ERROR, namespaceName);
                throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
            }
            String jsonName = reqJson.get("name").getAsString();
            String jsonValue = reqJson.get("value").getAsString();
            if (!propertyName.equals(jsonName)){
                ServletError error = new ServletError(GAL5029_PROPERTY_NAME_DOES_NOT_MATCH_ERROR, jsonName, propertyName);
                throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
            }
            nameValidator.assertPropertyNameCharPatternIsValid(propertyName);
            CPSProperty property = namespace.getPropertyFromStore(propertyName);
            if (property.getValue() == null){
                // Property does not exist in store, create a new property
                property = applyPropertyToStore(jsonName, jsonValue, namespaceName, false);
            } else {
                // Property does exist in store, update the property
                property = applyPropertyToStore(jsonName, jsonValue, namespaceName, true);
            }
            
            JsonObject respJson = new JsonObject();
            respJson.addProperty("name", property.getName());
            respJson.addProperty("value", property.getValue());
            properties = gson.toJson(respJson);
        }catch (ConfigurationPropertyStoreException f){
                ServletError error = new ServletError(GAL5016_INVALID_NAMESPACE_ERROR,namespaceName);  
                throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND, f);
        }
        return properties;
    }

    private CPSProperty applyPropertyToStore (String propertyName, String propertyValue, String namespaceName , boolean isUpdateAction) throws InternalServletException, ConfigurationPropertyStoreException{
        GalasaProperty galasaProperty = new GalasaProperty(namespaceName, propertyName, propertyValue);
        CPSFacade cps = new CPSFacade(framework);
        CPSNamespace namespace = cps.getNamespace(galasaProperty.getNamespace());
        if (namespace.isHidden()) {
            ServletError error = new ServletError(GAL5016_INVALID_NAMESPACE_ERROR,namespaceName);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
        CPSProperty property = namespace.getPropertyFromStore(galasaProperty.getName());
        if(!checkPropertyNamespaceMatchesURLNamespace(property, namespaceName)){
            ServletError error = new ServletError(GAL5028_PROPERTY_NAMESPACE_DOES_NOT_MATCH_ERROR,property.getNamespace(), namespaceName);  
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
        property.setPropertyToStore(galasaProperty, isUpdateAction);
        return namespace.getPropertyFromStore(galasaProperty.getName());
    }
}
