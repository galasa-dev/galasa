/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.linux.internal.dse;

import dev.galasa.ipnetwork.IIpPort;
import dev.galasa.ipnetwork.IpNetworkManagerException;
import dev.galasa.ipnetwork.spi.AbstractGenericIpHost;
import dev.galasa.linux.internal.LinuxManagerImpl;
import dev.galasa.framework.spi.creds.CredentialsException;

public class LinuxDSEIpHost extends AbstractGenericIpHost {

    protected LinuxDSEIpHost(LinuxManagerImpl linuxManager, String hostid)
            throws IpNetworkManagerException, CredentialsException {
        super(linuxManager.getCps(), linuxManager.getDss(), linuxManager.getFramework().getCredentialsService(),
                "image", hostid);
    }

    @Override
    public IIpPort provisionPort(String type) throws IpNetworkManagerException {
        throw new UnsupportedOperationException("Not written yet");
    }

}
