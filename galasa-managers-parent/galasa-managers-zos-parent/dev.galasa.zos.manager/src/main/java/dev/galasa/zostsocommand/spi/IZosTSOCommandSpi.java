/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zostsocommand.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.zos.IZosImage;
import dev.galasa.zostsocommand.IZosTSOCommand;
import dev.galasa.zostsocommand.ZosTSOCommandManagerException;

/**
 * SPI interface to {@link IZosTSOCommand}
 */
public interface IZosTSOCommandSpi {

    /**
     * Returns a zOS TSO Command instance
     * @param image zOS Image
     * @return an {@link IZosTSOCommand} implementation instance
     */
    public IZosTSOCommand getZosTSOCommand(@NotNull IZosImage image) throws ZosTSOCommandManagerException;
}
