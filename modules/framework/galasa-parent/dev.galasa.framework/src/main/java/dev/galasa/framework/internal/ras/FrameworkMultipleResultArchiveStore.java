/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.ras;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IResultArchiveStoreService;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.teststructure.TestStructure;

/**
 * Stub for multiple Result Archive Stores.
 *
 *  
 *
 */
public class FrameworkMultipleResultArchiveStore implements IResultArchiveStoreService {

    private final ArrayList<IResultArchiveStoreService> rasServices = new ArrayList<>();

    public FrameworkMultipleResultArchiveStore(@NotNull IFramework framework,
            @NotNull IResultArchiveStoreService rasService) throws ResultArchiveStoreException {
        if (framework.getTestRunName() != null) {
            throw new ResultArchiveStoreException("RAS does not yet support multiple stores during test runs");
        }

        this.rasServices.add(rasService);
    }

    public void addResultArchiveStoreService(@NotNull IResultArchiveStoreService resultArchiveStoreService) {
        this.rasServices.add(resultArchiveStoreService);
    }

    /*
     * (non-Javadoc)
     *
     * @see dev.galasa.framework.spi.IResultArchiveStore#writeLog(java.lang.String)
     */
    @Override
    public void writeLog(@NotNull String message) throws ResultArchiveStoreException {
        for (IResultArchiveStoreService rasService : this.rasServices) {
            rasService.writeLog(message);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see dev.galasa.framework.spi.IResultArchiveStore#writeLog(java.util.List)
     */
    @Override
    public void writeLog(@NotNull List<String> messages) throws ResultArchiveStoreException {
        for (IResultArchiveStoreService rasService : this.rasServices) {
            rasService.writeLog(messages);
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
        for (IResultArchiveStoreService rasService : this.rasServices) {
            rasService.updateTestStructure(testStructure);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see dev.galasa.framework.spi.IResultArchiveStore#getStoredArtifactsRoot()
     */
    @Override
    public Path getStoredArtifactsRoot() {
        // *** Multiple RASs not supported yet during test runs
        // *** We need to create an stub filesystem to be able to write to 2 filesystms
        // and read from the first
        return this.rasServices.get(0).getStoredArtifactsRoot();
    }

    @Override
    public void flush() {
        for (IResultArchiveStoreService rasService : this.rasServices) {
            rasService.flush();
        }
    }

    @Override
    public void shutdown() {
        for (IResultArchiveStoreService rasService : this.rasServices) {
            rasService.shutdown();
        }
    }

//	@Override
//	public List<IRunResult> getRuns(String runName) throws ResultArchiveStoreException {
//		for(IResultArchiveStoreService rasService : this.rasServices) {
//			return rasService.getRuns(runName);
//		}
//		return new ArrayList<>();
//	}

    @Override
    public @NotNull List<IResultArchiveStoreDirectoryService> getDirectoryServices() {
        ArrayList<IResultArchiveStoreDirectoryService> dirs = new ArrayList<>();

        for (IResultArchiveStoreService rasService : this.rasServices) {
            dirs.addAll(rasService.getDirectoryServices());
        }

        return dirs;
    }

    @Override
    public String calculateRasRunId() {
        
        if (this.rasServices.size() > 0) {
            return this.rasServices.get(0).calculateRasRunId();
        }
        
        return null;
    }
    

}