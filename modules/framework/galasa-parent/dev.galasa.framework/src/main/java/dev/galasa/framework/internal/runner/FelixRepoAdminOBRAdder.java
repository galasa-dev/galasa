/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.runner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import dev.galasa.framework.TestRunException;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.utils.ITimeService;

public class FelixRepoAdminOBRAdder {

    public static final int MAX_ADD_OBR_OPERATION_RETRIES = 10;
    public static final long DELAY_BETWEEN_ADD_OBR_OPERATION_RETRIES_MILLISECS = 3000;

    private Log logger = LogFactory.getLog(FelixRepoAdminOBRAdder.class);
    private IConfigurationPropertyStoreService cps;
    private RepositoryAdmin repoAdmin;

    private ITimeService timeService;

    public FelixRepoAdminOBRAdder(
        RepositoryAdmin repoAdmin,
        IConfigurationPropertyStoreService cps,
        ITimeService timeService
    ) {
        this.repoAdmin = repoAdmin;
        this.cps = cps;
        this.timeService = timeService;
    }

    public void addOBRsToRepoAdmin(String streamName, String runOBRList) throws TestRunException {
        String testOBR = getTestOBRFromStream(streamName);
        testOBR = getOverriddenValue(testOBR, runOBRList);
        addOBRsToRepoAdmin(testOBR, repoAdmin);
    }

    private String getTestOBRFromStream(String streamName) throws TestRunException {
        String testOBR = null ;
        if (streamName != null) {
            logger.debug("Loading test stream " + streamName);
            try {
                testOBR = this.cps.getProperty("test.stream", "obr", streamName);
            } catch (Exception e) {
                throw new TestRunException("Unable to load stream " + streamName + " settings", e);
            }
        }
        return testOBR ;
    }

    private void addOBRsToRepoAdmin(String testOBR, RepositoryAdmin repoAdmin) throws TestRunException {
        if (testOBR != null) {
            logger.debug("Loading test obr repository " + testOBR);

            String[] testOBRs = testOBR.split("\\,");
            for (String obr : testOBRs) {
                obr = obr.trim();
                if (!obr.isEmpty()) {
                    addRepositoryWithRetry(
                        obr,
                        repoAdmin,
                        MAX_ADD_OBR_OPERATION_RETRIES,
                        DELAY_BETWEEN_ADD_OBR_OPERATION_RETRIES_MILLISECS
                    );
                }
            }
        }
    }
    
    private void addRepositoryWithRetry(String obr, RepositoryAdmin repoAdmin, int maxRetries, long retryDelayMs) throws TestRunException {
        int attempt = 1;

        while (attempt <= maxRetries) {
            try {
                if (attempt > 1) {
                    logger.info("Retrying OBR load for " + obr + " (attempt " + attempt + " of " + maxRetries + ")");
                }
                repoAdmin.addRepository(obr);
                logger.info("Successfully loaded OBR " + obr);
                break;
            } catch (Exception e) {
                attempt++;
                
                if (attempt <= maxRetries) {
                    logger.warn("Failed to load OBR. Retrying in " + retryDelayMs + "ms...");
                    waitForRetryDelay(retryDelayMs);
                } else {
                    // All retries exhausted
                    throw new TestRunException("Unable to load specified OBR " + obr + " after " + maxRetries + " attempts", e);
                }
            }
        }        
    }

    private void waitForRetryDelay(long retryDelayMs) throws TestRunException {
        try {
            timeService.sleepMillis(retryDelayMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new TestRunException("Interrupted while waiting to retry an operation", ie);
        }
    }
    private String getOverriddenValue(String existingValue, String possibleOverrideValue) {
        String result = existingValue ;
        String possibleNulledValue = AbstractManager.nulled(possibleOverrideValue);
        if (possibleNulledValue != null) {
            result = possibleNulledValue;
        }
        return result ;
    }
}
