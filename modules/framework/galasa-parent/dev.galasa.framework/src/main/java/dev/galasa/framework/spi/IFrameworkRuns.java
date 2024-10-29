/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.validation.constraints.NotNull;

public interface IFrameworkRuns {
    
    public enum SharedEnvironmentPhase {
        BUILD,
        DISCARD
    }
    

    @NotNull
    List<IRun> getActiveRuns() throws FrameworkException;

    @NotNull
    List<IRun> getQueuedRuns() throws FrameworkException;;

    @NotNull
    List<IRun> getAllRuns() throws FrameworkException;

    @NotNull
    List<IRun> getAllGroupedRuns(@NotNull String groupName) throws FrameworkException;

    @NotNull
    Set<String> getActiveRunNames() throws FrameworkException;

    @NotNull
    IRun submitRun(String type, String requestor, String bundleName, String testName, String groupName,
            String mavenRepository, String obr, String stream, boolean local, boolean trace, Properties overrides,
            SharedEnvironmentPhase sharedEnvironmentPhase, String sharedEnvironmentRunName, String language) throws FrameworkException;

    boolean delete(String runname) throws DynamicStatusStoreException;

    IRun getRun(String runname) throws DynamicStatusStoreException;

    boolean reset(String runname) throws DynamicStatusStoreException;

}
