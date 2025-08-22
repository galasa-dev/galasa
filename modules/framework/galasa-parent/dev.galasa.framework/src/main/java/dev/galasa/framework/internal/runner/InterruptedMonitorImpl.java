/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.runner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.TestRunException;
import dev.galasa.framework.spi.DssPropertyKeyRunNameSuffix;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;

/**
 * An implementation of an InterruptedMonitor which checks the dss key
 * every time the isInterrupted() call is made.
 */
public class InterruptedMonitorImpl implements InterruptedMonitor {
    private Log logger = LogFactory.getLog(InterruptedMonitorImpl.class);
    private final IDynamicStatusStoreService dss;
    private final String testRunName;

    /**
     * @param dss - The dynamic state of the system.
     * @param testRunName - A String, for example U12345. We will look up dss properties based on this name.
     */
    public InterruptedMonitorImpl(IDynamicStatusStoreService dss, String testRunName) {
        this.dss = dss ;
        this.testRunName = testRunName;
    }

    @Override
    public boolean isInterrupted() throws TestRunException {

        boolean isInterrupted ;

        // The DSS has two properties set. 
        // "run."+testRunName+".interruptReason" - The reason for the test being interrupted.
        // "run."+testRunName+".rasActions" - A list of things which need to happen to clean up the RAS, and move this test to the correct state.
        String dssKey = "run."+testRunName+"."+DssPropertyKeyRunNameSuffix.INTERRUPT_REASON;
        
        try {
            String interruptedReason = dss.get(dssKey);

            if ( interruptedReason==null || interruptedReason.isBlank() ) {
                isInterrupted = false ;
                logger.info("Run "+testRunName+" has not been interrupted.");
            } else {
                isInterrupted = true ;
                logger.info("Run "+testRunName+" has been interrupted. Reason:"+interruptedReason);

                logWhyTestInterupted(testRunName);
            }
        } catch( DynamicStatusStoreException ex) {
            throw new TestRunException("Could not find out if test run "+testRunName+" is interrupted or not.");
        }
        return isInterrupted ;
    }

    private void logWhyTestInterupted(String testRunName) {
        String dssKey = "run."+testRunName+"."+DssPropertyKeyRunNameSuffix.INTERRUPTED_AT;
        try {
            String interruptedAt = dss.get(dssKey);

            if ( interruptedAt==null || interruptedAt.isBlank() ) {
                logger.info("Run "+testRunName+" was interrupted, but we don't know when it was interrupted.");
            } else {
                logger.info("Run "+testRunName+" was interrupted at "+interruptedAt);
            }
        } catch( Exception ex) {
            logger.info("Something went wrong trying to log why this test run was interrupted.",ex);
            // Suppress the exception, as we are only trying to log extra detail for diagnostics reasons.
        }
    }
    
}
