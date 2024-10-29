/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.galasaecosystem.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.logging.Log;
import dev.galasa.Test;
import dev.galasa.core.manager.Logger;
import dev.galasa.galasaecosystem.EcosystemEndpoint;
import dev.galasa.galasaecosystem.GalasaEcosystemManagerException;
import dev.galasa.galasaecosystem.IKubernetesEcosystem;
import dev.galasa.galasaecosystem.KubernetesEcosystem;
import dev.galasa.kubernetes.IKubernetesNamespace;
import dev.galasa.kubernetes.KubernetesNamespace;

@Test
public class EcosystemTest {
    
    @Logger
    public Log logger;
    
    @KubernetesEcosystem
    public IKubernetesEcosystem ecosystem;
    
    @KubernetesNamespace
    public IKubernetesNamespace namespace;
    
    
    @Test
    public void checkProvisioning() throws GalasaEcosystemManagerException {
        assertThat(this.ecosystem).as("Check the ecosystem is provisioned").isNotNull();
        logger.info("Ecosystem has been provisioned");
    }
    
    
    @Test
    public void checkCpsProperties() throws GalasaEcosystemManagerException {
        
        // Set the property
        ecosystem.setCpsProperty("bob", "hello");

        //Check it comes back
        assertThat(ecosystem.getCpsProperty("bob")).as("Property retrieve worked").isEqualTo("hello");
        
        // Delete the property
        ecosystem.setCpsProperty("bob", null);

        // Check it has gone
        assertThat(ecosystem.getCpsProperty("bob")).as("Property delete worked").isNull();
        logger.info("CPS is working");
    }
    
    @Test
    public void checkAllEndpointsPopulated() throws GalasaEcosystemManagerException {
        for(EcosystemEndpoint endpoint : EcosystemEndpoint.values()) {
            assertThat(ecosystem.getEndpoint(endpoint)).as("Endpoint " + endpoint + " is populated").isNotNull();
        }
        
        logger.info("All endpoints are populated");
    }
    

}
