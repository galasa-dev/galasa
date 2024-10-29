/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosfile.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosfile.ZosFileManagerException;


public interface IZosFileSpi {

    /**
     * Returns a zOS File Handler instance
     * @return an {@link dev.galasa.zosfile.IZosFileHandler} implementation instance
     */
    @NotNull
    public IZosFileHandler getZosFileHandler() throws ZosFileManagerException;
}
