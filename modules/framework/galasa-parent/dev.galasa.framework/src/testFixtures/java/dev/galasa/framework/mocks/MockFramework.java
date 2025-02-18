/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import dev.galasa.framework.Framework;
import dev.galasa.framework.internal.cps.FpfConfigurationPropertyStore;
import dev.galasa.framework.internal.cps.FrameworkConfigurationPropertyService;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;

import java.io.File;
import java.util.Properties;
import javax.validation.constraints.NotNull;

public class MockFramework extends Framework {

    private Properties overrides = new Properties();
    private Properties records = new Properties();
    private File cpsFile;

    private MockDSSStore mockDss;
    private MockCPSStore mockCps;

    private String runName ;

    public boolean isLogCaptured = false ;

    public MockFramework() {
        super();
    }

    public MockFramework(MockCPSStore mockCps, MockDSSStore mockDss) {
        this.mockCps = mockCps;
        this.mockDss = mockDss;
    }

    public MockFramework(File cpsFile) {
        this.cpsFile = cpsFile;
    }

    @Override
    public @NotNull IConfigurationPropertyStoreService getConfigurationPropertyService(@NotNull String namespace) throws ConfigurationPropertyStoreException {
        IConfigurationPropertyStoreService cpsService = null;
        if (this.mockCps != null) {
            cpsService = mockCps;
        } else if (this.cpsFile != null) {
            try {
                cpsService = new FrameworkConfigurationPropertyService(this,
                        new FpfConfigurationPropertyStore(cpsFile.toURI()), overrides, records, namespace);
            } catch (Exception e) {
                throw new ConfigurationPropertyStoreException("error initialising", e);
            }
        } else {
            cpsService = super.getConfigurationPropertyService(namespace);
        }
        return cpsService;
    }

    @Override
    public @NotNull IDynamicStatusStoreService getDynamicStatusStoreService(@NotNull String namespace) throws DynamicStatusStoreException {
        IDynamicStatusStoreService dss = mockDss;
        if (dss == null) {
            dss = super.getDynamicStatusStoreService(namespace);
        }
        return dss;
    }

    public void setMockCps(MockCPSStore mockCps) {
        this.mockCps = mockCps;
    }

    public void setMockDss(MockDSSStore mockDss) {
        this.mockDss = mockDss;
    }

    @Override
    public void setTestRunName(String runName) throws FrameworkException {
        this.runName = runName;
    }

    @Override
    public String getTestRunName() {
        return this.runName ;
    }
    
    @Override
    public void installLogCapture() {
        this.isLogCaptured = true;
    }
}