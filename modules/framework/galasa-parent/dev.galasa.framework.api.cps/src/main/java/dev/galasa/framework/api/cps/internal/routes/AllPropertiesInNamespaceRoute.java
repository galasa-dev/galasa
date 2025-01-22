/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.routes;

import javax.servlet.ServletException;
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
import dev.galasa.framework.api.common.resources.GalasaPropertyName;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.rbac.RBACException;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.util.Map;

public class AllPropertiesInNamespaceRoute extends CPSRoute {

    protected static final String path = "\\/namespace\\/([a-z][a-z0-9]+)\\/?";
    
    public AllPropertiesInNamespaceRoute(ResponseBuilder responseBuilder, IFramework framework) throws RBACException {
        /* Regex to match endpoints: 
		*  -> /cps/namespace/<NamespaceName>
		*  -> /cps/namespace/<NamespaceName>/
		*/
        super(responseBuilder, path, framework);
    }

    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams, HttpRequestContext requestContext, HttpServletResponse response) throws ServletException, FrameworkException {
        HttpServletRequest request = requestContext.getRequest();
        String namespace = getNamespaceNameFromURL(pathInfo);
        String properties = getNamespaceProperties(namespace);
        checkNamespaceExists(namespace);
		return getResponseBuilder().buildResponse(request, response, "application/json", properties, HttpServletResponse.SC_OK); 
    }

    private String getNamespaceNameFromURL(String pathInfo) throws InternalServletException {
        try {
            String[] namespace = pathInfo.split("/");
            return namespace[2];
        } catch (Exception e) {
            ServletError error = new ServletError(GAL5000_GENERIC_API_ERROR);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND, e);
        }
    }

    private String getNamespaceProperties(String namespaceName) throws InternalServletException{
        String properties = "";
         try {
            nameValidator.assertNamespaceCharPatternIsValid(namespaceName);
            CPSFacade cps = new CPSFacade(framework);
            CPSNamespace namespace = cps.getNamespace(namespaceName);
            if (namespace.isHidden()) {
                ServletError error = new ServletError(GAL5016_INVALID_NAMESPACE_ERROR, namespaceName);
                throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
            }
            Map<GalasaPropertyName, CPSProperty> sortedProperties = sortPropertiesByPropertyName(namespace.getProperties());

            properties = buildPropertiesResponseBody(sortedProperties);
        }catch (FrameworkException f){
            ServletError error = new ServletError(GAL5016_INVALID_NAMESPACE_ERROR,namespaceName);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND, f);
        }
        return properties;
    }
    
}
