/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.windows.internal;

import dev.galasa.ipnetwork.IIpPort;
import dev.galasa.ipnetwork.IpNetworkManagerException;
import dev.galasa.ipnetwork.spi.AbstractGenericIpHost;
import dev.galasa.framework.spi.creds.CredentialsException;

public class WindowsDSEIpHost extends AbstractGenericIpHost {

    protected WindowsDSEIpHost(WindowsManagerImpl windowsManager, String hostid)
            throws IpNetworkManagerException, CredentialsException {
        super(windowsManager.getCps(), windowsManager.getDss(), windowsManager.getFramework().getCredentialsService(),
                "image", hostid);
    }

    @Override
    public IIpPort provisionPort(String type) throws IpNetworkManagerException {
        throw new UnsupportedOperationException("Not written yet");
    }

}
