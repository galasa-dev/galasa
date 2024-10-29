/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosprogram;

import java.lang.reflect.Field;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosfile.IZosDataset;
import dev.galasa.zosprogram.ZosProgram.Language;

/** 
 * Represents a zOS Program
 */
public interface IZosProgram {
    
    /**
     * Get the Galasa test field associated with this zOS Program
     * @return the test field
     */
    Field getField();

    /**
     * Get the zOS Program name
     * @return the program name
     */
    public String getName();
    
    /**
     * Get the zOS Program language
     * @return the program language
     */
    public Language getLanguage();
    
    /**
     * Is the zOS Program a CICS program
     * @return true if is CICS
     */
    public boolean isCics();
    
    /**
     * Get the zOS Program load library
     * @return the program load library
     */
    public IZosDataset getLoadlib();
    
    /**
     * Get the zOS image associated with the zOS Program
     * @return the zOS image
     */
    public IZosImage getImage();

    /**
     * Get the program source
     * @return the program source
     * @throws ZosProgramException 
     */
    public String getProgramSource() throws ZosProgramException;

    /**
     * Compile and link the zOS Program. Only applicable when {@link ZosProgram#compile()} annotation element is set to {@code false} 
     * @return
     * @throws ZosProgramManagerException
     */
    public IZosProgram compile() throws ZosProgramManagerException;

    /**
     * Return the compile zOS Batch Job 
     * @return the batch job
     */
    public IZosBatchJob getCompileJob();
}
