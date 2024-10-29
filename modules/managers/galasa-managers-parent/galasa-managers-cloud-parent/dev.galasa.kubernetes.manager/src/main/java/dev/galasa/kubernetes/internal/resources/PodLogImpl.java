/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.kubernetes.internal.resources;

import dev.galasa.kubernetes.IPodLog;

/**
 * A holder for the Pod Log
 * 
 *  
 *
 */
public class PodLogImpl implements IPodLog {
    private final String name;
    private final String log;

    public PodLogImpl(String name, String log) {
        this.name = name;
        this.log  = log;
    }

    @Override
    public String getLog() {
        return this.log;
    }
    
    public String getName() {
        return this.name;
    }

}
