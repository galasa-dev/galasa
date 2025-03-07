/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal.mocks;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IRun;

public class MockFrameworkRuns implements IFrameworkRuns {

    @Override
    public @NotNull List<IRun> getActiveRuns() throws FrameworkException {
        throw new UnsupportedOperationException("Unimplemented method 'getActiveRuns'");
    }

    @Override
    public @NotNull List<IRun> getQueuedRuns() throws FrameworkException {
        throw new UnsupportedOperationException("Unimplemented method 'getQueuedRuns'");
    }

    @Override
    public @NotNull List<IRun> getAllRuns() throws FrameworkException {
        throw new UnsupportedOperationException("Unimplemented method 'getAllRuns'");
    }

    @Override
    public @NotNull List<IRun> getAllGroupedRuns(@NotNull String groupName) throws FrameworkException {
        throw new UnsupportedOperationException("Unimplemented method 'getAllGroupedRuns'");
    }

    @Override
    public @NotNull Set<String> getActiveRunNames() throws FrameworkException {
        throw new UnsupportedOperationException("Unimplemented method 'getActiveRunNames'");
    }

    @Override
    public @NotNull IRun submitRun(String type, String requestor, String bundleName, String testName, String groupName,
            String mavenRepository, String obr, String stream, boolean local, boolean trace, Properties overrides,
            SharedEnvironmentPhase sharedEnvironmentPhase, String sharedEnvironmentRunName, String language, String runId)
            throws FrameworkException {
        throw new UnsupportedOperationException("Unimplemented method 'submitRun'");
    }

    @Override
    public boolean delete(String runname) throws DynamicStatusStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public IRun getRun(String runname) throws DynamicStatusStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'getRun'");
    }

    @Override
    public boolean reset(String runname) throws DynamicStatusStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'reset'");
    }
    
}
