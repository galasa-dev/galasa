/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.runner;

import java.time.Instant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.TestRunException;
import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.EventsException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IEventsService;
import dev.galasa.framework.spi.events.IEvent;
import dev.galasa.framework.spi.events.TestHeartbeatStoppedEvent;
import dev.galasa.framework.spi.events.TestRunLifecycleStatusChangedEvent;

/**
 * An implementation of something which publishes events through the OSGi registered events service.
 */
public class TestRunnerEventsProducer implements ITestRunnerEventsProducer {

    private Log logger = LogFactory.getLog(TestRunnerEventsProducer.class);
    
    private IConfigurationPropertyStoreService cps;
    private IEventsService eventsService ;
    private boolean isEnabled = false;
    
    public TestRunnerEventsProducer(IEventsService eventsService, IConfigurationPropertyStoreService cps) throws TestRunException {
        this.eventsService = eventsService ;
        this.cps = cps ;

        boolean isProduceEventsEnabled = isProduceEventsFeatureFlagTrue(cps);
        setEnabled(isProduceEventsEnabled);
    }
    private void setEnabled(boolean isProduceEventsEnabled) {
        this.isEnabled = isProduceEventsEnabled;
    }

    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }

    @Override
    public void produceTestHeartbeatStoppedEvent(String testRunName) throws TestRunException {
        if (this.isEnabled) {
            logger.debug("Producing a test heartbeat stopped event.");

            String message = String.format("Galasa test run %s's heartbeat has been stopped.",testRunName );
            TestHeartbeatStoppedEvent event = new TestHeartbeatStoppedEvent(this.cps, Instant.now().toString(), message);
            String topic = event.getTopic();

            publishEvent(topic,event);
        }
    }

    @Override
    public void produceTestRunLifecycleStatusChangedEvent(String testRunName, TestRunLifecycleStatus status) throws TestRunException {
        if (this.isEnabled) {
            try {
                logger.debug("Producing a test run lifecycle status change event.");

                String message = String.format("Galasa test run %s is now in status: %s.", testRunName , status.toString());
                TestRunLifecycleStatusChangedEvent event = new TestRunLifecycleStatusChangedEvent(this.cps, Instant.now().toString(), message);
                String topic = event.getTopic();

                publishEvent(topic,event);
            } catch (TestRunException e) {
               logger.error("Unable to produce a test run lifecycle status changed event to the Events Service", e);
            }
        }
    }

    private void publishEvent(String topic, IEvent event) throws TestRunException {
        if (topic != null) {
            try {
                this.eventsService.produceEvent(topic, event);
            } catch (EventsException e) {
                throw new TestRunException("Failed to publish a test run lifecycle status changed event to the Events Service", e);
            }
        }
    }

    private boolean isProduceEventsFeatureFlagTrue(IConfigurationPropertyStoreService cps) throws TestRunException {
        boolean produceEvents = false;
        try {
            String produceEventsProp = cps.getProperty("produce", "events");
            if (produceEventsProp != null) {
                logger.debug("CPS property framework.produce.events was found and is set to: " + produceEventsProp);
                produceEvents = Boolean.parseBoolean(produceEventsProp);
            }
        } catch (ConfigurationPropertyStoreException ex) {
            throw new TestRunException("Problem reading the CPS property to check if framework event production has been activated.",ex);
        }
        return produceEvents;
    }
}
