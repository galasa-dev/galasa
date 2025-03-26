/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.monitors.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.api.beans.generated.GalasaMonitor;
import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.MimeType;
import dev.galasa.framework.api.common.ProtectedRoute;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.ServletErrorMessage;
import dev.galasa.framework.api.monitors.internal.IKubernetesApiClient;
import dev.galasa.framework.api.monitors.internal.MonitorTransform;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.rbac.RBACService;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Deployment;

public class MonitorsDetailsRoute extends ProtectedRoute {

    // Regex to match /monitors/{monitorName} and /monitors/{monitorName}/
    // where {monitorName} can consist of the following characters:
    // - Alphanumeric characters (a-zA-Z0-9)
    // - Underscores (_)
    // - Dashes (-)
    private static final String PATH_PATTERN = "\\/([a-zA-Z0-9_-]+)\\/?";

    private Log logger = LogFactory.getLog(getClass());

    private IKubernetesApiClient kubeApiClient;
    private final String kubeNamespace;

    private MonitorTransform monitorTransform = new MonitorTransform();

    public MonitorsDetailsRoute(
        ResponseBuilder responseBuilder,
        RBACService rbacService,
        IKubernetesApiClient kubeApiClient,
        String kubeNamespace
    ) {
        super(responseBuilder, PATH_PATTERN, rbacService);
        this.kubeApiClient = kubeApiClient;
        this.kubeNamespace = kubeNamespace;
    }

    @Override
    public HttpServletResponse handleGetRequest(
        String pathInfo,
        QueryParameters queryParams,
        HttpRequestContext requestContext,
        HttpServletResponse response
    ) throws FrameworkException {

        logger.info("handleGetRequest() entered");

        HttpServletRequest request = requestContext.getRequest();

        String monitorName = getMonitorNameFromPath(pathInfo);
        V1Deployment matchingDeployment = getDeploymentByName(monitorName);

        GalasaMonitor monitorBean = null;
        if (matchingDeployment == null) {
            ServletError error = new ServletError(GAL5422_ERROR_MONITOR_NOT_FOUND_BY_NAME);
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        } else {
            monitorBean = monitorTransform.createGalasaMonitorBeanFromDeployment(matchingDeployment);
        }

        String monitorJson = gson.toJson(monitorBean);
        logger.info("handleGetRequest() exiting");

        return getResponseBuilder().buildResponse(request, response, MimeType.APPLICATION_JSON.toString(), monitorJson, HttpServletResponse.SC_OK);
    }

    private V1Deployment getDeploymentByName(String monitorName) throws InternalServletException {
        V1Deployment matchingDeployment = null;
        try {
            matchingDeployment = kubeApiClient.getDeploymentByName(monitorName, kubeNamespace);
        } catch (ApiException e) {
            ServletError error = new ServletError(GAL5421_ERROR_GETTING_MONITOR_DEPLOYMENTS);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
        return matchingDeployment;
    }

    private String getMonitorNameFromPath(String pathInfo) throws InternalServletException {
        Matcher matcher = this.getPathRegex().matcher(pathInfo);
        matcher.matches();
        String monitorName;
        try {
            monitorName =  matcher.group(1);
        } catch (Exception ex) {
            ServletError error = new ServletError(ServletErrorMessage.GAL5423_INVALID_MONITOR_NAME_PROVIDED);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
        return monitorName;
    }
}
