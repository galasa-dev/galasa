/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;

public class ResourceManagementRunWatch  {

    private static final int RESOURCE_MANAGEMENT_RUN_WATCH_POLL_INTERVAL_SECONDS = 1;

    private final Log logger = LogFactory.getLog(this.getClass());
    private DssEventWatcher watcher;

    protected ResourceManagementRunWatch(
        IFramework framework,
        ResourceManagementProviders resourceManagementProviders,
        ScheduledExecutorService scheduledExecutorService
    ) throws FrameworkException {

        logger.debug("ResourceManagementRunWatch: entered.");
        // Create a thread-safe queue. Multiple threads can produce/consume onto this queue without blocking
        // beyond a synchronize lock around the queue itself.
        Queue<DssEvent> eventQueue = new LinkedBlockingQueue<DssEvent>();
        DssWatchEventProcessor processor = new DssWatchEventProcessor(eventQueue, resourceManagementProviders);
    
        scheduledExecutorService.scheduleWithFixedDelay(processor, 
				framework.getRandom().nextInt(20),
				RESOURCE_MANAGEMENT_RUN_WATCH_POLL_INTERVAL_SECONDS,
				TimeUnit.SECONDS);

        IDynamicStatusStoreService dss = framework.getDynamicStatusStoreService("framework");

        DssEventWatcher watcher = new DssEventWatcher(eventQueue, dss);
        
        watcher.startWatching();
        logger.debug("ResourceManagementRunWatch: exiting.");
    }

    public void shutdown() {
        logger.debug("ResourceManagementRunWatch: shutdown() entered.");
        try {
            this.watcher.stopWatching();
        } catch (DynamicStatusStoreException e) {
        }
        logger.debug("ResourceManagementRunWatch: shutdown() exiting.");
    }

}
