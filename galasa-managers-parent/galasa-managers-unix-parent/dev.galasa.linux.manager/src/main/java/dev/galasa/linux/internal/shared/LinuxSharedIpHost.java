/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.linux.internal.shared;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.ipnetwork.IIpPort;
import dev.galasa.ipnetwork.IpNetworkManagerException;
import dev.galasa.ipnetwork.spi.AbstractGenericIpHost;
import dev.galasa.linux.internal.LinuxManagerImpl;

public class LinuxSharedIpHost extends AbstractGenericIpHost {

    protected LinuxSharedIpHost(LinuxManagerImpl linuxManager, String hostid)
            throws IpNetworkManagerException, CredentialsException {
        super(linuxManager.getCps(), linuxManager.getDss(), linuxManager.getFramework().getCredentialsService(),
                "image", hostid);
    }

    public LinuxSharedIpHost(IFramework framework, IConfigurationPropertyStoreService cps, IDynamicStatusStoreService dss, String hostid)
            throws IpNetworkManagerException, CredentialsException {
        super(cps, dss, framework.getCredentialsService(),
                "image", hostid);
    }

    @Override
    public IIpPort provisionPort(String type) throws IpNetworkManagerException {
        throw new UnsupportedOperationException("Not written yet");
    }

}
