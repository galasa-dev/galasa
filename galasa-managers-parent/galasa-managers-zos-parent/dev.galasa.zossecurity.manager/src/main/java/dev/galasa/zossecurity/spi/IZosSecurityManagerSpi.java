/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.zos.IZosImage;
import dev.galasa.zossecurity.IZosSecurity;
import dev.galasa.zossecurity.ZosSecurityManagerException;

/**
 * SPI interface to {@link IZosSecurity}
 */
public interface IZosSecurityManagerSpi {

    /**
     * Returns a zOS Security instance
     * @param image the zOS Image
     * @return a {@link IZosSecurity} implementation instance
     * @throws ZosSecurityManagerException
     */
    @NotNull
    public IZosSecurity getZosSecurity(IZosImage image) throws ZosSecurityManagerException;
}
