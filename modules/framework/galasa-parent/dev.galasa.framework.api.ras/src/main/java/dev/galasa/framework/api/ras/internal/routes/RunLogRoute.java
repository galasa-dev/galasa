/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.util.regex.Matcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.rbac.RBACException;

/**
 * Implementation to retrieve the run log for a given run based on its runId.
 */
public class RunLogRoute extends RunsRoute {

    protected static final String path = "\\/runs\\/([A-Za-z0-9.\\-=]+)\\/runlog\\/?";

    public RunLogRoute(ResponseBuilder responseBuilder, IFramework framework) throws RBACException {
        //  Regex to match endpoint: /ras/runs/{runid}/runlog
        super(responseBuilder, path, framework);
    }

    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams, HttpRequestContext requestContext, HttpServletResponse res) throws ServletException, IOException, FrameworkException {
        HttpServletRequest request = requestContext.getRequest();
        Matcher matcher = this.getPathRegex().matcher(pathInfo);
        matcher.matches();
        String runId = matcher.group(1);
        String runLog = getRunlog(runId);
        if (runLog != null) {
            return getResponseBuilder().buildResponse(request, res, "text/plain", runLog, HttpServletResponse.SC_OK);
        } else {
            ServletError error = new ServletError(GAL5002_INVALID_RUN_ID, runId);
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
    }


    public String getRunlog(String runId) throws ResultArchiveStoreException, InternalServletException {

        IRunResult run = getRunByRunId(runId);
        String runLog = null;

        if (run != null) {
           runLog = run.getLog();
        }

        return runLog;
     }
}