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

import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.resources.CPSFacade;
import dev.galasa.framework.api.common.resources.CPSNamespace;
import dev.galasa.framework.api.common.resources.CPSProperty;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.rbac.BuiltInAction;
import dev.galasa.framework.spi.rbac.RBACException;

/**
 * A route used by all the Property-related Requests.
 */
public class PropertyUpdateRoute extends CPSRoute {

    protected static final String path = "\\/([a-z][a-z0-9]+)/properties/([a-zA-Z][a-zA-Z0-9\\.\\-\\_@]+)" ;

    public PropertyUpdateRoute(ResponseBuilder responseBuilder, IFramework framework) throws RBACException {
		/* Regex to match endpoints: 
		*  -> /cps/<namespace>/properties/<propertyName>
		*/
		super(responseBuilder, path, framework);
	}

    /*
     * Handle Get Request
     */
    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams, HttpRequestContext requestContext, HttpServletResponse response) throws ServletException, IOException, FrameworkException {
        HttpServletRequest request = requestContext.getRequest();

        String namespace = getNamespaceFromURL(pathInfo);
        String propertyName = getPropertyNameFromURL(pathInfo);
        checkNamespaceExists(namespace);
        String property = retrieveProperty(namespace,propertyName);
		return getResponseBuilder().buildResponse(request, response, "application/json", property, HttpServletResponse.SC_OK); 
    }

    private String retrieveProperty (String namespaceName, String propertyName) throws FrameworkException {
        if (!propertyName.contains(".")){
            ServletError error = new ServletError(GAL5024_INVALID_GALASAPROPERTY,
            "Invalid property name. Property name much have at least two parts seperated by a '.' (dot)");
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
        if (propertyName.endsWith(".")){
            ServletError error = new ServletError(GAL5024_INVALID_GALASAPROPERTY,
            "Invalid property name. Property name '"+propertyName+"' can not end with a '.' (dot) seperator.");
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
        try {
            nameValidator.assertPropertyNameCharPatternIsValid(propertyName);
        } catch (FrameworkException f){
            ServletError error = new ServletError(GAL5024_INVALID_GALASAPROPERTY, f.getMessage());
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST, f);
        }
        CPSFacade cps = new CPSFacade(framework);
        CPSNamespace namespace = cps.getNamespace(namespaceName);
        CPSProperty property = namespace.getProperty(propertyName);
        return buildResponseBody(property);
    }

    /*
     * Handle Put Request
     */
    @Override
    public HttpServletResponse handlePutRequest(String pathInfo, HttpRequestContext requestContext, HttpServletResponse response)
            throws IOException, FrameworkException {
        
        HttpServletRequest request = requestContext.getRequest();
        validateActionPermitted(BuiltInAction.CPS_PROPERTIES_SET, requestContext.getUsername());

        String namespaceName = getNamespaceFromURL(pathInfo);
        String name = getPropertyNameFromURL(pathInfo);
        checkRequestHasContent(request);
        ServletInputStream body = request.getInputStream();
        String jsonString = new String (body.readAllBytes(),StandardCharsets.UTF_8);
        body.close();
        checkNameMatchesRequest(name, jsonString);
        nameValidator.assertNamespaceCharPatternIsValid(namespaceName);
        nameValidator.assertPropertyNameCharPatternIsValid(name);
        checkNamespaceExists(namespaceName);
        CPSProperty property = applyPropertyToStore(jsonString, namespaceName, true);
        String responseBody = String.format("Successfully updated property %s in %s",property.getName(), property.getNamespace());
        return getResponseBuilder().buildResponse(request, response, "text/plain", responseBody, HttpServletResponse.SC_OK); 
    }


    /*
     * Handle Delete Request
     */
    @Override
    public HttpServletResponse handleDeleteRequest(String pathInfo,
            HttpRequestContext requestContext, HttpServletResponse response)
            throws FrameworkException {
        HttpServletRequest request = requestContext.getRequest();

        String namespace = getNamespaceFromURL(pathInfo);
        String property = getPropertyNameFromURL(pathInfo);
        nameValidator.assertNamespaceCharPatternIsValid(namespace);
        nameValidator.assertPropertyNameCharPatternIsValid(property);
        deleteProperty(namespace, property);
        String responseBody = String.format("Successfully deleted property %s in %s",property, namespace);
        return getResponseBuilder().buildResponse(request, response, "text/plain", responseBody, HttpServletResponse.SC_OK);
    }


    private void deleteProperty(String namespace, String propertyName) throws FrameworkException {
        if (checkPropertyExists(namespace, propertyName)){
            framework.getConfigurationPropertyService(namespace).deleteProperty(propertyName);
        }else{
            ServletError error = new ServletError(GAL5017_PROPERTY_DOES_NOT_EXIST_ERROR,propertyName);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
    }


}