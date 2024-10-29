/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.windows.internal;

import java.util.List;

import dev.galasa.windows.spi.IWindowsProvisionedImage;
import dev.galasa.windows.spi.IWindowsProvisioner;

public class WindowsDefaultProvisioner implements IWindowsProvisioner {

    @Override
    public IWindowsProvisionedImage provisionWindows(String tag, List<String> capabilities) {

        // *** Boo

        return null;
    }

}
