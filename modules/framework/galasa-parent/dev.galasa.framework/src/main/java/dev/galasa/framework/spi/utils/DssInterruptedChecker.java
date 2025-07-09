/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.utils; 

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.BaseTestRunner;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;

/**
 * Checks whether the test run has been interrupted or not.
 */
public class DssInterruptedChecker {
    
    private IDynamicStatusStoreService dss ;

    private Log logger = LogFactory.getLog(BaseTestRunner.class);

    public DssInterruptedChecker(IDynamicStatusStoreService dss) {
        this.dss = dss;
    }

    /**
     * Checks to see if the test run pod is being interrupted (with a cancel from the user for example).
     * @return true if the test pod is being interrupted, false otherwise.
     */
    public boolean isTestRunInterrupted(String runName) {
        boolean isInterrupted = false ;

        String interruptedReasonKey = "run." + runName + ".interruptReason";
        try {
            String interruptedReason = dss.get(interruptedReasonKey);
            if (interruptedReason != null) {
                logger.info("This pod has noticed that it is being interrupted. "+interruptedReason);
                isInterrupted = true;
            }
        } catch (DynamicStatusStoreException e) {
            logger.info("Could not get dss property "+interruptedReasonKey+" value. Assuming this pod has not been interrupted. ",e);
        }

        return isInterrupted; 
    }

}
