/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
  * A queue of dss events to be processed.
  * 
  * Calls to this queue do not block for long, but are thread safe.
  */
public class DssEventQueue {
    private Log logger = LogFactory.getLog(getClass());
    private Queue<DssEvent> queue = new LinkedList<DssEvent>();

    public DssEvent dequeue() {
        
        logger.debug("DssEventQueue: getting dss event from queue.");
        DssEvent event = null ;
        synchronized(queue) {
            // Takes item from head, or null if queue is empty.
            event = queue.poll(); 
        }
        if (event == null) {
            logger.debug("DssEventQueue: get(): No events to process.");
        } else {
            logger.debug("DssEventQueue: get(): returning "+event.toString());
        }
        return event;
    }
    
    public void enqueue(DssEvent newDssEvent) {
        synchronized(queue) {
            queue.add(newDssEvent);
        }
        if (newDssEvent == null) {
            logger.debug("DssEventQueue: add(): No events to process.");
        } else {
            logger.debug("DssEventQueue: add(): adding event "+newDssEvent.toString());
        }
    }

    public int size() {
        return queue.size();
    }
}