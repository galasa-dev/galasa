/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import dev.galasa.framework.spi.events.IEvent;

public interface IEventsService {

    void produceEvent(String topic, IEvent event) throws EventsException;

    void shutdown();
    
}

