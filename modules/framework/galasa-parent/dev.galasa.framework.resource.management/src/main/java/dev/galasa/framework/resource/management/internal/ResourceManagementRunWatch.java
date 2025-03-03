/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagementProvider;

public class ResourceManagementRunWatch  {

    private final Log logger = LogFactory.getLog(this.getClass());
    private DssEventWatcher watcher;

    protected ResourceManagementRunWatch(
        IFramework framework,
        ArrayList<IResourceManagementProvider> resourceManagementProviders,
        ScheduledExecutorService scheduledExecutorService
    ) throws FrameworkException {

        logger.debug("ResourceManagementRunWatch: entered.");
        BlockingQueue<DssEvent> eventQueue = new LinkedBlockingQueue<DssEvent>();
        DssWatchEventProcessor processor = new DssWatchEventProcessor(eventQueue, resourceManagementProviders);
    
        scheduledExecutorService.scheduleWithFixedDelay(processor, 
				framework.getRandom().nextInt(20),
				10, 
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
