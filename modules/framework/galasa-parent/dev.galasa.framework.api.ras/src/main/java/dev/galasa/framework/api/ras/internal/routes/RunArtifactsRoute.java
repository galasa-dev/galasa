/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.routes;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.rbac.RBACException;

/**
 * An abstract route for holding common code used by multiple artifact-related
 * routes to inherit from.
 *
 * In particular, artifact routes all use a file system which is passed-in.
 */
public abstract class RunArtifactsRoute extends RunsRoute {

    // A shared file-system used by all artifact routes.
    // Get it using the getter method.
    private IFileSystem fileSystem;

    public RunArtifactsRoute(
        ResponseBuilder responseBuilder,
        String path,
        IFileSystem fileSystem,
        IFramework framework
    ) throws RBACException {
        super(responseBuilder, path, framework);
        this.fileSystem = fileSystem;
    }

    protected IFileSystem getFileSystem() {
        return this.fileSystem;
    }

    /**
     * Gets a JsonArray of artifacts for a given test run. The format of the
     * returned JSON array is as follows:
     *
     * <pre>
     * <code>
     * [
     *   {
     *     "path": "/framework/cps_record.properties",
     *     "contentType": "text/plain",
     *     "size": 2306
     *   },
     *   {
     *     "path": "/run.log",
     *     "contentType": "text/plain",
     *     "size": 71478
     *   },
     *   {
     *     "path": "/structure.json",
     *     "contentType": "application/json",
     *     "size": 1301
     *   },
     *   {
     *     "path": "/artifacts.properties",
     *     "contentType": "text/plain",
     *     "size": 378
     *   }
     * ]
     * </code>
     * </pre>
     *
     * @param run
     * @return
     * @throws ResultArchiveStoreException
     * @throws IOException
     */
    public JsonArray getArtifacts(IRunResult run) throws ResultArchiveStoreException, IOException {

        JsonArray artifactRecords = new JsonArray();
        List<Path> artifactPaths = getArtifactPaths(run.getArtifactsRoot(), new ArrayList<>());
        for (Path artifactPath : artifactPaths) {
            JsonObject artifactRecord = getArtifactAsJsonObject("/artifacts" + artifactPath.toString(), fileSystem.probeContentType(artifactPath), fileSystem.size(artifactPath));
            artifactRecords.add(artifactRecord);
        }
        return artifactRecords;
    }

    /**
     * Walks through an artifact directory recursively, collecting each artifact and
     * filtering out all subdirectories
     *
     * @param directory        - the path to an artifact directory
     * @param accumulatedPaths - an intermediate list of accumulated artifact paths
     * @return a list of artifact paths
     */
    protected List<Path> getArtifactPaths(Path directory, List<Path> accumulatedPaths) throws IOException {
        FileSystemProvider fsProvider = directory.getFileSystem().provider();

        try (DirectoryStream<Path> stream = fsProvider.newDirectoryStream(directory, defaultFilter)) {
            for (Path entry : stream) {
                if (fileSystem.isDirectory(entry)) {
                    accumulatedPaths = getArtifactPaths(entry, accumulatedPaths);
                } else {
                    accumulatedPaths.add(entry);
                }
            }
        }
        return accumulatedPaths;
    }

    protected JsonObject getArtifactAsJsonObject(String artifactPath, String contentType, long fileSize) throws IOException {
        JsonObject artifactRecord = new JsonObject();
        artifactRecord.addProperty("path", artifactPath);
        artifactRecord.addProperty("contentType", contentType);
        artifactRecord.addProperty("size", fileSize);
        return artifactRecord;
    }
}