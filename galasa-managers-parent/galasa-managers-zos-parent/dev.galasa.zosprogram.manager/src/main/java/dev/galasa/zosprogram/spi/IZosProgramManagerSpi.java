/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosprogram.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosprogram.IZosProgram;
import dev.galasa.zosprogram.ZosProgram;
import dev.galasa.zosprogram.ZosProgram.Language;
import dev.galasa.zosprogram.ZosProgramManagerException;

/**
 * Provides the SPI access to the zOS Program Manager
 *
 */
public interface IZosProgramManagerSpi {
    /**
     * Returns a new zOS Program
     * @param image The zOS Image
     * @param name The program name
     * @param source The program source in the bundle
     * @param language The programming language. See {@link ZosProgram.Language}
     * @param cics Is a CICS program.
     * @param loadlib The load module data set name
     * @return The zOS Program
     * @throws ZosProgramManagerException
     */
    public IZosProgram newZosProgram(@NotNull IZosImage image, @NotNull String name, @NotNull String source, @NotNull Language language, boolean cics, String loadlib) throws ZosProgramManagerException;
}
