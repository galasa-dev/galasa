/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ipnetwork.internal;

import java.nio.file.FileSystem;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ICredentials;
import dev.galasa.ManagerException;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.IResourcePoolingService;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.ipnetwork.IpNetworkManagerException;
import dev.galasa.ipnetwork.internal.ssh.filesystem.SSHFileSystem;
import dev.galasa.ipnetwork.spi.IIpNetworkManagerSpi;
import dev.galasa.ipnetwork.spi.SSHClient;

@Component(service = { IManager.class })
public class IpNetworkManagerImpl extends AbstractManager implements IIpNetworkManagerSpi {
    protected final static String              NAMESPACE = "ipnetwork";

    @SuppressWarnings("unused")
    private final static Log                   logger    = LogFactory.getLog(IpNetworkManagerImpl.class);

    private IFramework                         framework;
    private IConfigurationPropertyStoreService cps;
    private IDynamicStatusStoreService         dss;

    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);

        try {
            this.framework = framework;
            this.cps = framework.getConfigurationPropertyService(NAMESPACE);
            this.dss = framework.getDynamicStatusStoreService(NAMESPACE);
        } catch (Exception e) {
            throw new IpNetworkManagerException("Unable to initialise the IP Network Manager", e);
        }
    }

    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest)
            throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);
    }

    public IConfigurationPropertyStoreService getCPS() {
        return this.cps;
    }

    public IDynamicStatusStoreService getDSS() {
        return this.dss;
    }

    public IResourcePoolingService getRPS() {
        return this.framework.getResourcePoolingService();
    }

    @Override
    public @NotNull ICommandShell getCommandShell(IIpHost ipHost, ICredentials credentials)
            throws IpNetworkManagerException {
        return new SSHClient(ipHost.getHostname(), ipHost.getSshPort(), credentials, 60000);
    }

    @Override
    public @NotNull FileSystem getFileSystem(IIpHost ipHost) throws IpNetworkManagerException {
        return new SSHFileSystem(ipHost.getHostname(), ipHost.getSshPort(), ipHost.getDefaultCredentials());
    }

    @Override
    public @NotNull FileSystem getFileSystem(IIpHost ipHost, ICredentials credentials) throws IpNetworkManagerException {
        return new SSHFileSystem(ipHost.getHostname(), ipHost.getSshPort(), credentials);
    }

}
