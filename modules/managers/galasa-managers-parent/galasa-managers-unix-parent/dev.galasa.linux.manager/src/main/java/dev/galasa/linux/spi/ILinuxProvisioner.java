/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.linux.spi;

import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.linux.OperatingSystem;

public interface ILinuxProvisioner {
    
    /**
     * @return the priority of the provisioner, larger number, the higher the provisioner is in the list
     */
    int getLinuxPriority();

    ILinuxProvisionedImage provisionLinux(@NotNull String tag, @NotNull OperatingSystem operatingSystem,
            @NotNull List<String> capabilities) throws ManagerException, ResourceUnavailableException;

}
