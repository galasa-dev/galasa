/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.ras.directory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import dev.galasa.framework.FileSystem;
import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.teststructure.TestStructure;
import dev.galasa.framework.spi.utils.GalasaGson;

public class DirectoryRASRunResult implements IRunResult {

    private static final int LOG_STREAM_BUFFER_BYTES = 8 * 1024;

    private final Path                           runDirectory;
    private final TestStructure                  testStructure;
    private final DirectoryRASFileSystemProvider fileSystemProvider;
    private final String                         id;
    private final IFileSystem                    fileSystem;

    protected DirectoryRASRunResult(Path runDirectory, GalasaGson gson, String id)
            throws JsonSyntaxException, JsonIOException, IOException {
        this(runDirectory, gson, id, new FileSystem(), new DirectoryRASFileSystemProvider(runDirectory));
    }

    protected DirectoryRASRunResult(
        Path runDirectory,
        GalasaGson gson,
        String id,
        IFileSystem fileSystem,
        DirectoryRASFileSystemProvider fileSystemProvider
    )
            throws JsonSyntaxException, JsonIOException, IOException {
        this.runDirectory = runDirectory;
        this.id           = id;
        this.fileSystem   = fileSystem;

        Path structureFile = this.runDirectory.resolve("structure.json");
        
        try (InputStreamReader in = new InputStreamReader(fileSystem.newInputStream(structureFile))){
           this.testStructure = gson.fromJson(in, TestStructure.class);
        }

        this.fileSystemProvider = fileSystemProvider;
    }
    
    //for testing purposes
    protected DirectoryRASRunResult() {
    	this.testStructure = null;
    	this.runDirectory = null;
    	this.fileSystemProvider = null;
    	this.id                 = null;
        this.fileSystem = null;
    }

    @Override
    public TestStructure getTestStructure() throws ResultArchiveStoreException {
        return this.testStructure;
    }

    @Override
    public Path getArtifactsRoot() throws ResultArchiveStoreException {
        return this.fileSystemProvider.getActualFileSystem().getPath("/");
    }

    @Override
    public String getLog() throws ResultArchiveStoreException {

        Path runLog = runDirectory.resolve("run.log");
        if (Files.exists(runLog)) {
            try {
                return new String(Files.readAllBytes(runLog));
            } catch (Exception e) {
                throw new ResultArchiveStoreException("Unable to read the run log at " + runLog.toString(), e);
            }
        }

        return "";
    }

    @Override
    public void streamLog(OutputStream outputStream) throws ResultArchiveStoreException {
        Path runLog = runDirectory.resolve("run.log");
        if (fileSystem.exists(runLog)) {
            try (InputStream inputStream = fileSystem.newInputStream(runLog)) {
                byte[] buffer = new byte[LOG_STREAM_BUFFER_BYTES];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            } catch (IOException e) {
                throw new ResultArchiveStoreException("Unable to stream the run log at " + runLog.toString(), e);
            }
        }
    }

    @Override
    public long getLogSize() throws ResultArchiveStoreException {
        long size = 0;
        
        // First try to get size from TestStructure metadata
        if (testStructure != null) {
            Long logSize = testStructure.getLogSize();
            if (logSize != null) {
                size = logSize.longValue();
            } else {
                // Fall back to checking file size for legacy runs
                Path runLog = runDirectory.resolve("run.log");
                if (fileSystem.exists(runLog)) {
                    try {
                        size = fileSystem.size(runLog);
                    } catch (IOException e) {
                        throw new ResultArchiveStoreException("Unable to get size of run log at " + runLog.toString(), e);
                    }
                }
            }
        }
        
        return size;
    }

    public void discard() throws ResultArchiveStoreException {
        //TODO
    }

    @Override
    public String getRunId() {
        return this.id;
    }

    @Override
    public void loadArtifacts() throws ResultArchiveStoreException {
        // Artifacts for local runs are already available on the filesystem so there is no need to load anything
    }

    @Override
    public void loadArtifact(String artifactPath) throws ResultArchiveStoreException {
        // Artifacts for local runs are already available on the filesystem so there is no need to load anything
    }
}