/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.ras.directory;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IResultArchiveStoreService;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.teststructure.TestStructure;
import dev.galasa.framework.spi.utils.GalasaGson;

/**
 * A RAS Service for storing the result archive store
 *
 *  
 *
 */
@Component(service = { IResultArchiveStoreService.class })
public class DirectoryResultArchiveStoreService implements IResultArchiveStoreService {

    private static final Charset           UTF8     = Charset.forName("utf-8");

    private final IFramework               framework;                           // NOSONAR
    private final URI                      rasUri;
    private final Path                     baseDirectory;

    private boolean                        shutdown = false;

    private Path                           runDirectory;
    private Path                           testStructureFile;
    private Path                           runLog;

    private final GalasaGson                     gson     = new GalasaGson();

    private DirectoryRASFileSystemProvider provider;

    public DirectoryResultArchiveStoreService(IFramework framework, URI rasUri) throws ResultArchiveStoreException {
        this.framework = framework;
        this.rasUri = rasUri;

        // *** Create the base RAS directory
        this.baseDirectory = Paths.get(this.rasUri);
        try {
            Files.createDirectories(this.baseDirectory);
        } catch (final Exception e) {
            throw new ResultArchiveStoreException(
                    "Unable to create the RAS base directory '" + this.baseDirectory + "'", e);
        }

        // *** Get the runname to create the directory
        final String runName = this.framework.getTestRunName();
        if (runName == null) { // *** Dont need to do anything more for non runs
            return;
        }
        setRasRun(runName);

        // *** Set the locations of the standard files
        this.testStructureFile = this.runDirectory.resolve("structure.json");
        this.runLog = this.runDirectory.resolve("run.log");

        // *** Create an empty run log so we append it
        try {
            Files.createFile(this.runLog);
        } catch (final IOException e) {
            throw new ResultArchiveStoreException("Unable to create Run Log", e);
        }

        // *** Setup the provider to do all the work
        try {
            this.provider = new DirectoryRASFileSystemProvider(this.runDirectory);
        } catch (final IOException e) {
            throw new ResultArchiveStoreException("Unable to create the RAS Provider", e);
        }

    }

    /**
     * Setup the run directory
     *
     * @param runname     - the name of the run
     * @param testRunName - the name of the run
     * @throws ResultArchiveStoreException - if we can't create the directories
     */
    private void setRasRun(String runname) throws ResultArchiveStoreException {
        this.runDirectory = this.baseDirectory.resolve(runname);
        try {
            // *** If this run name directory exists move it to a similar named one. This
            // maybe
            // *** possible for framework and manager development where the runname maybe
            // reused often
            if (Files.exists(runDirectory)) {
                Path movePath = null;
                for (int i = 2;; i++) {
                    movePath = this.runDirectory.resolveSibling(runname + "-" + Integer.toString(i));
                    if (!Files.exists(movePath)) {
                        Files.move(runDirectory, movePath);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw new ResultArchiveStoreException("Unable to create the RAS run directory '" + this.runDirectory + "'",
                    e);
        }

        try {
            Files.createDirectories(this.runDirectory);
            Files.createDirectories(this.runDirectory.resolve("artifacts"));
        } catch (final IOException e) {
            throw new ResultArchiveStoreException("Unable to create the RAS run directory '" + this.runDirectory + "'",
                    e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see dev.galasa.framework.spi.IResultArchiveStore#writeLog(java.lang.String)
     */
    @Override
    public void writeLog(@NotNull String message) throws ResultArchiveStoreException {
        Objects.requireNonNull(message);

        if (!message.endsWith("\n")) {
            message = message + "\n";
        }

        //If the framework is shutting down we will have lost the CTS - by which point 
        //there should be no confidential text to obscure anyway
        if(framework.getConfidentialTextService() != null)
            message = framework.getConfidentialTextService().removeConfidentialText(message);

        try {
            Files.write(this.runLog, message.getBytes(UTF8), StandardOpenOption.APPEND);
        } catch (final Exception e) {
            throw new ResultArchiveStoreException("Unable to write message to run log", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see dev.galasa.framework.spi.IResultArchiveStore#writeLog(java.util.List)
     */
    @Override
    public void writeLog(@NotNull List<String> messages) throws ResultArchiveStoreException {
        Objects.requireNonNull(messages);
        for (final String message : messages) {
            try{
                this.writeLog(message);
            } catch(final Exception e){
                throw new ResultArchiveStoreException("Unable to write messages to run log", e);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * dev.galasa.framework.spi.IResultArchiveStore#updateTestStructure(dev.galasa.
     * framework.spi.teststructure.ITestStructure)
     */
    @Override
    public void updateTestStructure(@NotNull TestStructure testStructure) throws ResultArchiveStoreException {
        try {
            testStructure.normalise();
            final String json = this.gson.toJson(testStructure);
            Files.write(this.testStructureFile, json.getBytes(UTF8));
        } catch (final Exception e) {
            throw new ResultArchiveStoreException("Unable to write the test structure", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see dev.galasa.framework.spi.IResultArchiveStore#getStoredArtifactsRoot()
     */
    @Override
    public Path getStoredArtifactsRoot() {
        return this.provider.getActualFileSystem().getPath("/");
    }

    @Override
    public void flush() {
    }

    @Override
    public void shutdown() {
        this.shutdown = true;
    }

    public boolean isShutdown() {
        return this.shutdown;
    }

    @Override
    public @NotNull List<IResultArchiveStoreDirectoryService> getDirectoryServices() {
        ArrayList<IResultArchiveStoreDirectoryService> dirs = new ArrayList<>(1);
        dirs.add(new DirectoryRASDirectoryService(this.baseDirectory, gson));
        return dirs;
    }

}