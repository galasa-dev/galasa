/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.runs.common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkRuns.SharedEnvironmentPhase;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.api.run.Run;
import dev.galasa.api.runs.ScheduleRequest;
import dev.galasa.api.runs.ScheduleStatus;
import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ProtectedRoute;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.teststructure.TestStructure;
import dev.galasa.framework.spi.utils.GalasaGson;
import dev.galasa.framework.spi.utils.ITimeService;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

public class GroupRuns extends ProtectedRoute {

    protected IFramework framework;
    private IResultArchiveStore rasStore;
    private final GalasaGson gson = new GalasaGson();
    private final ITimeService timeService;

    public GroupRuns(ResponseBuilder responseBuilder, String path, IFramework framework, ITimeService timeService) throws RBACException {
        super(responseBuilder, path, framework.getRBACService());
        this.framework = framework;
        this.rasStore = framework.getResultArchiveStore();
        this.timeService = timeService;
    }

    protected List<IRun> getRuns(String groupName) throws InternalServletException {
        List<IRun> runs = null;
        try {
            runs = this.framework.getFrameworkRuns().getAllGroupedRuns(groupName);
        } catch (FrameworkException fe) {
            ServletError error = new ServletError(GAL5019_UNABLE_TO_RETRIEVE_RUNS, groupName);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, fe);
        }
        return runs;
    }

    private Run setwebUiAndRestApiUrls(IRun run, Environment env) {
        String runId = run.getRasRunId();

        String baseWebUiUrl = env.getenv(EnvironmentVariables.GALASA_EXTERNAL_WEBUI_URL);
        String webUiUrl = baseWebUiUrl + "/test-runs/" + runId;

        String baseServletUrl = env.getenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL);
        String restApiUrl = baseServletUrl + "/ras/runs/" + runId;

        Run serializedRun = run.getSerializedRun();
        serializedRun.setWebUiUrl(webUiUrl);
        serializedRun.setRestApiUrl(restApiUrl);

        return serializedRun;
    }

    protected ScheduleStatus serializeRuns(@NotNull List<IRun> runs, Environment env) {

        ScheduleStatus status = new ScheduleStatus();
        boolean complete = true;
        for (IRun run : runs) {
            ScheduleRunCompleteStatus runstatus = ScheduleRunCompleteStatus.getFromString(run.getStatus());
            if (runstatus == null) {
                complete = false;
            }

            Run serializedRun = setwebUiAndRestApiUrls(run, env);

            status.getRuns().add(serializedRun);
        }

        status.setComplete(complete);
        return status;
    }

    protected ScheduleRequest getScheduleRequestfromRequest(HttpServletRequest request)
            throws InternalServletException {
        ScheduleRequest scheduleRequest;
        try {
            String payload = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            scheduleRequest = gson.fromJson(payload, ScheduleRequest.class);
        } catch (Exception e) {
            ServletError error = new ServletError(GAL5020_UNABLE_TO_CONVERT_TO_SCHEDULE_REQUEST);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST, e);
        }
        return scheduleRequest;
    }

    public ScheduleStatus scheduleRun(ScheduleRequest request, String groupName, String jwtRequestor, Environment env)
            throws ServletException, IOException, InternalServletException {

        ScheduleStatus status = new ScheduleStatus();
        status.setComplete(false);

        for (String className : request.getClassNames()) {
            // className is in format bundle/testClass
            String[] classNameSplit = className.split("/");

            SharedEnvironmentPhase senvPhase = null;
            String sharedEnvironmentPhase = request.getSharedEnvironmentPhase();
            if (sharedEnvironmentPhase != null) {
                try {
                    senvPhase = SharedEnvironmentPhase.valueOf(request.getSharedEnvironmentPhase());
                } catch (Throwable t) {
                    ServletError error = new ServletError(GAL5022_UNABLE_TO_PARSE_SHARED_ENVIRONMENT_PHASE,
                            sharedEnvironmentPhase);
                    throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t);
                }
            }

            if (jwtRequestor == null) {
                jwtRequestor = request.getRequestor();
            }

            String user = request.getUser();
            if (user == null || user.isEmpty()) {
                user = jwtRequestor;
            } 

            try {

                String submissionId = UUID.randomUUID().toString();
                List<String> requestedTestMethods = new ArrayList<>();

                IRun newRun = framework.getFrameworkRuns().submitRun(
                        request.getRequestorType(),
                        jwtRequestor,
                        user,
                        classNameSplit[0],
                        classNameSplit[1],
                        groupName,
                        request.getMavenRepository(),
                        request.getObr(),
                        request.getTestStream(),
                        false,
                        request.isTrace(),
                        request.getTags(),
                        request.getOverrides(),
                        senvPhase,
                        request.getSharedEnvironmentRunName(),
                        "java",
                        submissionId,
                        requestedTestMethods
                    );
                        
                Run serializedRun = setwebUiAndRestApiUrls(newRun, env);

                status.getRuns().add(serializedRun);

                // Create an empty RAS record for the submitted run
                createRunRasRecord(newRun, rasStore, timeService, env);

            } catch (FrameworkException fe) {
                ServletError error = new ServletError(GAL5021_UNABLE_TO_SUBMIT_RUNS, className);
                throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, fe);
            }
        }
        return status;
    }

    protected void createRunRasRecord(IRun newRun, IResultArchiveStore rasStore, ITimeService timeService,
            Environment env) throws InternalServletException {
        TestStructure newTestStructure = newRun.toTestStructure();
        newTestStructure.setStatus(TestRunLifecycleStatus.QUEUED.toString());
        newTestStructure.setQueued(timeService.now());
        String runId = newRun.getRasRunId();

        try {
            rasStore.createTestStructure(runId, newTestStructure);
        } catch (ResultArchiveStoreException e) {
            ServletError error = new ServletError(GAL5021_UNABLE_TO_SUBMIT_RUNS, runId);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
    }
}
