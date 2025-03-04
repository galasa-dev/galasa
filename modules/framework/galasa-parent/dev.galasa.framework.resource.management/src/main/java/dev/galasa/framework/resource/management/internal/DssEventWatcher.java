/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import java.util.Queue;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreWatcher;

/**
 * Something which watches the DSS for events which occur.
 * 
 * Any events which occur are added to an event queue to be processed later.
 * 
 * This watcher must not block for long, as it needs to get execution back to etcd.
 */
class DssEventWatcher implements IDynamicStatusStoreWatcher {

    // The key we get from the DSS is something like this:
    // run.U4657.status
    private final Pattern runTestPattern = Pattern.compile("^run[.](\\w+)[.]status$");
    private final Queue<DssEvent> eventQueue ;
    private UUID watchID;
    private final IDynamicStatusStoreService dss;

    public DssEventWatcher(Queue<DssEvent> eventQueue, IDynamicStatusStoreService dss) {
        this.eventQueue = eventQueue;
        this.dss = dss;
    }

    public void startWatching() throws DynamicStatusStoreException {
        this.watchID = this.dss.watchPrefix(this, "run");
    }

    public void stopWatching() throws DynamicStatusStoreException {
        this.dss.unwatch(this.watchID);
    }

    /**
     * The DSS is telling us that something has changed we were watching.
     * 
     * We must note that this happened and return as soon as possible to 
     * stop this thread from blocking.
     * 
     * So we queue the event for later processing only, not actually 
     * processing the event further on this thread.
     */
    @Override
    public void propertyModified(String key, Event event, String oldValue, String newValue) {

        if (event != null && key != null) {

            Matcher matcher = runTestPattern.matcher(key);
            if (matcher.find()) {
                // Queue up an event to be processed the next time the queue processor gets a look-in
                // on it's regular schedule.
                String runName = matcher.group(1);
                DssEvent dssEvent = new DssEvent(event, runName, oldValue, newValue);
                eventQueue.add(dssEvent);
            }
        }
    }
}