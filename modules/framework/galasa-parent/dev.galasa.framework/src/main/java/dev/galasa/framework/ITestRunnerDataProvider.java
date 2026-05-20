/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import java.util.Properties;

import dev.galasa.framework.internal.runner.ITestRunnerEventsProducer;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.IShuttableFramework;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.framework.spi.streams.IStreamsService;

public interface ITestRunnerDataProvider {
    public IRun getRun();
    public IConfigurationPropertyStoreService getCPS();
    public ICredentialsService getCredentialsService();
    public IDynamicStatusStoreService getDSS();
    public IResultArchiveStore getRAS();
    public IStreamsService getStreamsService();
    public IShuttableFramework getFramework();
    public IBundleManager getBundleManager();
    public IAnnotationExtractor getAnnotationExtractor();
    public Properties getOverrideProperties();
    public ITestRunManagers createTestRunManagers(GalasaTest galasaTest) throws TestRunException;
    public IFileSystem getFileSystem();
    public ITestRunnerEventsProducer getEventsProducer();
}
