/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.monitors.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.util.ArrayList;
import java.util.List;

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
import dev.galasa.framework.api.monitors.internal.IKubernetesApiClient;
import dev.galasa.framework.api.monitors.internal.MonitorTransform;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.rbac.RBACService;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Deployment;

public class MonitorsRoute extends ProtectedRoute {

    // Regex to match /monitors and /monitors/ only
    private static final String PATH_PATTERN = "\\/?";

    private static final String MONITOR_DEPLOYMENT_LABEL = "galasa-monitor";

    private Log logger = LogFactory.getLog(getClass());

    private IKubernetesApiClient kubeApiClient;
    private final String kubeNamespace;

    private MonitorTransform monitorTransform = new MonitorTransform();

    public MonitorsRoute(
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
        List<GalasaMonitor> monitors = new ArrayList<>();
        try {
            List<V1Deployment> deploymentsList = kubeApiClient.getNamespacedDeployments(kubeNamespace, MONITOR_DEPLOYMENT_LABEL);

            for (V1Deployment deployment : deploymentsList) {
                GalasaMonitor monitor = monitorTransform.createGalasaMonitorBeanFromDeployment(deployment);
                monitors.add(monitor);
            }

        } catch (ApiException e) {
            ServletError error = new ServletError(GAL5421_ERROR_GETTING_MONITOR_DEPLOYMENTS);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        logger.info("handleGetRequest() exiting");
        return getResponseBuilder().buildResponse(request, response, MimeType.APPLICATION_JSON.toString(),
            gson.toJson(monitors), HttpServletResponse.SC_OK);
    }
}
