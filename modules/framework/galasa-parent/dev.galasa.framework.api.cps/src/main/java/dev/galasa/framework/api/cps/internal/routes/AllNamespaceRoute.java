/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.routes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;

import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.resources.CPSFacade;
import dev.galasa.framework.api.common.resources.CPSNamespace;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.utils.GalasaGson;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.util.Map;

public class AllNamespaceRoute extends CPSRoute {

    protected static final String path = "\\/namespace\\/?";
    private static final GalasaGson gson = new GalasaGson();
    
    public AllNamespaceRoute(ResponseBuilder responseBuilder, IFramework framework) throws RBACException {
        /* Regex to match endpoints: 
		*  -> /cps/namespace
		*  -> /cps/namespace/
		*/
		super(responseBuilder, path, framework);
	}

    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams, HttpRequestContext requestContext, HttpServletResponse response) throws ServletException, FrameworkException {
        HttpServletRequest request = requestContext.getRequest();
        String namespaces = getNamespaces();
		return getResponseBuilder().buildResponse(request, response, "application/json", namespaces, HttpServletResponse.SC_OK); 
    }
    
    private String getNamespaces() throws InternalServletException {
        logger.debug("Getting the list of namespaces");
        JsonArray namespaceArray = new JsonArray();
        try {
            CPSFacade cps = new CPSFacade(framework);
            Map<String, CPSNamespace>  namespaces = cps.getNamespaces();
            for (Map.Entry<String, CPSNamespace>  namespaceEntry : namespaces.entrySet()){
                CPSNamespace namespace = namespaceEntry.getValue();
                if (!namespace.isHidden()){
                    namespaceArray.add(namespace.getName());
                }
            }
        } catch (ConfigurationPropertyStoreException e) {
            ServletError error = new ServletError(GAL5015_INTERNAL_CPS_ERROR);
			throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
        return gson.toJson(namespaceArray);
    }

}
