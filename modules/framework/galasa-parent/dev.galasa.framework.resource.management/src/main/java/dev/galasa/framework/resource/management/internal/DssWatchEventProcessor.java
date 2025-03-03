/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

import dev.galasa.framework.spi.IResourceManagementProvider;
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

    private final BlockingQueue<DssEvent> queue;
    private final ArrayList<IResourceManagementProvider> resourceManagementProviders;

    public DssWatchEventProcessor(BlockingQueue<DssEvent> queue, ArrayList<IResourceManagementProvider> resourceManagementProviders) {
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

                // queue.take() blocks momentarily.
                DssEvent dssEvent = queue.take();
                if (dssEvent == null) {
                    isDone = true;
                } else {

                    Event event = dssEvent.getEventType();
                    String runName = dssEvent.getRunName();
                    String newValue = dssEvent.getNewValue();

                    if (event == Event.DELETE) {
                        logger.debug("Detected deleted run " + runName);
                        this.runFinishedOrDeleted(runName, this.resourceManagementProviders);
                    } else {
            
                        if ("finished".equalsIgnoreCase(newValue)) {
                            logger.debug("Detected finished run " + runName);
                            this.runFinishedOrDeleted(runName, this.resourceManagementProviders);
                        }
                    }
                }
            }
            logger.debug("completed a sweep of DSS events to process");
        } catch( Exception ex ) {
            logger.warn("Exception caught and ignored in WatchEventProcessor: "+ex);
        }
    }

    private void runFinishedOrDeleted(String runName, ArrayList<IResourceManagementProvider> resourceManagementProviders) {
        logger.debug("runFinishedOrDeleted() entered");
        for (IResourceManagementProvider provider : resourceManagementProviders) {
            logger.debug("About to call runFinishedOrDeleted() for provider "+provider.getClass().getCanonicalName());
            provider.runFinishedOrDeleted(runName);
            logger.debug("Returned from call runFinishedOrDeleted() for provider "+provider.getClass().getCanonicalName());
        }
        logger.debug("runFinishedOrDeleted() exiting");
    }
}