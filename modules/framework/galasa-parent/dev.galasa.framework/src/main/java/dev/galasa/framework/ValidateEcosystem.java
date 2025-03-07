/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import java.time.Instant;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IRun;

@Component(service = { ValidateEcosystem.class })
public class ValidateEcosystem {
    
    private Log             logger  =  LogFactory.getLog(this.getClass());
    
    private IFramework framework;
    
    /**
     * <p>Validate the Ecosystem will work for remote access</p>
     * 
     * @param bootstrapProperties
     * @param overrideProperties
     * @throws FrameworkException
     */
    public void setup(Properties bootstrapProperties, Properties overrideProperties) throws FrameworkException {
        
        logger.info("Initialising Validate Ecosystem Service");
        
        FrameworkInitialisation frameworkInitialisation = null;
        try {
            frameworkInitialisation = new FrameworkInitialisation(bootstrapProperties, overrideProperties);
        } catch (Exception e) {
            throw new FrameworkException("Unable to initialise the Framework Service", e);
        }
        
        framework = frameworkInitialisation.getFramework();
        
        logger.info("Framework successfully initialised");
        
        String sRunTest = System.getenv("GALASA_VALIDATE_ENGINE");
        if (sRunTest == null || sRunTest.trim().isEmpty()) {
            sRunTest = "true";
        }
        boolean runTest = Boolean.parseBoolean(sRunTest);
        
        if (!runTest) {
            logger.info("Bypassing engine test");
        } else {
            IFrameworkRuns frameworkRuns = framework.getFrameworkRuns();
            
            IRun testRun = frameworkRuns.submitRun(null, 
                    "validateeco", 
                    "dev.galasa.core.manager.ivt", 
                    "dev.galasa.core.manager.ivt.CoreManagerIVT", 
                    null, 
                    null, 
                    null, 
                    null, 
                    false, 
                    true, 
                    null, 
                    null, 
                    null, 
                    null,
                    UUID.randomUUID().toString());
            
            logger.info("Test CoreManagerIVT submitted as run " + testRun.getName());
            
            Instant expire = Instant.now().plusSeconds(120);
            Instant report = Instant.now().plusSeconds(5);
            while(expire.isAfter(Instant.now())) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new FrameworkException("Wait for test run interrupted",e);
                }

                IRun pollRun = frameworkRuns.getRun(testRun.getName());
                
                String status = pollRun.getStatus();
                if (status.equals("finished")) {
                    break;
                }
                
                if (report.isBefore(Instant.now())) {
                    logger.info("Test CoreManagerIVT (" + pollRun.getName() + ") has not yet finished");
                    report = Instant.now().plusSeconds(5);
                }
            }
            
            IRun pollRun = frameworkRuns.getRun(testRun.getName());
            String status = pollRun.getStatus();
            if (!status.equals("finished")) {
                logger.error("Test CoreManagerIVT (" + pollRun.getName() + ") did not finish in time, actual status = " + status);
                throw new FrameworkException("Validation failed");
            } else {
                logger.info("Test CoreManagerIVT (" + pollRun.getName() + ") has finished");
            }
            String result = pollRun.getResult();
            if (!result.equals("Passed")) {
                logger.error("Test CoreManagerIVT (" + pollRun.getName() + ") did not pass, actual result = " + result);
                throw new FrameworkException("Validation failed");
            } else {
                logger.info("Test CoreManagerIVT (" + pollRun.getName() + ") has passed");
            }
        }
                
        logger.info("Ending Validate Ecosystem Service");
        
        frameworkInitialisation.shutdownFramework();
        
    }
    
}
