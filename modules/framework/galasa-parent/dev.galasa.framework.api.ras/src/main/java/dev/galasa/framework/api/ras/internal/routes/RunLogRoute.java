/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.io.OutputStream;
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

        try {
            streamRunlog(runId, request, res);
            return res;
        } catch (ResultArchiveStoreException e) {
            ServletError error = new ServletError(GAL5002_INVALID_RUN_ID, runId);
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Stream the run log directly to the response output stream.
     * This avoids loading the entire log into memory, which is critical for large logs.
     *
     * @param runId The ID of the run to get the log for
     * @param request The HTTP request
     * @param res The HTTP response to stream to
     * @throws ResultArchiveStoreException if there's an error accessing the log
     * @throws InternalServletException if the run is not found
     * @throws IOException if there's an error writing to the response
     */
    private void streamRunlog(String runId, HttpServletRequest request, HttpServletResponse res)
    		throws ResultArchiveStoreException, InternalServletException, IOException {

    	IRunResult run = getRunByRunId(runId);

    	long logSize = run.getLogSize();
    	if (logSize == -1) {
            // Fall back to loading the run log for legacy runs without the logSize metadata
            String runLog = run.getLog();
            if (runLog == null) {
                ServletError error = new ServletError(GAL5002_INVALID_RUN_ID, runId);
                throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
            }
    	}

    	res = getResponseBuilder().buildResponse(request, res, "text/plain", HttpServletResponse.SC_OK);

    	// Stream the log content directly to the response
    	try (OutputStream outStream = res.getOutputStream()) {
    		run.streamLog(outStream);
    	}
    }
}