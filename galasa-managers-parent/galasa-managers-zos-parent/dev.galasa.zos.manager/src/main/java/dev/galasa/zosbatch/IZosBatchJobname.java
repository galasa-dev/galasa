/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch;

/**
 * <p>Represents a privovision zOS Batch Jobname</p>
 * 
 * <p>Use a {@link ZosBatchJobname} annotation to populate this field with</p>
 * 
 *  
 *
 */
public interface IZosBatchJobname {
 
    /**
     * Get the name of the zOS batch Jobname
     * @return String
     */
    public String getName();
}
