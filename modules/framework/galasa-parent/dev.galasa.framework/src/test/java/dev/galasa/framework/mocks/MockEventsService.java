/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import dev.galasa.framework.spi.IEventsService;
import dev.galasa.framework.spi.events.IEvent;

public class MockEventsService implements IEventsService {

    @Override
    public void produceEvent(String topic, IEvent event) {
        throw new UnsupportedOperationException("Unimplemented method 'produceEvent'");
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("Unimplemented method 'shutdown'");
    }
    
}
