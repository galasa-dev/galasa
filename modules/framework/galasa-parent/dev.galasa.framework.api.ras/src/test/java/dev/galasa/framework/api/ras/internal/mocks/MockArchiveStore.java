/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.mocks;

import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.teststructure.TestStructure;

import java.nio.file.Path;
import java.util.List;
import javax.validation.constraints.NotNull;

public class MockArchiveStore implements IResultArchiveStore {

    private List<IResultArchiveStoreDirectoryService> directoryServices;


    public MockArchiveStore(List<IResultArchiveStoreDirectoryService> directoryServices) {
        this.directoryServices = directoryServices;
    }

    @Override
    public void writeLog(@NotNull String message) throws ResultArchiveStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'writeLog'");
    }

    @Override
    public void writeLog(@NotNull List<String> messages) throws ResultArchiveStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'writeLog'");
    }

    @Override
    public void updateTestStructure(@NotNull TestStructure testStructure) throws ResultArchiveStoreException {
        try {
            testStructure.normalise();
        } catch (final Exception e) {
            throw new ResultArchiveStoreException("Unable to write the test structure", e);
        }
    }

    @Override
    public Path getStoredArtifactsRoot() {
        throw new UnsupportedOperationException("Unimplemented method 'getStoredArtifactsRoot'");
    }

    @Override
    public void flush() {
        throw new UnsupportedOperationException("Unimplemented method 'flush'");
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("Unimplemented method 'shutdown'");
    }

    @Override
    public @NotNull List<IResultArchiveStoreDirectoryService> getDirectoryServices() {
        return this.directoryServices;
    }
}