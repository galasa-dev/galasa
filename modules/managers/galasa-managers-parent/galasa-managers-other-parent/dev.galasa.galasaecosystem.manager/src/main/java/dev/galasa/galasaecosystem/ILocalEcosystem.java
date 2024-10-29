/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.galasaecosystem;

import dev.galasa.ipnetwork.ICommandShell;

/**
 * Kubernetes Ecosystem TPI
 * 
 * Provides access to the ecosystem endpoints and provides the mean to manipulate the ecosystem
 *  
 *  
 *
 */
public interface ILocalEcosystem extends IGenericEcosystem {
    
    
    void startSimPlatform() throws GalasaEcosystemManagerException;
    
    void stopSimPlatform() throws GalasaEcosystemManagerException;

    /**
     * retrieve the directory location to which the isolated archive was unpacked.
     * @return string
     * */
    public String getIsolatedDirectory();
    
    public ICommandShell getCommandShell() throws GalasaEcosystemManagerException;
    
}