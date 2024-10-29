/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosbatch.IZosBatch;

/**
 * SPI interface to {@link IZosBatch}
 */
public interface IZosBatchSpi {

    /**
     * Returns a zOS Batch instance
     * @param image zOS Image
     * @return an {@link IZosBatch} implementation instance
     */
    @NotNull
    public IZosBatch getZosBatch(@NotNull IZosImage image);
}
