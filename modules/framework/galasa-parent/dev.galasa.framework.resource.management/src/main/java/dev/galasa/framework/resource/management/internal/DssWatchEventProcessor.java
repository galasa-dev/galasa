/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import java.util.Queue;

import dev.galasa.framework.spi.IDynamicStatusStoreWatcher.Event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A runnable thread which processes events coming out of DSS.
 * 
 * It can be scheduled to fire regularly to check the queue of events coming from DSS.
 * 
 * It reads all the events in the dss queue and processes them all before returning.
 */
class DssWatchEventProcessor implements Runnable {

    private Log logger = LogFactory.getLog(getClass());

    private final Queue<DssEvent> queue;
    private final ResourceManagementProviders resourceManagementProviders;

    public DssWatchEventProcessor(
        Queue<DssEvent> queue, 
        ResourceManagementProviders resourceManagementProviders    
    ) {
        this.queue = queue;
        this.resourceManagementProviders = resourceManagementProviders;
    }

    /**
     * Called every few seconds on a fixed schedule, so we can look for any dss events we want to process, 
     * and process them on this thread.
     */
    @Override
    public void run() {

        try {
            boolean isDone = false;

            logger.debug("starting a sweep of DSS events to process");
            while(!isDone) {

                DssEvent dssEvent = queue.poll();
                if (dssEvent == null) {
                    isDone = true;
                } else {

                    Event event = dssEvent.getEventType();
                    String runName = dssEvent.getRunName();
                    String newValue = dssEvent.getNewValue();

                    if (event == Event.DELETE) {
                        logger.debug("Detected deleted run " + runName);
                        this.resourceManagementProviders.runFinishedOrDeleted(runName);
                    } else {
            
                        if ("finished".equalsIgnoreCase(newValue)) {
                            logger.debug("Detected finished run " + runName);
                            this.resourceManagementProviders.runFinishedOrDeleted(runName);
                        }
                    }
                }
            }
            logger.debug("completed a sweep of DSS events to process");
        } catch( Exception ex ) {
            logger.warn("Exception caught and ignored in WatchEventProcessor: "+ex);
        }
    }


}