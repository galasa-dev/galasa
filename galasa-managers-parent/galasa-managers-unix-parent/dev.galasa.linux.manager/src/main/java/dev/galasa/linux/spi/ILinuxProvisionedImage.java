/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.linux.spi;

import dev.galasa.linux.ILinuxImage;

public interface ILinuxProvisionedImage extends ILinuxImage {
    
    void discard();

}
