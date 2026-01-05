/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.Environment;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.SystemEnvironment;

@Component(service = { ValidateEcosystem.class })
public class ValidateEcosystem {
    
    private static final String GALASA_VALIDATE_ENGINE_ENV_VAR = "GALASA_VALIDATE_ENGINE";

    private Log             logger  =  LogFactory.getLog(this.getClass());
    
    private IFramework framework;

    private Environment env;

    private static final Set<String> NULL_TAGS    = null;
    private static final List<String> NULL_METHODS = null;

    public ValidateEcosystem() {
        this(new SystemEnvironment());
    }

    public ValidateEcosystem(Environment env) {
        this.env = env;
    }
    
    /**
     * <p>Validate the Ecosystem will work for remote access</p>
     * 
     * @param bootstrapProperties
     * @param overrideProperties
     * @throws FrameworkException
     */
    public void setup(Properties bootstrapProperties, Properties overrideProperties) throws FrameworkException {
        
        logger.info("Initialising Validate Ecosystem Service");
        
        FrameworkInitialisation frameworkInitialisation = initialiseFramework(bootstrapProperties, overrideProperties);
        framework = frameworkInitialisation.getFramework();
        logger.info("Framework successfully initialised");
        
        if (!isSubmittingTest()) {
            logger.info("Bypassing engine test");
        } else {
            IFrameworkRuns frameworkRuns = framework.getFrameworkRuns();
            IRun testRun = submitCoreManagerTest(frameworkRuns);

            String rasRunId = testRun.getRasRunId();
            if (rasRunId != null) {
                framework.getResultArchiveStore().createTestStructure(rasRunId, testRun.toTestStructure());
            }

            Instant expire = Instant.now().plusSeconds(120);
            Instant report = Instant.now().plusSeconds(5);
            waitForTestRunToFinish(frameworkRuns, testRun, expire, report);

            logTestRunResult(frameworkRuns, testRun);
        }

        logger.info("Ending Validate Ecosystem Service");

        frameworkInitialisation.shutdownFramework();
    }

    private void logTestRunResult(IFrameworkRuns frameworkRuns, IRun testRun)
            throws DynamicStatusStoreException, FrameworkException {
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

    private void waitForTestRunToFinish(IFrameworkRuns frameworkRuns, IRun testRun, Instant expire, Instant report)
            throws FrameworkException, DynamicStatusStoreException {
        while (expire.isAfter(Instant.now())) {
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
    }

    private IRun submitCoreManagerTest(IFrameworkRuns frameworkRuns) throws FrameworkException {
        String submissionId = UUID.randomUUID().toString();
        
        IRun testRun = frameworkRuns.submitRun(null, 
                "validateeco", 
                "validateeco", 
                "dev.galasa.core.manager.ivt", 
                "dev.galasa.core.manager.ivt.CoreManagerIVT", 
                null, 
                null, 
                null, 
                null, 
                false, 
                true,
                NULL_TAGS, 
                null, 
                null, 
                null, 
                null,
                submissionId,
                NULL_METHODS);
        
        logger.info("Test CoreManagerIVT submitted as run " + testRun.getName());

        return testRun;
    }

    private boolean isSubmittingTest() {
        String sRunTest = env.getenv(GALASA_VALIDATE_ENGINE_ENV_VAR);
        if (sRunTest == null || sRunTest.trim().isEmpty()) {
            sRunTest = "true";
        }
        boolean runTest = Boolean.parseBoolean(sRunTest);
        return runTest;
    }

    private FrameworkInitialisation initialiseFramework(Properties bootstrapProperties, Properties overrideProperties) throws FrameworkException {
        FrameworkInitialisation frameworkInitialisation = null;
        try {
            frameworkInitialisation = new FrameworkInitialisation(bootstrapProperties, overrideProperties);
        } catch (Exception e) {
            throw new FrameworkException("Unable to initialise the Framework Service", e);
        }
        return frameworkInitialisation;
    }
}
