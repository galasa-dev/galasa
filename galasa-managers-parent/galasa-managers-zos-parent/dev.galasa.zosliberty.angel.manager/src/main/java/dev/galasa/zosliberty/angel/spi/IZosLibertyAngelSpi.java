/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosliberty.angel.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosliberty.angel.IZosLibertyAngel;
import dev.galasa.zosliberty.angel.ZosLibertyAngelManagerException;

/**
 * SPI interface to {@link IZosLibertyAngel}
 */
public interface IZosLibertyAngelSpi {

    /**
     * Returns a zOS Liberty Angel instance
     * @param zosImage zOS image
     * @param angelName Angel name. Can be null for Galasa generated name
     * @return a {@link IZosLibertyAngel} implementation instance
     * @throws ZosLibertyAngelManagerException
     */
    @NotNull
    public IZosLibertyAngel newZosLibertyAngel(@NotNull IZosImage zosImage, @NotNull String angelName) throws ZosLibertyAngelManagerException;
}
