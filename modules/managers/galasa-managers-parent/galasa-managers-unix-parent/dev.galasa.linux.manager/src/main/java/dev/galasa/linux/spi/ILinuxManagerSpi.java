/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.linux.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.linux.ILinuxImage;
import dev.galasa.linux.ILinuxManager;
import dev.galasa.linux.LinuxManagerException;

public interface ILinuxManagerSpi extends ILinuxManager {

    void registerProvisioner(ILinuxProvisioner provisioner);

    ILinuxImage getImageForTag(@NotNull String imageTag) throws LinuxManagerException;

}
