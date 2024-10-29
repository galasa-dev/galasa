/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.events;

import dev.galasa.framework.TestRunException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

public class TestHeartbeatStoppedEvent extends Event {

    private final String TOPIC;

    public TestHeartbeatStoppedEvent(IConfigurationPropertyStoreService cps, String timestamp, String message) throws TestRunException {
        super(timestamp, message);
        try {
            this.TOPIC = cps.getProperty(this.getClass().getSimpleName().toLowerCase(), "name", "topic");
        } catch (ConfigurationPropertyStoreException e) {
            throw new TestRunException("There was a problem retrieving from the CPS the name of the topic to send TestHeartbeatStoppedEvents.", e);
        }
    }

    public String getTopic() {
        return this.TOPIC;
    }
    
}
