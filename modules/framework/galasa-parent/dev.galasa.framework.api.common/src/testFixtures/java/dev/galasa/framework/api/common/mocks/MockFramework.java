/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.mocks;

import dev.galasa.framework.spi.Api;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
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

import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import javax.validation.constraints.NotNull;

public class MockFramework implements IFramework {
    private IResultArchiveStore archiveStore;
    private IFrameworkRuns frameworkRuns;
    private MockIConfigurationPropertyStoreService cpsService = new MockIConfigurationPropertyStoreService("framework");
    private MockCredentialsService creds = new MockCredentialsService(new HashMap<>());
    private IAuthStoreService authStoreService;
    private RBACService rbacService;
    private IDynamicStatusStoreService dssService;
    
    public MockFramework() {
        // Do nothing...
    }

    public MockFramework(MockCredentialsService credsService) {
        this.creds = credsService;
    }

    public MockFramework(IAuthStoreService authStoreService) {
        this.authStoreService = authStoreService;
    }

    public MockFramework(IResultArchiveStore archiveStore) {
        this.archiveStore = archiveStore;
    }

    public MockFramework(IFrameworkRuns frameworkRuns){
        this.frameworkRuns = frameworkRuns;
    }

    public MockFramework(RBACService rbacService){ 
        this.rbacService = rbacService;
    }

    public MockFramework(IDynamicStatusStoreService dssService) {
        this.dssService = dssService;
    }

    public MockFramework(IResultArchiveStore archiveStore, IFrameworkRuns frameworkRuns) {
        this.archiveStore = archiveStore;
        this.frameworkRuns = frameworkRuns;
    }

    public MockFramework(IConfigurationPropertyStoreService cpsService){
        this.cpsService = (MockIConfigurationPropertyStoreService) cpsService;
    }

    @Override
    public @NotNull IAuthStoreService getAuthStoreService() {
        return this.authStoreService;
    }

    @Override
    public @NotNull IConfigurationPropertyStoreService getConfigurationPropertyService(@NotNull String namespace)
            throws ConfigurationPropertyStoreException {
            if(this.cpsService.namespaceInput.equalsIgnoreCase("error")){
                throw new ConfigurationPropertyStoreException();
            }
       return this.cpsService;
    }

    @Override
    public @NotNull IResultArchiveStore getResultArchiveStore() {
        return archiveStore;
    }

    @Override
    public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
        return this.frameworkRuns;
    }

    @Override
    public @NotNull ICredentialsService getCredentialsService() throws CredentialsException {
        return this.creds;
    }

    @Override
    public @NotNull RBACService getRBACService() throws RBACException {
        return this.rbacService;
    }

    @Override
    public @NotNull IDynamicStatusStoreService getDynamicStatusStoreService(@NotNull String namespace)
            throws DynamicStatusStoreException {
        return this.dssService;
    }

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
    public @NotNull IResourcePoolingService getResourcePoolingService() {
        throw new UnsupportedOperationException("Unimplemented method 'getResourcePoolingService'");
    }

    @Override
    public @NotNull IConfidentialTextService getConfidentialTextService() {
        throw new UnsupportedOperationException("Unimplemented method 'getConfidentialTextService'");
    }

    @Override
    public String getTestRunName() {
        throw new UnsupportedOperationException("Unimplemented method 'getTestRunName'");
    }

    @Override
    public Random getRandom() {
        throw new UnsupportedOperationException("Unimplemented method 'getRandom'");
    }

    @Override
    public IRun getTestRun() {
        throw new UnsupportedOperationException("Unimplemented method 'getTestRun'");
    }

    @Override
    public Properties getRecordProperties() {
        throw new UnsupportedOperationException("Unimplemented method 'getRecordProperties'");
    }

    @Override
    public URL getApiUrl(@NotNull Api api) throws FrameworkException {
        throw new UnsupportedOperationException("Unimplemented method 'getApiUrl'");
    }

    @Override
    public SharedEnvironmentRunType getSharedEnvironmentRunType() throws ConfigurationPropertyStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'getSharedEnvironmentRunType'");
    }

    @Override
    public @NotNull IAuthStore getAuthStore() {
        throw new UnsupportedOperationException("Unimplemented method 'getAuthStore'");
    }

    @Override
    public @NotNull IEventsService getEventsService() {
        throw new UnsupportedOperationException("Unimplemented method 'getEventsService'");
    }


}