/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.events;

import dev.galasa.framework.TestRunException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

public class TestRunLifecycleStatusChangedEvent extends Event {

    private static String topic = null;

    public TestRunLifecycleStatusChangedEvent(IConfigurationPropertyStoreService cps, String timestamp, String message) throws TestRunException {
        super(timestamp, message);
        try {
            // Each test run could produce multiple (9) instances of this event, so the topic can be queried once and cached.
            synchronized(this.getClass()) {
                if (TestRunLifecycleStatusChangedEvent.topic == null) {
                    TestRunLifecycleStatusChangedEvent.topic = cps.getProperty(this.getClass().getSimpleName().toLowerCase(), "name", "topic");
                }
            }
        } catch (ConfigurationPropertyStoreException e) {
            throw new TestRunException("There was a problem retrieving from the CPS the name of the topic to send TestRunLifecycleStatusChangedEvents.", e);
        }
    }

    public String getTopic() {
        return TestRunLifecycleStatusChangedEvent.topic;
    }
    
}
