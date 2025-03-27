/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.net.URL;
import java.util.Properties;
import java.util.Random;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.Api;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.ICertificateStoreService;
import dev.galasa.framework.spi.IConfidentialTextService;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IEventsService;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IResourcePoolingService;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.IShuttableFramework;
import dev.galasa.framework.spi.SharedEnvironmentRunType;
import dev.galasa.framework.spi.auth.IAuthStore;
import dev.galasa.framework.spi.auth.IAuthStoreService;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.streams.IStreamsService;

import static org.assertj.core.api.Assertions.*;

public class MockShutableFramework implements IShuttableFramework {

    private boolean isShutDown = false;
    private IResultArchiveStore ras ;
    private IDynamicStatusStoreService dss ;
    private String testRunName ;
    private IRun run;
    private IFrameworkRuns frameworkRuns;

    public MockShutableFramework(
        IResultArchiveStore ras, 
        IDynamicStatusStoreService dss, 
        String testRunName, 
        IRun run, 
        IFrameworkRuns frameworkRuns
    ) {
        this.ras = ras ;
        this.dss = dss ;
        this.testRunName = testRunName;
        this.run = run;
        this.frameworkRuns = frameworkRuns;
    }

    public boolean isShutDown() {
        return this.isShutDown ;
    }

    @Override
    public void shutdown() throws FrameworkException {
        assertThat(this.isShutDown).as("Framework was shut down twice!").isFalse();
        this.isShutDown = true ;
    }

    @Override
    public @NotNull IResultArchiveStore getResultArchiveStore() {
        return this.ras;
    }


    @Override
    public @NotNull IDynamicStatusStoreService getDynamicStatusStoreService(@NotNull String namespace) {
        return this.dss;
    }

    @Override
    public String getTestRunName() {
        return this.testRunName;
    }

    @Override
    public IRun getTestRun() {
        return run;
    }

    @Override
    public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
        return frameworkRuns ;
    }

    @Override
    public Properties getRecordProperties() {
        return new Properties();
    }

    @Override
    public @NotNull IConfigurationPropertyStoreService getConfigurationPropertyService(@NotNull String namespace)
            throws ConfigurationPropertyStoreException {
               throw new UnsupportedOperationException("Unimplemented method 'getConfigurationPropertyService'");
    }

    // ----------------- un-implemented methods follow -------------------

    @Override
    public void setFrameworkProperties(Properties overrideProperties) {
               throw new UnsupportedOperationException("Unimplemented method 'setFrameworkProperties'");
    }

    @Override
    public boolean isInitialised() {
               throw new UnsupportedOperationException("Unimplemented method 'isInitialised'");
    }

    @Override
    public @NotNull ICertificateStoreService getCertificateStoreService() {
               throw new UnsupportedOperationException("Unimplemented method 'getCertificateStoreService'");
    }

    @Override
    public @NotNull IAuthStore getAuthStore() {
               throw new UnsupportedOperationException("Unimplemented method 'getAuthStore'");
    }

    @Override
    public @NotNull IAuthStoreService getAuthStoreService() {
               throw new UnsupportedOperationException("Unimplemented method 'getAuthStoreService'");
    }

    @Override
    public @NotNull IResourcePoolingService getResourcePoolingService() {
               throw new UnsupportedOperationException("Unimplemented method 'getResourcePoolingService'");
    }

    @Override
    public @NotNull IConfidentialTextService getConfidentialTextService() {
               throw new UnsupportedOperationException("Unimplemented method 'getConfidentialTextService'");
    }

    @Override
    public @NotNull IEventsService getEventsService() {
               throw new UnsupportedOperationException("Unimplemented method 'getEventsService'");
    }

    @Override
    public @NotNull ICredentialsService getCredentialsService() throws CredentialsException {
               throw new UnsupportedOperationException("Unimplemented method 'getCredentialsService'");
    }

    @Override
    public Random getRandom() {
               throw new UnsupportedOperationException("Unimplemented method 'getRandom'");
    }
    public URL getApiUrl(@NotNull Api api) throws FrameworkException {
               throw new UnsupportedOperationException("Unimplemented method 'getApiUrl'");
    }

    @Override
    public SharedEnvironmentRunType getSharedEnvironmentRunType() throws ConfigurationPropertyStoreException {
               throw new UnsupportedOperationException("Unimplemented method 'getSharedEnvironmentRunType'");
    }

    @Override
    public @NotNull RBACService getRBACService() throws RBACException {
        throw new UnsupportedOperationException("Unimplemented method 'getRBACService'");
    }

    @Override
    public @NotNull IStreamsService getStreamsService() {
        throw new UnsupportedOperationException("Unimplemented method 'getStreamsService'");
    }
    
}
