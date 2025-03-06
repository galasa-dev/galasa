/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.IDynamicStatusStoreWatcher.Event;

/**
 * Describes an event which was spotted by the dss, and we want to process on a different thread.
 */
public class DssEvent {

    private Log logger = LogFactory.getLog(getClass());

    private final Event eventType;
    private final String runName;
    private final String newValue;
    private final String oldValue;
    
    public DssEvent(Event eventType, String runName, String oldValue , String newValue) {
        this.eventType = eventType ;
        this.runName = runName ;
        this.newValue = newValue;
        this.oldValue = oldValue ;

        logger.debug("Created: "+this.toString());
    }


    @Override
    public String toString() {
        return "Dss event: type:"+eventType+" runName:"+runName+" oldValue:"+oldValue+" newValue:"+newValue;
    }

    public String getRunName() {
        return this.runName;
    }
    public Event getEventType() {
        return this.eventType;
    }

    public String getNewValue() {
        return this.newValue;
    }

    public String getOldValue() {
        return this.oldValue;
    }
}