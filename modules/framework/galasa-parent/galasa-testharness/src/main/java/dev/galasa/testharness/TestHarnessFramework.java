/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.testharness;

import java.net.URL;
import java.util.Properties;
import java.util.Random;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.FrameworkRuns;
import dev.galasa.framework.internal.cps.FrameworkConfigurationPropertyService;
import dev.galasa.framework.internal.cts.FrameworkConfidentialTextService;
import dev.galasa.framework.internal.dss.FrameworkDynamicStatusStoreService;
import dev.galasa.framework.spi.Api;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.FrameworkResourcePoolingService;
import dev.galasa.framework.spi.ICertificateStoreService;
import dev.galasa.framework.spi.IConfidentialTextService;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IEventsService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IResourcePoolingService;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.SharedEnvironmentRunType;
import dev.galasa.framework.spi.auth.IAuthStore;
import dev.galasa.framework.spi.auth.IAuthStoreService;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.streams.IStreamsService;
import dev.galasa.framework.spi.streams.StreamsException;

public class TestHarnessFramework implements IFramework {
    
    public InMemoryCps cpsStore = new InMemoryCps();
    public InMemoryDss dssStore = new InMemoryDss();
    public InMemoryCreds credsStore = new InMemoryCreds();
    public Properties overrides = new Properties();
    public Properties record = new Properties();
    public FrameworkResourcePoolingService rps = new FrameworkResourcePoolingService();
    public FrameworkConfidentialTextService cts = new FrameworkConfidentialTextService();
    
    public FrameworkRuns frameworkRuns;
    
    private String runName;
    public  IRun run; 

    public TestHarnessFramework() throws FrameworkException {
        this.frameworkRuns = new FrameworkRuns(this);
    }

    @Override
    public void setFrameworkProperties(Properties overrideProperties) {
        this.overrides = overrideProperties;
    }

    @Override
    public boolean isInitialised() {
        return true;
    }

    @Override
    public @NotNull IConfigurationPropertyStoreService getConfigurationPropertyService(@NotNull String namespace)
            throws ConfigurationPropertyStoreException {
        return new FrameworkConfigurationPropertyService(this, cpsStore, overrides, record, namespace);
    }

    @Override
    public @NotNull IDynamicStatusStoreService getDynamicStatusStoreService(@NotNull String namespace)
            throws DynamicStatusStoreException {
        return new FrameworkDynamicStatusStoreService(this, dssStore, namespace);
    }

    @Override
    public @NotNull IResultArchiveStore getResultArchiveStore() {
        throw new Unavailable();  // To be written when someone needs it
    }

    @Override
    public @NotNull IResourcePoolingService getResourcePoolingService() {
        return this.rps;
    }

    @Override
    public @NotNull IConfidentialTextService getConfidentialTextService() {
        return cts;
    }

    @Override
    public @NotNull ICredentialsService getCredentialsService() throws CredentialsException {
        return this.credsStore;
    }

    @Override
    public String getTestRunName() {
        return this.runName;
    }
    
    public void setTestRunName(@NotNull String runName) throws DynamicStatusStoreException {
        this.runName = runName;
        this.run     = this.frameworkRuns.getRun(runName);
    }

    @Override
    public Random getRandom() {
        return new Random();
    }

    @Override
    public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
        return this.frameworkRuns;
    }

    @Override
    public IRun getTestRun() {
        return this.run;
    }

    @Override
    public Properties getRecordProperties() {
        return this.record;
    }

    @Override
    public URL getApiUrl(@NotNull Api api) throws FrameworkException {
        throw new Unavailable();// To be written when someone needs it
    }

    @Override
    public SharedEnvironmentRunType getSharedEnvironmentRunType() throws ConfigurationPropertyStoreException {
    	throw new Unavailable();// To be written when someone needs it
    }

	@Override
	public @NotNull ICertificateStoreService getCertificateStoreService() {
		throw new Unavailable();// To be written when someone needs it
	}

    @Override
    public @NotNull IAuthStoreService getAuthStoreService() {
        throw new Unavailable();
    }

    @Override
    public @NotNull IAuthStore getAuthStore() {
        throw new Unavailable();
    }

    @Override
    public @NotNull IEventsService getEventsService() {
        throw new Unavailable();
    }

    @Override
    public @NotNull RBACService getRBACService() throws RBACException {
        throw new UnsupportedOperationException("Unimplemented method 'getRBACService'");
    }

    @Override
    public @NotNull IStreamsService getStreamsService() {
        throw new Unavailable();
    }

}