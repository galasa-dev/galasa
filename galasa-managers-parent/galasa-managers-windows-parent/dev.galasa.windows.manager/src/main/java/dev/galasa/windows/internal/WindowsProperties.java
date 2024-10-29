/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.windows.internal;

import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.windows.WindowsManagerException;

public class WindowsProperties {
    private final IConfigurationPropertyStoreService cps;

    public WindowsProperties(@NotNull IFramework framework) throws WindowsManagerException {
        try {
            this.cps = framework.getConfigurationPropertyService(WindowsManagerImpl.NAMESPACE);
        } catch (ConfigurationPropertyStoreException e) {
            throw new WindowsManagerException("Unable to request CPS for the Windows Manager", e);
        }
    }

    public List<String> getExtraBundles() throws WindowsManagerException {
        try {
            return AbstractManager.split(this.cps.getProperty("bundle.extra", "managers"));
        } catch (ConfigurationPropertyStoreException e) {
            throw new WindowsManagerException("Problem asking CPS for the extra bundles", e);
        }
    }

}
