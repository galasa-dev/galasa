/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.events.kafka.mocks;

import java.util.Properties;
import java.util.ArrayList;
import java.util.List;

import dev.galasa.framework.spi.IEventProducer;
import dev.galasa.framework.spi.events.IEvent;

public class MockEventProducer implements IEventProducer {

    public List<IEvent> events = new ArrayList<IEvent>();

    public MockEventProducer(Properties properties, String topic) {
    }

    @Override
    public void sendEvent(IEvent event) {
        events.add(event);
    }

    @Override
    public void close() {
    }

    public List<IEvent> getEvents() {
        return events;
    }
    
}
