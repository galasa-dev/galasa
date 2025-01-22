/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.runs.common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkRuns.SharedEnvironmentPhase;
import dev.galasa.api.runs.ScheduleRequest;
import dev.galasa.api.runs.ScheduleStatus;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ProtectedRoute;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.utils.GalasaGson;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

public class GroupRuns extends ProtectedRoute {

    protected IFramework framework;
    private final GalasaGson gson = new GalasaGson();
    

    public GroupRuns(ResponseBuilder responseBuilder, String path, IFramework framework) throws RBACException {
        super(responseBuilder, path, framework.getRBACService());
        this.framework = framework;
    }

    protected List<IRun> getRuns(String groupName) throws InternalServletException {
         List<IRun> runs = null;
        try {
            runs = this.framework.getFrameworkRuns().getAllGroupedRuns(groupName);
        } catch (FrameworkException fe) {
            ServletError error = new ServletError(GAL5019_UNABLE_TO_RETRIEVE_RUNS, groupName);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND, fe);
        }
        return runs;
    }

    protected ScheduleStatus serializeRuns(@NotNull List<IRun> runs) {
       
        ScheduleStatus status = new ScheduleStatus();
        boolean complete = true;
        for (IRun run : runs) {
            ScheduleRunCompleteStatus runstatus = ScheduleRunCompleteStatus.getFromString(run.getStatus());
            if (runstatus ==null) {
                complete = false;
            }

            status.getRuns().add(run.getSerializedRun());
        }

        status.setComplete(complete);
        return status;
    }

    protected ScheduleRequest getScheduleRequestfromRequest(HttpServletRequest request) throws InternalServletException{
        ScheduleRequest scheduleRequest;
        try{
            String payload = new String (request.getInputStream().readAllBytes(),StandardCharsets.UTF_8);
            scheduleRequest = gson.fromJson(payload,ScheduleRequest.class);
        } catch(Exception e) {
            ServletError error = new ServletError(GAL5020_UNABLE_TO_CONVERT_TO_SCHEDULE_REQUEST);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST, e);
        }
        return scheduleRequest;
    }

    public ScheduleStatus scheduleRun(ScheduleRequest request, String groupName, String jwtRequestor) throws ServletException, IOException, InternalServletException {
            
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
                    ServletError error = new ServletError(GAL5022_UNABLE_TO_PARSE_SHARED_ENVIRONMENT_PHASE,sharedEnvironmentPhase);
                    throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t);
                }
            }

            if(jwtRequestor == null){
                jwtRequestor = request.getRequestor(); 
            }
            try {
                IRun newRun = framework.getFrameworkRuns().submitRun(request.getRequestorType(), jwtRequestor, classNameSplit[0], classNameSplit[1],
                        groupName, request.getMavenRepository(), request.getObr(), request.getTestStream(), false,
                        request.isTrace(), request.getOverrides(), 
                        senvPhase, 
                        request.getSharedEnvironmentRunName(),
                        "java");
                
                status.getRuns().add(newRun.getSerializedRun());
            } catch (FrameworkException fe) {
                ServletError error = new ServletError(GAL5021_UNABLE_TO_SUBMIT_RUNS, className);  
                throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, fe);
            }
        }
        return status;
    }
}
