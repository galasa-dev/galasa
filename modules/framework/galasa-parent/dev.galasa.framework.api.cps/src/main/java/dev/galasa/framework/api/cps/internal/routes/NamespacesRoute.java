/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import dev.galasa.framework.api.common.resources.GalasaNamespace;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.utils.GalasaGson;


/**
 * An abstract route used by all the Property-related routes.
 */
public class NamespacesRoute extends CPSRoute {

    protected static final String path = "\\/?";
    private static final GalasaGson gson = new GalasaGson();


    public NamespacesRoute(ResponseBuilder responseBuilder, IFramework framework) throws RBACException {
		/* Regex to match endpoints: 
		*  -> /cps
		*  -> /cps/
		*/
		super(responseBuilder, path, framework);
	}

    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams, HttpRequestContext requestContext, HttpServletResponse response) throws ServletException, IOException, FrameworkException {
        HttpServletRequest request = requestContext.getRequest();
        String namespaces = getNamespaces(request.getRequestURI());
		return getResponseBuilder().buildResponse(request, response, "application/json", namespaces, HttpServletResponse.SC_OK); 
    }

    private String getNamespaces(String url) throws InternalServletException {
        logger.debug("Getting the list of namespaces");
        List<GalasaNamespace> namespaceArray = new ArrayList<GalasaNamespace>();
        try {
            CPSFacade cps = new CPSFacade(framework);
            Map<String, CPSNamespace>  namespaces = cps.getNamespaces();
            for (Map.Entry<String, CPSNamespace>  namespaceEntry : namespaces.entrySet()){
                CPSNamespace namespace = namespaceEntry.getValue();
                if (!namespace.isHidden()){
                    namespaceArray.add(new GalasaNamespace(namespace));
                }
            }
        } catch (ConfigurationPropertyStoreException e) {
            ServletError error = new ServletError(GAL5015_INTERNAL_CPS_ERROR);
			throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
        return gson.toJson(namespaceArray);
    }

}