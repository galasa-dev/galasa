/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.util.ArrayList;
import java.util.List;

import dev.galasa.framework.TestRunException;
import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.internal.runner.ITestRunnerEventsProducer;

public class MockTestRunnerEventsProducer implements ITestRunnerEventsProducer {

    /**
     * An event which has been produced by the events producer.
     * 
     * Retained so we can check the history of activity in unit tests later.
     */
    public static class ProducedEvent {
        String testRunName ;
        String eventType ;
        TestRunLifecycleStatus testRunLifecycleStatus ;

        public ProducedEvent(String testRunName, String eventType) {
            this(testRunName, eventType, null);
        }
        public ProducedEvent(String testRunName, String eventType, TestRunLifecycleStatus status) {
            this.testRunName = testRunName ;
            this.eventType = eventType;
            this.testRunLifecycleStatus = status ;
        }
    }

    private boolean isEnabled = false;

    private List<ProducedEvent> history = new ArrayList<>();


    public List<ProducedEvent> getHistory() {
        return this.history;
    }

    public void setEnabled(boolean isProduceEventsEnabled) {
        isEnabled = isProduceEventsEnabled;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void produceTestHeartbeatStoppedEvent(String testRunName) throws TestRunException {
        ProducedEvent event = new ProducedEvent(testRunName,"TestHeartbeatStoppedEvent");
        history.add(event);
    }

    @Override
    public void produceTestRunLifecycleStatusChangedEvent(String testRunName, TestRunLifecycleStatus status)
            throws TestRunException {
        ProducedEvent event = new ProducedEvent(testRunName,"TestRunLifecycleStatusChangedEvent", status);
        history.add(event);
    }
    
}
