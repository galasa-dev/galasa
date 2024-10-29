/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.windows.internal;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.windows.WindowsManagerException;
import dev.galasa.windows.spi.IWindowsProvisionedImage;
import dev.galasa.windows.spi.IWindowsProvisioner;

public class WindowsDSEProvisioner implements IWindowsProvisioner {

    private final Log                                logger = LogFactory.getLog(getClass());

    private final WindowsManagerImpl                   manager;
    private final IConfigurationPropertyStoreService cps;

    public WindowsDSEProvisioner(WindowsManagerImpl manager) {

        this.manager = manager;
        this.cps = this.manager.getCps();
    }

    @Override
    public IWindowsProvisionedImage provisionWindows(String tag, List<String> capabilities)
            throws WindowsManagerException {

        try {
            String hostid = WindowsManagerImpl.nulled(this.cps.getProperty("dse.tag", tag + ".hostid"));

            if (hostid == null) {
                return null;
            }

            logger.info("Loading DSE for Windows Image tagged " + tag);

            return new WindowsDSEImage(manager, this.cps, tag, hostid);
        } catch (Exception e) {
            throw new WindowsManagerException("Unable to provision the Windows DSE", e);
        }
    }

}
