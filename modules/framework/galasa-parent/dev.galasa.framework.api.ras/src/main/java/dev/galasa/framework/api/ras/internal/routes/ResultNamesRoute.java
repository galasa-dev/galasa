/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.ras.internal.common.RasQueryParameters;
import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.SupportedQueryParameterNames;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.utils.GalasaGson;

public class ResultNamesRoute extends RunsRoute {

	protected static final String path = "\\/resultnames\\/?";

	private static final GalasaGson gson = new GalasaGson();

	public static final String QUERY_PARAMETER_SORT = "sort";
    public static final SupportedQueryParameterNames SUPPORTED_QUERY_PARAMETER_NAMES = new SupportedQueryParameterNames(
        QUERY_PARAMETER_SORT
    );

	public ResultNamesRoute(ResponseBuilder responseBuilder, IFramework framework) throws RBACException {
		/* Regex to match endpoints: 
		*  -> /ras/resultnames
		*  -> /ras/resultnames?
		*/
		super(responseBuilder, path, framework);
	}

    @Override
    public SupportedQueryParameterNames getSupportedQueryParameterNames() {
        return SUPPORTED_QUERY_PARAMETER_NAMES;
    }

    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams, HttpRequestContext requestContext, HttpServletResponse response) 
	throws ServletException, IOException, FrameworkException {
		HttpServletRequest request = requestContext.getRequest();

        String outputString = retrieveResults(new RasQueryParameters(queryParams));
		return getResponseBuilder().buildResponse(request, response, "application/json", outputString, HttpServletResponse.SC_OK);
    }

    public String retrieveResults (RasQueryParameters queryParams) throws ServletException, InternalServletException{
        List<String> resultsList = getResultNames();

		try {
            if (queryParams.getSortValue() !=null ){
			    if (!queryParams.isAscending("resultnames")) {
				    Collections.reverse(resultsList);
                }
			}
		} catch (InternalServletException e){
			ServletError error = new ServletError(GAL5011_SORT_VALUE_NOT_RECOGNIZED, "resultnames");
			throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST, e);
		}

		JsonElement json = gson.toJsonTree(resultsList);
		JsonObject resultnames = new JsonObject();
		resultnames.add("resultnames", json);
		return resultnames.toString();
    }

}
