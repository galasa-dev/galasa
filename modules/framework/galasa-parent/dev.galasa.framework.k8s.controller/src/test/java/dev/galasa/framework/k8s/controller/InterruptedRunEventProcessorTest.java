/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Test;

import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.mocks.MockFrameworkRuns;
import dev.galasa.framework.mocks.MockRun;
import dev.galasa.framework.spi.IRun;

public class InterruptedRunEventProcessorTest {

    private MockRun createMockRun(String runName, String status, String interruptReason) {
        // We only care about the run's name, status, and interrupt reason
        MockRun mockRun = new MockRun(
            "bundle",
            "testclass",
            runName,
            "testStream",
            "testStreamOBR",
            "testStreamMavenRepo",
            "requestor",
            false
        );

        mockRun.setInterruptReason(interruptReason);
        mockRun.setStatus(status);
        return mockRun;
    }

    @Test
    public void testEventProcessorMarksRunFinishedOk() throws Exception {
        // Given...
        String runName = "RUN1";
        String status = "running";
        String interruptReason = "cancelled";

        MockRun mockRun = createMockRun(runName, status, interruptReason);
        List<IRun> mockRuns = List.of(mockRun);
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(mockRuns);

        Queue<RunInterruptEvent> eventQueue = new LinkedBlockingQueue<>();
        RunInterruptEvent interruptEvent = new RunInterruptEvent(runName, interruptReason, null);
        eventQueue.add(interruptEvent);

        InterruptedRunEventProcessor processor = new InterruptedRunEventProcessor(eventQueue, mockFrameworkRuns);

        // When...
        processor.run();

        // Then...
        assertThat(eventQueue).isEmpty();
        assertThat(mockRun.getStatus()).isEqualTo(TestRunLifecycleStatus.FINISHED.toString());
        assertThat(mockRun.getResult()).isEqualTo(interruptReason);
    }
    
}
