/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.runs.routes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.api.runs.ScheduleRequest;
import dev.galasa.api.runs.ScheduleStatus;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.RunStatusUpdate;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.runs.common.GroupRunActionJson;
import dev.galasa.framework.api.runs.common.GroupRuns;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.rbac.BuiltInAction;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.utils.GalasaGson;
import dev.galasa.framework.spi.utils.SystemTimeService;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;
public class GroupRunsRoute extends GroupRuns{

    protected static final String path = "\\/[a-zA-Z0-9_\\-]*";
    private final GalasaGson gson = new GalasaGson();
    private final String GROUP_RUNS_CANCELLED_STATUS = "cancelled";
    private final Environment env;

    public GroupRunsRoute(ResponseBuilder responseBuilder, IFramework framework, Environment env) throws RBACException {
        // Regex to match endpoints:
		// -> /runs/{GroupID}
		//
        super(responseBuilder, path, framework, new SystemTimeService() );
        this.env = env;
    }

    @Override
    public HttpServletResponse handleGetRequest(
        String groupName,
        QueryParameters queryParams,
        HttpRequestContext requestContext,
        HttpServletResponse response
    ) throws ServletException, IOException, FrameworkException{

        HttpServletRequest request = requestContext.getRequest();

        List<IRun> runs = getRuns(groupName.substring(1));
        if (runs != null){
            ScheduleStatus serializedRuns = serializeRuns(runs, env);
            return getResponseBuilder().buildResponse(request, response, "application/json", gson.toJson(serializedRuns), HttpServletResponse.SC_OK);
        }else{
            ServletError error = new ServletError(GAL5019_UNABLE_TO_RETRIEVE_RUNS, groupName);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public HttpServletResponse handlePostRequest(
        String groupName,
        HttpRequestContext requestContext,
        HttpServletResponse response
    ) throws ServletException, IOException, FrameworkException {

        HttpServletRequest request = requestContext.getRequest();
        String requestor = requestContext.getUsername();

        validateActionPermitted(BuiltInAction.TEST_RUN_LAUNCH, requestor);

        checkRequestHasContent(request);
        ScheduleRequest scheduleRequest = getScheduleRequestfromRequest(request);

        String user = scheduleRequest.getUser();
        if (user != null && !user.isBlank()) {
            boolean isRequestorPermittedToSetUser = isActionPermitted(BuiltInAction.TEST_RUN_SET_USER, requestor);
            if (isRequestorPermittedToSetUser) {
                validateActionPermitted(BuiltInAction.TEST_RUN_LAUNCH, user);
            } else {
                // If the requestor login ID does not have the TEST_RUN_SET_USER action,
                // don't block them from launching the test, but don't let them override the user.
                scheduleRequest.setUser(requestor);
            }
        }

        ScheduleStatus scheduleStatus = scheduleRun(scheduleRequest, groupName.substring(1), requestor, env);
        return getResponseBuilder().buildResponse(request, response, "application/json", gson.toJson(scheduleStatus), HttpServletResponse.SC_CREATED);
    }

    @Override
    public HttpServletResponse handlePutRequest(String groupName, HttpRequestContext requestContext,
            HttpServletResponse res) throws ServletException, IOException, FrameworkException {

        HttpServletRequest request = requestContext.getRequest();
        String strippedGroupName = groupName.substring(1); //remove leading slash

        GroupRunActionJson runAction = getRunActionFromRequestBody(request);
        String responseBody = updateRunStatus(strippedGroupName, runAction);

        int responseStatusCode = HttpServletResponse.SC_ACCEPTED;
        
        if (responseBody.isBlank()) {
            responseStatusCode = HttpServletResponse.SC_OK;
            responseBody = String.format("Info: When trying to cancel the run group '%s', no recent active (unfinished) test runs were found which are part of that group. Archived test runs may be part of that group, which can be queried separately from the Result Archive Store.", strippedGroupName);
        }

        return getResponseBuilder().buildResponse(request, res, "text/plain", responseBody, responseStatusCode);
    }

    private String updateRunStatus(String groupId, GroupRunActionJson runAction)
            throws InternalServletException, ResultArchiveStoreException {

        String responseBody = "";
        List<IRun> groupedRuns = getRuns(groupId);
        String result = runAction.getResult();
        RunStatusUpdate runStatusUpdate = new RunStatusUpdate(framework);

        if (!result.equals(GROUP_RUNS_CANCELLED_STATUS)) {
            ServletError error = new ServletError(GAL5431_INVALID_CANCEL_UPDATE_REQUEST, runAction.getResult());
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
                
        if (groupedRuns != null && !groupedRuns.isEmpty()) {

            for (IRun run : groupedRuns) {

                String runName = run.getName();
                runStatusUpdate.cancelRun(runName, result);
                logger.info("Run cancelled by external source.");

            }

            responseBody = String.format("The request to cancel run with group id '%s' has been received.", groupId);

        }

        return responseBody;
    }

    private GroupRunActionJson getRunActionFromRequestBody(HttpServletRequest request) throws IOException{

        ServletInputStream body = request.getInputStream();
        String jsonString = new String(body.readAllBytes(), StandardCharsets.UTF_8);
        body.close();
        GroupRunActionJson runAction = gson.fromJson(jsonString, GroupRunActionJson.class);

        return runAction;

    }

}