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
                TestStructure testStructure = getRunTestStructure(runId);
                if (testStructure != null) {
    
                    // Set the status and result for the run if it doesn't already have the desired status
                    String runStatus = testStructure.getStatus();
                    String desiredRunStatus = rasAction.getDesiredRunStatus();
                    if (!desiredRunStatus.equals(runStatus)) {
                        testStructure.setStatus(desiredRunStatus);
                        testStructure.setResult(rasAction.getDesiredRunResult());
    
                        rasStore.updateTestStructure(runId, testStructure);
                    } else {
                        logger.info("Run already has status '" + desiredRunStatus + "', will not update its RAS record");
                    }
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
