/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.events;

import dev.galasa.framework.spi.events.IEvent;
import dev.galasa.framework.spi.IEventsService;

public class NullEventsService implements IEventsService {

    // The Events Service has no implementation for local runs...

    @Override
    public void produceEvent(String topic, IEvent event) {
    }

    @Override
    public void shutdown(){
        // Nothing to shut down
    }
    
}

