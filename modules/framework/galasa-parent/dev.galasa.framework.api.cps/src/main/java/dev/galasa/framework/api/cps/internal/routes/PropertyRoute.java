/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.routes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.SupportedQueryParameterNames;
import dev.galasa.framework.api.common.resources.CPSFacade;
import dev.galasa.framework.api.common.resources.CPSNamespace;
import dev.galasa.framework.api.common.resources.CPSProperty;
import dev.galasa.framework.api.common.resources.GalasaPropertyName;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.rbac.BuiltInAction;
import dev.galasa.framework.spi.rbac.RBACException;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

public class PropertyRoute extends CPSRoute{

    protected static final String path = "\\/([a-z][a-z0-9]+)/properties([?]?|[^/])+$";

    public PropertyRoute(ResponseBuilder responseBuilder, IFramework framework) throws RBACException {
        super(responseBuilder, path , framework);
    }


    @Override 
    public SupportedQueryParameterNames getSupportedQueryParameterNames() {
        return SUPPORTED_QUERY_PARAMETER_NAMES ;
    }

    /*
     * Property Query
     */
    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams, HttpRequestContext requestContext, HttpServletResponse response)
            throws ServletException, IOException, FrameworkException {
        HttpServletRequest request = requestContext.getRequest();
        String namespace = getNamespaceFromURL(pathInfo);
        String properties = getNamespaceProperties(namespace, queryParams);
        checkNamespaceExists(namespace);
        return getResponseBuilder().buildResponse(request, response, "application/json", properties, HttpServletResponse.SC_OK); 
    }

    private String getNamespaceProperties(String namespaceName, QueryParameters queryParams) throws InternalServletException{
        String properties = "";
         try {
            nameValidator.assertNamespaceCharPatternIsValid(namespaceName);
            CPSFacade cps = new CPSFacade(framework);
            CPSNamespace namespace = cps.getNamespace(namespaceName);
            if (namespace.isHidden()) {
                ServletError error = new ServletError(GAL5016_INVALID_NAMESPACE_ERROR, namespaceName);
                throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
            }
            String prefix = queryParams.getSingleString(QUERY_PARAMETER_PREFIX, null);
            String suffix = queryParams.getSingleString(QUERY_PARAMETER_SUFFIX, null);
            List<String> infixes = queryParams.getMultipleString(QUERY_PARAMETER_INFIX, null);
            Map<GalasaPropertyName, CPSProperty> propertiesMap = getProperties(namespace, prefix, suffix, infixes);
            properties = buildResponseBody(propertiesMap);
        }catch (FrameworkException f){
            ServletError error = new ServletError(GAL5016_INVALID_NAMESPACE_ERROR,namespaceName);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND, f);
        }
        return properties;
    }
    
    /*
     * Property Create
     */
    @Override
    public HttpServletResponse handlePostRequest(String pathInfo,
            HttpRequestContext requestContext, HttpServletResponse response)
            throws  IOException, FrameworkException {

        HttpServletRequest request = requestContext.getRequest();
        validateActionPermitted(BuiltInAction.CPS_PROPERTIES_SET, requestContext.getUsername());

        String namespaceName = getNamespaceFromURL(pathInfo);
        checkRequestHasContent(request);
        ServletInputStream body = request.getInputStream();
        String jsonString = new String (body.readAllBytes(),StandardCharsets.UTF_8);
        body.close();
        CPSProperty property = applyPropertyToStore(jsonString, namespaceName, false);
        String responseBody = String.format("Successfully created property %s in %s",property.getName(), property.getNamespace());
        return getResponseBuilder().buildResponse(request, response, "text/plain", responseBody, HttpServletResponse.SC_CREATED); 
    }

}
