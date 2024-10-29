/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import java.nio.file.Path;
import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.teststructure.TestStructure;

/**
 * <p>
 * Used to gain access to the Result Archive Store (RAS)
 * </p>
 * 
 * <p>
 * POnce initialised, the Framework will provide access to 0 or more Result
 * Archive Stores.
 * </p>
 * 
 * <p>
 * Apache CouchDB is the preferred RAS for an automation, and a file directory
 * on the local filesystem is the preferred RAS for local runs.
 * </p>
 * 
 * <p>
 * An {@link IResultArchiveStore} can be obtained from
 * {@link IFramework#getResultArchiveStore()}.
 * </p>
 * 
 *  
 *
 */
public interface IResultArchiveStore {

    /**
     * Write a message to the run log in the RASs
     * 
     * @param message - a Message to write to the run log
     * @throws ResultArchiveStoreException - If there is a problem writing to the
     *                                     store
     */
    void writeLog(@NotNull String message) throws ResultArchiveStoreException;

    /**
     * Write multiple messages to the run log in the RASs
     * 
     * @param messages - Messages to write
     * @throws ResultArchiveStoreException - If there is a problem writing to the
     *                                     store
     */
    void writeLog(@NotNull List<String> messages) throws ResultArchiveStoreException;

    /**
     * Update the Test Structure object in the RASs with the current status
     * 
     * @param testStructure - The Test Structure
     * @throws ResultArchiveStoreException - If there is a problem writing to the
     *                                     store
     */
    void updateTestStructure(@NotNull TestStructure testStructure) throws ResultArchiveStoreException;


    /**
     * Obtain the root directory of the stored artifacts file system
     * 
     * @return a {@link java.nio.file.Path}
     */
    Path getStoredArtifactsRoot();

    void flush();

    void shutdown();

    @NotNull
    List<IResultArchiveStoreDirectoryService> getDirectoryServices();
    
    String calculateRasRunId();

}
