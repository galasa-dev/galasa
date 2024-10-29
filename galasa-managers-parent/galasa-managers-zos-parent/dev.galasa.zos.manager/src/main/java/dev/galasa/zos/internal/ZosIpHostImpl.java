/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.internal;

import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.ipnetwork.IIpPort;
import dev.galasa.ipnetwork.IpNetworkManagerException;
import dev.galasa.ipnetwork.spi.AbstractGenericIpHost;

public class ZosIpHostImpl extends AbstractGenericIpHost {
	
	private ZosManagerImpl zosManager;
	private String image;
	
    public ZosIpHostImpl(ZosManagerImpl zosManager, String imageId) throws IpNetworkManagerException, CredentialsException {
        super(zosManager.getCPS(), zosManager.getDSS(), zosManager.getFramework().getCredentialsService(), "image", imageId);
        this.zosManager = zosManager;
        this.image = imageId;
    }

    @Override
    public IIpPort provisionPort(String type) throws IpNetworkManagerException {
		try {
			String allocatedPort = zosManager.getZosPortController().allocatePort(image);
			return new ZosIpPortImpl(this, Integer.parseInt(allocatedPort), type);
		} catch (Exception exception) {
			throw new IpNetworkManagerException("Exception whilst allocating a z/OS port from the pool");
		}
	}
}
