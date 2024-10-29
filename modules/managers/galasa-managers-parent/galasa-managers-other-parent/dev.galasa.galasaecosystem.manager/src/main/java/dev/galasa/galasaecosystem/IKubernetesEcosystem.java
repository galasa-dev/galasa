/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.galasaecosystem;

import com.google.gson.JsonObject;

/**
 * Kubernetes Ecosystem TPI
 * 
 * Provides access to the ecosystem endpoints and provides the mean to manipulate the ecosystem
 *  
 *  
 *
 */
public interface IKubernetesEcosystem extends IGenericEcosystem {
           
    public JsonObject getSubmittedRuns(String groupName) throws GalasaEcosystemManagerException;

    public JsonObject waitForGroupNames(String groupName, long timeout) throws GalasaEcosystemManagerException; 
    
}