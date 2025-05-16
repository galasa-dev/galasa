/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ProtectedRoute;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.ResultNames;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.ras.RasTestClass;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.utils.GalasaGson;


/**
 * An abstract route used by all the Run-related routes.
 */
public abstract class RunsRoute extends ProtectedRoute {

    static final GalasaGson gson = new GalasaGson();

    // A pattern for run IDs allowing IDs containing at least one character of:
    // Alphanumeric characters (A-Za-z0-9)
    // Periods (.)
    // Dashes (-)
    // Equals signs (=)
    // Underscores (_)
    protected static final String RUN_ID_PATTERN = "([A-Za-z0-9.\\-=_]+)";

    // Define a default filter to accept everything
    static DirectoryStream.Filter<Path> defaultFilter = path -> { return true; };

    private IFramework framework;

    public RunsRoute(ResponseBuilder responseBuilder, String path, IFramework framework) throws RBACException {
        super(responseBuilder, path, framework.getRBACService());
        this.framework = framework;
    }

    protected IFramework getFramework() {
        return this.framework;
    }

    protected List<String> getResultNames () throws InternalServletException{
		List<String> resultsList = new ArrayList<>();

		try {
			for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {
                List<String> results = directoryService.getResultNames();
                if (results != null){
				resultsList.addAll(directoryService.getResultNames());
                }
			}
            for (String defaultResultName : ResultNames.getDefaultResultNames()){
				if (!resultsList.contains(defaultResultName)){
					resultsList.add(defaultResultName);
				}
		    }
		}
        catch(ResultArchiveStoreException r){
            ServletError error = new ServletError(GAL5004_ERROR_RETRIEVING_PAGE );
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, r);
        }

		Collections.sort(resultsList);

		return resultsList;
	}


    /**
     * 
     * @param id The id of the run. This is not the shortName, but the longer one starting with 'cdb-'. This is unique over the system.
     * @return The run we found with that ID. Or null if the run was not found
     * @throws ResultArchiveStoreException
     * @throws InternalServletException
     */
    protected @NotNull IRunResult getRunByRunId(@NotNull String id) throws ResultArchiveStoreException, InternalServletException {
        IRunResult run = null;

        for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {

            run = directoryService.getRunById(id);
            if (run != null) {
                break;
            }
        }

        if (run == null) {
            ServletError error = new ServletError(GAL5091_ERROR_RUN_NOT_FOUND_BY_ID, id);
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
        return run;
    }

    /**
     * 
     * @param groupId The groupId of a group of runs.
     * @return A list of runs matching the provided groupId
     * @throws ResultArchiveStoreException
     * @throws InternalServletException
     */
    protected @NotNull List<IRunResult> getRunsByGroupId(@NotNull String groupId) throws ResultArchiveStoreException, InternalServletException {

        HashSet<IRunResult> runsSet = new HashSet<>();

        for(IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {
            runsSet.addAll(directoryService.getRunsByGroupName(groupId)); 
        }

        List <IRunResult> groupRuns = new ArrayList<>(runsSet);
        return groupRuns;

    }

    protected List<String> getRequestors() throws ResultArchiveStoreException{
		HashSet<String> requestorSet = new HashSet<>();
		for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {
			requestorSet.addAll(directoryService.getRequestors());
		}
		//convert to list of strings
		List<String> requestors = new ArrayList<>(requestorSet);
		return requestors;
	}

    protected List<RasTestClass> getTestClasses() throws ResultArchiveStoreException, ServletException{
        List<RasTestClass> testClasses = new ArrayList<>();
        for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {
            testClasses.addAll(directoryService.getTests());
        }
        return testClasses;
    }
}