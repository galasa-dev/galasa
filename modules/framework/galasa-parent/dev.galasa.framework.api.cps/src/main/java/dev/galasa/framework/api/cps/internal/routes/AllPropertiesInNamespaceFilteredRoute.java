/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

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
import dev.galasa.framework.spi.rbac.RBACException;

public class AllPropertiesInNamespaceFilteredRoute extends CPSRoute {

    protected static final String path = "\\/namespace\\/([a-z][a-z0-9]+)\\/prefix\\/([a-zA-Z0-9\\.\\-\\_@]+)\\/suffix\\/([a-zA-Z0-9\\.\\-\\_@]+)\\/?";

    private String suffix;
    private String prefix;
    private String namespaceName;
    
    public AllPropertiesInNamespaceFilteredRoute(ResponseBuilder responseBuilder, IFramework framework) throws RBACException {
        /* Regex to match endpoints: 
		*  -> /cps/namespace/namespaceName/prefix/propertyStartsWith/suffix/propertyEndsWith
		*  -> /cps/namespace/namespaceName/prefix/propertyStartsWith/suffix/propertyEndsWith/
		*/
        super(responseBuilder, path, framework);
    }

    @Override 
    public SupportedQueryParameterNames getSupportedQueryParameterNames() {
        return SUPPORTED_QUERY_PARAMETER_NAMES ;
    }
    
    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams, HttpRequestContext requestContext, HttpServletResponse response)
            throws ServletException, FrameworkException {
        HttpServletRequest request = requestContext.getRequest();
        getPropertyDetailsFromURL(pathInfo);
        String properties = getNamespaceProperties(queryParams);
        checkNamespaceExists(namespaceName);
        return getResponseBuilder().buildResponse(request, response, "application/json", properties, HttpServletResponse.SC_OK); 
    }

    private void getPropertyDetailsFromURL(String pathInfo) throws InternalServletException {
        // Set the values for the suffix prefix and namespaceName in order to use the values
        try {
            String[] path = pathInfo.split("/");
            namespaceName = path[2];
            prefix = path[4];
            suffix = path[6];
        } catch (Exception e) {
            ServletError error = new ServletError(GAL5000_GENERIC_API_ERROR);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND, e);
        }
    }

    private String getNamespaceProperties( QueryParameters queryParams) throws InternalServletException{
        JsonObject responseProperty = new JsonObject();
         try {
            nameValidator.assertNamespaceCharPatternIsValid(namespaceName);
            CPSFacade cps = new CPSFacade(framework);
            CPSNamespace namespace = cps.getNamespace(namespaceName);
            if (namespace.isHidden()) {
                ServletError error = new ServletError(GAL5016_INVALID_NAMESPACE_ERROR, namespaceName);
                throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
            }
            List<String> infixes = queryParams.getMultipleString(QUERY_PARAMETER_INFIX, null);
            Map<GalasaPropertyName, CPSProperty> propertiesMap = getProperties(namespace, prefix, suffix, infixes);
            //Get First Property From propertiesMap and send it as a response
            if ( propertiesMap.size() > 0 ){
                CPSProperty property = propertiesMap.entrySet().iterator().next().getValue();
                responseProperty.addProperty("name", property.getName());
                responseProperty.addProperty("value", property.getValue());
            }

        }catch (FrameworkException f){
            ServletError error = new ServletError(GAL5016_INVALID_NAMESPACE_ERROR,namespaceName);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND, f);
        }
        return gson.toJson(responseProperty);
    }
}
