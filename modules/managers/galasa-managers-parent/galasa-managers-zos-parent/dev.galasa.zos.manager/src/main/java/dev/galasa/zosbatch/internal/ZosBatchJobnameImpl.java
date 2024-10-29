/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch.internal;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.RandomStringUtils;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosbatch.IZosBatchJobname;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.zosbatch.internal.properties.JobnamePrefix;

/**
 * Implementation of {@link IZosBatchJobname} 
 *
 */
public class ZosBatchJobnameImpl implements IZosBatchJobname {
    
    private String name;

    private String jobNamePrefix;
        
    public ZosBatchJobnameImpl(@NotNull IZosImage image) throws ZosBatchException {

        try {
            jobNamePrefix = JobnamePrefix.get(image.getImageID());
        } catch (ZosBatchManagerException e) {
            throw new ZosBatchException("Problem getting batch jobname prefix", e);
        }
        this.name = jobNamePrefix + RandomStringUtils.randomAlphanumeric(8-jobNamePrefix.length()).toUpperCase();
    }
    
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }
    
    public ZosBatchJobnameImpl(String name) {
        this.name = name.toUpperCase();
    }
}
