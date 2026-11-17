/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import static org.assertj.core.api.Assertions.*;

import java.net.URI;
import java.nio.file.Path;
import java.util.*;

import org.junit.Test;

import dev.galasa.framework.mocks.*;
import dev.galasa.framework.mocks.MockTestRunnerEventsProducer.ProducedEvent;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreMatchException;
import dev.galasa.framework.spi.IDssAction;

public class TestBaseTestRunner {

    private static final String SERIAL_LOCK_KEY = "locks.isSerial.myBundle/my.Class";

    private static BaseTestRunner createInitialisedRunner(MockIDynamicStatusStoreService dss, boolean isSerial) throws Exception {
        return createInitialisedRunner(dss, isSerial, new MockTestRunnerEventsProducer());
    }

    private static MockIDynamicStatusStoreService createDssWhichFailsToRelease() {
        return new MockIDynamicStatusStoreService() {
            @Override
            public void performActions(IDssAction... actions)
                    throws DynamicStatusStoreException, DynamicStatusStoreMatchException {
                throw new DynamicStatusStoreException("DSS is down");
            }
        };
    }

    private static BaseTestRunner createInitialisedRunner(MockIDynamicStatusStoreService dss, boolean isSerial,
            MockTestRunnerEventsProducer eventsProducer) throws Exception {
        MockRun run = new MockRun("myBundle", "my.Class", "U1", "stream", "obr", "http://repo", "requestor", false);
        run.setSerial(isSerial);

        MockFileSystem mockFileSystem = new MockFileSystem();
        MockIResultArchiveStore ras = new MockIResultArchiveStore("myRunId", mockFileSystem);
        MockFrameworkRuns frameworkRuns = new MockFrameworkRuns("myGroup", List.of(run));
        MockShutableFramework framework = new MockShutableFramework(ras, dss, "U1", run, frameworkRuns) {
            @Override
            public Random getRandom() {
                return new Random();
            }
        };

        MockTestRunnerDataProvider dataProvider = new MockTestRunnerDataProvider();
        dataProvider.setRun(run);
        dataProvider.setFramework(framework);
        dataProvider.setCps(new MockIConfigurationPropertyStoreService());
        dataProvider.setDss(dss);
        dataProvider.setRas(ras);
        dataProvider.setOverrideProperties(new Properties());
        dataProvider.setEventsPublisher(eventsProducer);

        BaseTestRunner runner = new BaseTestRunner();
        runner.init(dataProvider);
        runner.testStructure = runner.createNewTestStructure(run);
        return runner;
    }

    @Test
    public void testSerialRunReleasesItsLockWhenItFinishes() throws Exception {
        // Given...
        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        dss.put(SERIAL_LOCK_KEY, "U1");
        BaseTestRunner runner = createInitialisedRunner(dss, true);

        // When...
        runner.updateStatus(TestRunLifecycleStatus.FINISHED, "finished");

        // Then...
        assertThat(dss.get(SERIAL_LOCK_KEY)).isNull();
    }

    @Test
    public void testSerialRunKeepsItsLockWhileItIsNotFinished() throws Exception {
        // Given...
        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        dss.put(SERIAL_LOCK_KEY, "U1");
        BaseTestRunner runner = createInitialisedRunner(dss, true);

        // When...
        runner.updateStatus(TestRunLifecycleStatus.RUNNING, null);

        // Then...
        assertThat(dss.get(SERIAL_LOCK_KEY)).isEqualTo("U1");
    }

    @Test
    public void testSerialRunReleasesItsLockWhenItIsMadeToWait() throws Exception {
        // Given...
        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        dss.put(SERIAL_LOCK_KEY, "U1");
        BaseTestRunner runner = createInitialisedRunner(dss, true);

        // When...
        runner.markWaiting(runner.getFramework());

        // Then...
        assertThat(dss.get(SERIAL_LOCK_KEY)).isNull();
        assertThat(dss.get("run.U1.status")).isEqualTo("waiting");
    }

    @Test
    public void testSerialRunDoesNotReleaseALockOwnedByAnotherRunWhenItFinishes() throws Exception {
        // Given...
        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        dss.put(SERIAL_LOCK_KEY, "U2");
        BaseTestRunner runner = createInitialisedRunner(dss, true);

        // When...
        runner.updateStatus(TestRunLifecycleStatus.FINISHED, "finished");

        // Then...
        assertThat(dss.get(SERIAL_LOCK_KEY)).isEqualTo("U2");
    }

    @Test
    public void testFailedLockReleaseDoesNotFailTheFinishedStatusUpdateOrStopItsEvent() throws Exception {
        // Given... a DSS which fails when the lock is released.
        MockIDynamicStatusStoreService dss = createDssWhichFailsToRelease();
        dss.put(SERIAL_LOCK_KEY, "U1");
        MockTestRunnerEventsProducer eventsProducer = new MockTestRunnerEventsProducer();
        BaseTestRunner runner = createInitialisedRunner(dss, true, eventsProducer);

        // When...
        runner.updateStatus(TestRunLifecycleStatus.FINISHED, "finished");

        // Then... the status is updated and the status-changed event is still produced.
        assertThat(dss.get("run.U1.status")).isEqualTo("finished");
        List<ProducedEvent> events = eventsProducer.getHistory();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).extracting("eventType","testRunLifecycleStatus")
            .contains("TestRunLifecycleStatusChangedEvent", TestRunLifecycleStatus.FINISHED);
    }

    @Test
    public void testFailedLockReleaseDoesNotFailMakingTheRunWait() throws Exception {
        // Given... a DSS which fails when the lock is released.
        MockIDynamicStatusStoreService dss = createDssWhichFailsToRelease();
        dss.put(SERIAL_LOCK_KEY, "U1");
        BaseTestRunner runner = createInitialisedRunner(dss, true);

        // When...
        runner.markWaiting(runner.getFramework());

        // Then...
        assertThat(dss.get("run.U1.status")).isEqualTo("waiting");
    }

    @Test
    public void testCanInstantiateABaseTestRunner() throws Exception {

        // When...
        BaseTestRunner runner = new BaseTestRunner();

        MockFileSystem mockFileSystem = new MockFileSystem();

        Map<String,String> propsInRas = Map.of("initial","value");
        Path rasRootPath = mockFileSystem.getPath("/root");
        MockRASStoreService mockRas = new MockRASStoreService(propsInRas, rasRootPath );

        Properties props = new Properties();
        props.put("my.prop2","world");
        props.put("my.prop1", "hello");

        runner.saveAllOverridesPassedToArtifact(props, mockFileSystem, mockRas);

        List<Path> files = mockFileSystem.getListOfAllFiles();
        assertThat(files).hasSize(1);

        List<String> lines = mockFileSystem.readLines(new URI(files.get(0).toString()));
        assertThat(lines).hasSize(4);

        assertThat(lines.get(0)).contains("as overrides to the test");
        assertThat(lines.get(2)).contains("my.prop1","hello");
        assertThat(lines.get(3)).contains("my.prop2","world");
    }
}
