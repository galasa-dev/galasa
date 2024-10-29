/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.galasaecosystem.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.DssAdd;
import dev.galasa.framework.spi.DssDelete;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreMatchException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourcePoolingService;
import dev.galasa.framework.spi.InsufficientResourcesAvailableException;
import dev.galasa.galasaecosystem.GalasaEcosystemManagerException;
import dev.galasa.galasaecosystem.internal.properties.RunIdPrefix;

public class RunIdPrefixImpl {
    
    private final static Log                         logger    = LogFactory.getLog(RunIdPrefixImpl.class);
    
    private final IFramework framework;
    private final IDynamicStatusStoreService dss;
    
    private String runIdPrefix;
    
    public RunIdPrefixImpl(IFramework framework, IDynamicStatusStoreService dss) throws GalasaEcosystemManagerException, InsufficientResourcesAvailableException {
        this.framework = framework;
        this.dss       = dss;
        allocationPrefix();
    }

    private void allocationPrefix() throws GalasaEcosystemManagerException, LogConfigurationException, InsufficientResourcesAvailableException {
        
        IResourcePoolingService poolingService = this.framework.getResourcePoolingService();
        
        // Get the prefixes to generate
        List<String> prefixPool = RunIdPrefix.get();
        
        // Generate list of free prefixes
        List<String> possibleNames = null;
        try {
            List<String> exclude = new ArrayList<>();
            possibleNames = poolingService.obtainResources(prefixPool, exclude, 1, 1, this.dss,
                    "runid.prefix");
        } catch (InsufficientResourcesAvailableException e) {
            throw new InsufficientResourcesAvailableException("Insufficient runid prefixes available");
        }
        
        if (possibleNames.isEmpty()) {
            throw new InsufficientResourcesAvailableException("Insufficient runid prefixes available");
        }
        
        String possiblePrefix = possibleNames.remove(0);
        
        String dssResourceName = "runid.prefix." + possiblePrefix;
        String runName = this.framework.getTestRunName();
        
        // Reserve prefix
        DssAdd prefixResource = new DssAdd(dssResourceName, runName);
        DssAdd runPrefix = new DssAdd("run." + runName + "." + dssResourceName, "active");
        
        try {
            this.dss.performActions(prefixResource, runPrefix);
        } catch(DynamicStatusStoreMatchException e) {
            try {
                Thread.sleep(200 + new Random().nextInt(200));
                allocationPrefix();
            } catch (InterruptedException e1) {
                Thread.currentThread().interrupt();
                throw new GalasaEcosystemManagerException("Wait interrupted", e);
            }
        } catch (DynamicStatusStoreException e) {
            throw new GalasaEcosystemManagerException("Problem with DSS", e);
        }
        
        this.runIdPrefix = possiblePrefix;
        
        logger.trace("Selected run ID prefix " + this.runIdPrefix);
    }
    
    public String getRunIdPrefix() {
        return this.runIdPrefix;
    }

    public void discard() throws GalasaEcosystemManagerException {
        deleteFromDss(this.dss, this.framework.getTestRunName(), runIdPrefix);
    }
    
    
    public static void deleteFromDss(IDynamicStatusStoreService dss, String runName, String runIdprefix) throws GalasaEcosystemManagerException {
        
        String dssResourceName = "runid.prefix." + runIdprefix;
        
        // Delete prefix
        DssDelete prefixResource = new DssDelete(dssResourceName, null);
        DssDelete runPrefix = new DssDelete("run." + runName + "." + dssResourceName, null);

        try {
            dss.performActions(prefixResource, runPrefix);
        } catch (DynamicStatusStoreException e) {
            throw new GalasaEcosystemManagerException("Problem with DSS", e);
        }       

        logger.trace("Discarded run ID prefix " + runIdprefix);
    }

}
