/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

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
 * Implementation to download an artifact for a given run based on its runId and the path
 * to the artifact.
 */
public class RunArtifactsDownloadRoute extends RunArtifactsRoute {

    static final GalasaGson gson = new GalasaGson();

    // A pattern for artifact file paths that allows file paths containing at least one character of:
    // Alphanumeric characters (A-Za-z0-9)
    // periods (.)
    // dashes (-)
    // Equals signs (=)
    // Underscores (_)
    // Slashes (/)
    // Parentheses ( '('' and ')' )
    private static final String ARTIFACT_PATH_PATTERN = "([A-Za-z0-9.\\-=_\\/\\(\\)]+)";

    // The regex pattern for the "/ras/runs/{run-id}/files/{artifact-path}" endpoint
    private static final String path = "\\/runs\\/" + RUN_ID_PATTERN + "\\/files\\/" + ARTIFACT_PATH_PATTERN;

    private Map<String, IRunRootArtifact> rootArtifacts = new HashMap<>();

    public RunArtifactsDownloadRoute(ResponseBuilder responseBuilder, IFileSystem fileSystem, IFramework framework) throws RBACException {
        super(responseBuilder,
              path,
              fileSystem,
              framework
        );

        rootArtifacts.put("run.log", new RunLogArtifact());
        rootArtifacts.put("structure.json", new StructureJsonArtifact());
        rootArtifacts.put("artifacts.properties", new ArtifactsProperties(this));
        rootArtifacts.put("artifacts.json", new ArtifactsJson(this));
    }

    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams, HttpRequestContext requestContext, HttpServletResponse response) throws ServletException, IOException, FrameworkException {
        Matcher matcher = this.getPathRegex().matcher(pathInfo);
        matcher.matches();
        String runId = matcher.group(1);
        String artifactPath = matcher.group(2);
        return downloadArtifact(runId, artifactPath, response);
    }

    private HttpServletResponse downloadArtifact(String runId, String artifactPath, HttpServletResponse res) throws InternalServletException, IOException {
        IRunResult run = null;
        String runName = "";
        String artifactsPrefix = "artifacts/";

        // Get run details in order to find artifacts
        try {
            run = getRunByRunId(runId);
            run.loadArtifacts();
            runName = run.getTestStructure().getRunName();
        } catch (ResultArchiveStoreException e) {
            ServletError error = new ServletError(GAL5002_INVALID_RUN_ID,runId);
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND, e);
        }

        // Download the artifact that matches the artifact path or starts with "artifacts/"
        try {
            IRunRootArtifact artifact = rootArtifacts.get(artifactPath);
            if (artifact != null) {
                res = setDownloadResponse(res, artifact.getContent(run), artifact.getContentType());
            } else if (artifactPath.startsWith(artifactsPrefix)) {
                res = downloadStoredArtifact(res, run, artifactPath.substring(artifactsPrefix.length() - 1));
            } else {
                ServletError error = new ServletError(GAL5008_ERROR_LOCATING_ARTIFACT, artifactPath, runName);
                throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (ResultArchiveStoreException | IOException ex) {
            ServletError error = new ServletError(GAL5009_ERROR_RETRIEVING_ARTIFACT, artifactPath, runName);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex);
        }
        return res;
    }

    private HttpServletResponse downloadStoredArtifact(HttpServletResponse res, IRunResult run, String artifactPath) throws ResultArchiveStoreException, IOException {
        FileSystem artifactFileSystem = run.getArtifactsRoot().getFileSystem();
        Path artifactLocation = artifactFileSystem.getPath(artifactPath);

        // Open the artifact for reading
        Set<OpenOption> options = new HashSet<>();
        options.add(StandardOpenOption.READ);
        try (ByteChannel channel = artifactFileSystem.provider().newByteChannel(artifactLocation, options, new FileAttribute<?>[]{});
            OutputStream outStream = res.getOutputStream()) {

            // Create a buffer to read small amounts of data into to avoid out-of-memory issues
            int bufferCapacity = 1024;
            ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);

            // Read the artifact and write it to the response's output stream
            int bytesRead = channel.read(buffer);
            while (bytesRead > 0) {
                buffer.flip();
                byte[] bytes = new byte[bytesRead];
                buffer.get(bytes);

                outStream.write(bytes);

                buffer.clear();
                bytesRead = channel.read(buffer);
            }
            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentType(getFileSystem().probeContentType(artifactLocation));
            res.setHeader("Content-Disposition", "attachment");
        }
        return res;
    }

    private HttpServletResponse setDownloadResponse(HttpServletResponse res, byte[] content, String contentType) throws IOException {
        OutputStream outStream = res.getOutputStream();
        res.setStatus(HttpServletResponse.SC_OK);
        res.setContentType(contentType);
        res.setHeader("Content-Disposition", "attachment");
        outStream.write(content);
        outStream.close();
        return res;
    }
}