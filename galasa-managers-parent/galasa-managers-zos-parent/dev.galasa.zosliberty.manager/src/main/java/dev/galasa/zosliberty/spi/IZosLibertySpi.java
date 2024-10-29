/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosliberty.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.zosliberty.IZosLiberty;
import dev.galasa.zosliberty.ZosLibertyManagerException;

/**
 * SPI interface to {@link IZosLiberty}
 */
public interface IZosLibertySpi {

    /**
     * Returns a zOS Liberty instance
     * @return a {@link IZosLiberty} implementation instance
     * @throws ZosLibertyManagerException
     */
    @NotNull
    public IZosLiberty getZosLiberty() throws ZosLibertyManagerException;
}
