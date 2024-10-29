/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.windows.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.windows.IWindowsImage;
import dev.galasa.windows.IWindowsManager;
import dev.galasa.windows.WindowsManagerException;

public interface IWindowsManagerSpi extends IWindowsManager {

    void registerProvisioner(IWindowsProvisioner provisioner);

    IWindowsImage getImageForTag(@NotNull String imageTag) throws WindowsManagerException;

}
