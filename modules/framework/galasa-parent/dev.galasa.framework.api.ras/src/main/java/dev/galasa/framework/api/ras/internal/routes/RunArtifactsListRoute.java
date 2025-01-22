/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;

import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.api.ras.internal.common.ArtifactsJson;
import dev.galasa.framework.api.ras.internal.common.ArtifactsProperties;
import dev.galasa.framework.api.ras.internal.common.IRunRootArtifact;
import dev.galasa.framework.api.ras.internal.common.RunLogArtifact;
import dev.galasa.framework.api.ras.internal.common.StructureJsonArtifact;
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
import dev.galasa.framework.spi.utils.GalasaGson;

/**
 * Implementation to retrieve a list of artifacts for a given run based on its runId.
 */
public class RunArtifactsListRoute extends RunArtifactsRoute {

    static final GalasaGson gson = new GalasaGson();

    //  Regex to match endpoint: /ras/runs/{runId}/artifacts
    protected static final String path = "\\/runs\\/" + RUN_ID_PATTERN + "\\/artifacts\\/?";

    private List<IRunRootArtifact> rootArtifacts = new ArrayList<>();

    public RunArtifactsListRoute(
        ResponseBuilder responseBuilder,
        IFileSystem fileSystem,
        IFramework framework
    ) throws RBACException {
        super(responseBuilder, path, fileSystem, framework);
        rootArtifacts = Arrays.asList(
            new RunLogArtifact(),
            new StructureJsonArtifact(),
            new ArtifactsProperties(this),
            new ArtifactsJson(this)
        );
    }

    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams, HttpRequestContext requestContext, HttpServletResponse res) throws ServletException, IOException, FrameworkException {
        HttpServletRequest request = requestContext.getRequest();

        Matcher matcher = this.getPathRegex().matcher(pathInfo);
        matcher.matches();
        String runId = matcher.group(1);
        return getResponseBuilder().buildResponse(request, res, "application/json", retrieveResults(runId), HttpServletResponse.SC_OK);
    }

    private String retrieveResults(String runId) throws InternalServletException {
        IRunResult run = null;
        JsonArray artifacts = new JsonArray();
        try {
            run = getRunByRunId(runId);
            run.loadArtifacts();
        } catch (ResultArchiveStoreException e) {
            ServletError error = new ServletError(GAL5002_INVALID_RUN_ID, runId);
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND, e);
        }

        // Build a JSON array of artifacts, then return it as a JSON string
        try {
            artifacts = getArtifacts(run);
            artifacts.addAll(getRootArtifacts(run));
        } catch (ResultArchiveStoreException | IOException ex) {
            ServletError error = new ServletError(GAL5007_ERROR_RETRIEVING_ARTIFACTS_LIST, runId);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex);
        }
        return gson.toJson(artifacts);
    }

    private JsonArray getRootArtifacts(IRunResult run) throws ResultArchiveStoreException, IOException {
        JsonArray artifactRecords = new JsonArray();
        for (IRunRootArtifact rootArtifact : rootArtifacts) {
            byte[] content = rootArtifact.getContent(run);
            if (content != null) {
                artifactRecords.add(getArtifactAsJsonObject(rootArtifact.getPathName(), rootArtifact.getContentType(), content.length));
            }
        }
        return artifactRecords;
    }
}