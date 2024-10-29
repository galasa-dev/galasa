/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.linux.internal.dse;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.linux.LinuxManagerException;
import dev.galasa.linux.OperatingSystem;
import dev.galasa.linux.internal.LinuxManagerImpl;
import dev.galasa.linux.spi.ILinuxProvisionedImage;
import dev.galasa.linux.spi.ILinuxProvisioner;

public class LinuxDSEProvisioner implements ILinuxProvisioner {

    private final Log                                logger = LogFactory.getLog(getClass());

    private final LinuxManagerImpl                   manager;
    private final IConfigurationPropertyStoreService cps;

    public LinuxDSEProvisioner(LinuxManagerImpl manager) {

        this.manager = manager;
        this.cps = this.manager.getCps();
    }

    @Override
    public ILinuxProvisionedImage provisionLinux(String tag, OperatingSystem operatingSystem, List<String> capabilities)
            throws LinuxManagerException {

        try {
            String hostid = LinuxManagerImpl.nulled(this.cps.getProperty("dse.tag", tag + ".hostid"));

            if (hostid == null) {
                return null;
            }

            logger.info("Loading DSE for Linux Image tagged " + tag + ", host ID is " + hostid);

            return new LinuxDSEImage(manager, this.cps, tag, hostid);
        } catch (Exception e) {
            throw new LinuxManagerException("Unable to provision the Linux DSE", e);
        }
    }

    @Override
    public int getLinuxPriority() {
        // Make sure the DSE provisioner is at the top of the list
        return Integer.MAX_VALUE;
    }

}
