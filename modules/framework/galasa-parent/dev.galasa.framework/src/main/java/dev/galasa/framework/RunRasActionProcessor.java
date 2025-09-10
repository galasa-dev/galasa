/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunRasActionProcessor;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.RunRasAction;
import dev.galasa.framework.spi.teststructure.TestStructure;

/**
 * Handles the processing of RAS actions for runs in the DSS by performing the relevant
 * updates to run test structures in the RAS
 */
public class RunRasActionProcessor implements IRunRasActionProcessor {

    private final Log logger = LogFactory.getLog(getClass());

    private IResultArchiveStore rasStore;

    public RunRasActionProcessor(IResultArchiveStore rasStore) {
        this.rasStore = rasStore;
    }

    public void processRasActions(String runName, List<RunRasAction> rasActions) {
        logger.info("Processing RAS actions for run '" + runName + "'");

        for (RunRasAction rasAction : rasActions) {
            try {
                String runId = rasAction.getRunId();
                if (runId != null) {
                    TestStructure testStructure = getRunTestStructure(runId);
                    if (testStructure != null) {
        
                        // Set the status and result for the run if it doesn't already have the desired status
                        String runStatus = testStructure.getStatus();
                        String desiredRunStatus = rasAction.getDesiredRunStatus();
                        if (!desiredRunStatus.equals(runStatus)) {
                            String desiredRunResult = rasAction.getDesiredRunResult();

                            logger.info(runName + ": Updating run status in RAS from " + runStatus + " to " + desiredRunStatus);
                            logger.info(runName + ": Setting result to " + desiredRunResult);

                            testStructure.setStatus(desiredRunStatus);
                            testStructure.setResult(desiredRunResult);
        
                            rasStore.updateTestStructure(runId, testStructure);
                            logger.info("Successfully updated RAS record for run " + runName);
                        } else {
                            logger.info("Run already has status '" + desiredRunStatus + "', will not update its RAS record");
                        }
                    } else {
                        logger.info("No RAS test structure found for run " + runName + ", skipping RAS actions for this run");
                    }
                } else {
                    logger.info("No RAS record ID found for run " + runName + ", skipping RAS actions for this run");
                }
            } catch (ResultArchiveStoreException ex) {
                logger.error("Failed to process RAS action", ex);
            }
        }
        logger.info("Finished processing RAS actions for run '" + runName + "'");
    }

    private TestStructure getRunTestStructure(String runId) throws ResultArchiveStoreException {
        TestStructure testStructure = null;
        for (IResultArchiveStoreDirectoryService directoryService : this.rasStore.getDirectoryServices()) {
            IRunResult run = directoryService.getRunById(runId);

            if (run != null) {
                testStructure = run.getTestStructure();
                break;
            }
        }
        return testStructure;
    }
}
