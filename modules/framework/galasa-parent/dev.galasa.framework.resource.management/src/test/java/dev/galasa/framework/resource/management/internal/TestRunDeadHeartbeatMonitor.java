/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import java.time.*;
import java.time.temporal.*;

import org.junit.Test;

import dev.galasa.framework.mocks.*;

import dev.galasa.framework.resource.management.internal.mocks.*;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IRun;


public class TestRunDeadHeartbeatMonitor {
    
    @Test
    public void testCanInstantiateTheMonitor() throws Exception {
        MockFrameworkRuns runs = new MockFrameworkRuns();
        MockFramework framework = new MockFramework() {
            @Override
            public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
                return runs ;
            };
        };
        MockResourceManagement resourceManagement = new MockResourceManagement();
        MockDSSStore dss = new MockDSSStore(new HashMap<String,String>());
        MockResourceManagementProvider runResourceManagement = new MockResourceManagementProvider();
        MockCPSStore cps = new MockCPSStore(new HashMap<String,String>());
        MockLog log = new MockLog();
        MockTimeService timeService = new MockTimeService(Instant.MIN);
        new RunDeadHeartbeatMonitor(framework, resourceManagement, dss, 
            runResourceManagement, cps, log, timeService);
    }

    class MockResourceManagementExtended extends MockResourceManagement {
        public boolean isSuccessFul = false;

        @Override 
        public void resourceManagementRunSuccessful() {
            this.isSuccessFul = true ;
        }
    };

    class MockFrameworkRunsExtended extends MockFrameworkRuns {

        public List<IRun> activeRuns ;
        public List<String> runNamesInterrupted = new ArrayList<String>();

        public MockFrameworkRunsExtended(List<IRun> activeRuns) {
            this.activeRuns = activeRuns;
        }

        @Override 
        public List<IRun> getActiveRuns() throws FrameworkException {
            return activeRuns;
        }

        @Override
        public boolean markRunInterrupted(String runname, String interruptReason) throws DynamicStatusStoreException {
            runNamesInterrupted.add(runname);
            return true;
        }

        @Override
        public boolean delete(String runname) throws DynamicStatusStoreException {
            IRun matchedRun = null ;
            boolean isDeleted = false ;
            for( IRun run : activeRuns ) {
                if ( runname.equals(run.getName()) ) {
                    // It's this run which needs to be deleted.
                    matchedRun = run ;
                    break;
                }
            }
            if (matchedRun != null) {
                activeRuns.remove(matchedRun);
                isDeleted = true ;
            }
            return isDeleted ;
        }
    }

    @Test
    public void testCanRunMonitorWhenItDetectsNothing() throws Exception {
        MockFrameworkRuns runs = new MockFrameworkRuns() {
            @Override
            public List<IRun> getActiveRuns() throws FrameworkException {
                return new ArrayList<IRun>();
            }
        };
        MockFramework framework = new MockFramework() {
            @Override
            public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
                return runs ;
            };
        };

        MockResourceManagementExtended resourceManagement = new MockResourceManagementExtended();
        MockDSSStore dss = new MockDSSStore(new HashMap<String,String>());
        MockResourceManagementProvider resourceManagementProvider = new MockResourceManagementProvider();
        MockCPSStore cps = new MockCPSStore(new HashMap<String,String>());
        MockLog log = new MockLog();
        MockTimeService timeService = new MockTimeService(Instant.MIN);
        RunDeadHeartbeatMonitor monitor = new RunDeadHeartbeatMonitor(
            framework, resourceManagement, dss, resourceManagementProvider, 
            cps, log, timeService);

        monitor.run();

        assertThat(resourceManagement.isSuccessFul).isEqualTo(true);
    }


    public static final boolean LOCAL_TEST = true;
    public static final boolean REMOTE_TEST = false;


    @Test
    public void testCanRunMonitorWhenItDetectsAnRecentRunWithAHeartBeatSaysItsOK() throws Exception {
        String testRunName = "myTestRunName";
        MockRun run = new MockRun(
            "myTestBundle", 
            "myTestClassName",
            testRunName,
            "myTestStreamName",
            "myTestStreamOBR",
            "myTestStreamReportUrl",
            "myTestRequestorName",
            REMOTE_TEST );
        run.setSharedEnvironment(false);

        Instant timeOfRunHeartbeat = Instant.MIN;
        run.setHeartbeat(timeOfRunHeartbeat);
        
        List<IRun> runsList = new ArrayList<IRun>();
        runsList.add(run);

        MockFrameworkRuns runs = new MockFrameworkRuns() {
            @Override
            public List<IRun> getActiveRuns() throws FrameworkException {
                return runsList;
            }
        };
        MockFramework framework = new MockFramework() {
            @Override
            public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
                return runs ;
            };
        };

        MockResourceManagementExtended resourceManagement = new MockResourceManagementExtended();
        MockDSSStore dss = new MockDSSStore(new HashMap<String,String>());
        MockResourceManagementProvider resourceManagementProvider = new MockResourceManagementProvider();
        MockCPSStore cps = new MockCPSStore(new HashMap<String,String>());
        MockLog log = new MockLog();
        // The time service also thinks it's the same time as the test run heartbeat.
        MockTimeService timeService = new MockTimeService(timeOfRunHeartbeat);
        RunDeadHeartbeatMonitor monitor = new RunDeadHeartbeatMonitor(
            framework, resourceManagement, dss, resourceManagementProvider, 
            cps, log, timeService);

        // When...
        monitor.run();

        // Then...
        assertThat(log.contains("Run " + testRunName + " heartbeat is ok")).isTrue();
        assertThat(resourceManagement.isSuccessFul).isEqualTo(true);
    }



    @Test
    public void testCanRunMonitorWhenItDetectsAnAncientRemoteRunWithAHeartBeatGetsReset() throws Exception {
        String testRunName = "myTestRunName";
        MockRun run = new MockRun(
            "myTestBundle", 
            "myTestClassName",
            testRunName,
            "myTestStreamName",
            "myTestStreamOBR",
            "myTestStreamReportUrl",
            "myTestRequestorName",
            REMOTE_TEST );
        run.setSharedEnvironment(false);
        Instant timeOfRunHeartbeat = Instant.now();
        run.setHeartbeat(timeOfRunHeartbeat);
        
        List<IRun> runsList = new ArrayList<IRun>();
        runsList.add(run);

        MockFrameworkRunsExtended runs = new MockFrameworkRunsExtended(runsList);

        MockFramework framework = new MockFramework() {
            @Override
            public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
                return runs ;
            };
        };

        MockResourceManagementExtended resourceManagement = new MockResourceManagementExtended();
        MockDSSStore dss = new MockDSSStore(new HashMap<String,String>());
        MockResourceManagementProvider resourceManagementProvider = new MockResourceManagementProvider();
        MockCPSStore cps = new MockCPSStore(new HashMap<String,String>());
        MockLog log = new MockLog();

        // The timer service thinks it's more recent than the test run.
        // Adding one day to make the test look a day old.
        Instant laterTime = timeOfRunHeartbeat.plus(1, ChronoUnit.DAYS);
        MockTimeService timeService = new MockTimeService(laterTime);

        RunDeadHeartbeatMonitor monitor = new RunDeadHeartbeatMonitor(
            framework, resourceManagement, dss, resourceManagementProvider, 
            cps, log, timeService);

        // When...
        monitor.run();

        // Then...
        assertThat(runs.runNamesInterrupted.contains(testRunName)).isTrue();
        assertThat(log.contains("Resetting run " + testRunName + ", last heartbeat was at ")).isTrue();
        assertThat(resourceManagement.isSuccessFul).isEqualTo(true);
    }

    @Test
    public void testCanRunMonitorWhenItDetectsAnAncientRemoteRunWithNoHeartBeatGetsIgnoredInitially() throws Exception {
        String testRunName = "myTestRunName";
        MockRun run = new MockRun(
            "myTestBundle", 
            "myTestClassName",
            testRunName,
            "myTestStreamName",
            "myTestStreamOBR",
            "myTestStreamReportUrl",
            "myTestRequestorName",
            REMOTE_TEST );
        run.setSharedEnvironment(false);
        Instant timeOfRunHeartbeat = null;
        run.setHeartbeat(timeOfRunHeartbeat);
        
        List<IRun> runsList = new ArrayList<IRun>();
        runsList.add(run);

        MockFrameworkRunsExtended runs = new MockFrameworkRunsExtended(runsList);

        MockFramework framework = new MockFramework() {
            @Override
            public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
                return runs ;
            };
        };

        MockResourceManagementExtended resourceManagement = new MockResourceManagementExtended();
        MockDSSStore dss = new MockDSSStore(new HashMap<String,String>());
        MockResourceManagementProvider resourceManagementProvider = new MockResourceManagementProvider();
        MockCPSStore cps = new MockCPSStore(new HashMap<String,String>());
        MockLog log = new MockLog();

        // The timer service thinks it's more recent than the test run. But as the run has no heartbeat
        // that's quite easy for it.
        Instant initialTime = Instant.MIN.plus(1,ChronoUnit.DAYS);
        MockTimeService timeService = new MockTimeService(initialTime);

        RunDeadHeartbeatMonitor monitor = new RunDeadHeartbeatMonitor(
            framework, resourceManagement, dss, resourceManagementProvider, 
            cps, log, timeService);

        // When...
        monitor.run();

        // Then...
        // The test run should still be in the list.
        assertThat(runsList).contains(run); 

        // It should have been ignored initially.
        assertThat(log.contains("ignored")).isTrue();

        // The monitor should have passed overall.
        assertThat(resourceManagement.isSuccessFul).isEqualTo(true);
    }

    @Test
    public void testCanRunMonitorWhenItDetectsAnAncientRemoteRunWithNoHeartBeatGetsCleanedUpEventually() throws Exception {
        String testRunName = "myTestRunName";
        MockRun run = new MockRun(
            "myTestBundle", 
            "myTestClassName",
            testRunName,
            "myTestStreamName",
            "myTestStreamOBR",
            "myTestStreamReportUrl",
            "myTestRequestorName",
            REMOTE_TEST );
        run.setSharedEnvironment(false);
        Instant timeOfRunHeartbeat = null;
        run.setHeartbeat(timeOfRunHeartbeat);
        
        List<IRun> runsList = new ArrayList<IRun>();
        runsList.add(run);

        MockFrameworkRunsExtended runs = new MockFrameworkRunsExtended(runsList);

        MockFramework framework = new MockFramework() {
            @Override
            public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
                return runs ;
            };
        };

        MockResourceManagementExtended resourceManagement = new MockResourceManagementExtended();
        MockDSSStore dss = new MockDSSStore(new HashMap<String,String>());
        MockResourceManagementProvider resourceManagementProvider = new MockResourceManagementProvider();
        MockCPSStore cps = new MockCPSStore(new HashMap<String,String>());
        MockLog log = new MockLog();

        // The timer service thinks it's more recent than the test run. But as the run has no heartbeat
        // that's quite easy for it.
        // One day into the epoch
        Instant initialTime = Instant.MIN.plus(1,ChronoUnit.DAYS); 
        MockTimeService timeService = new MockTimeService(initialTime);

        RunDeadHeartbeatMonitor monitor = new RunDeadHeartbeatMonitor(
            framework, resourceManagement, dss, resourceManagementProvider, 
            cps, log, timeService);

        
        // The monitor should cache information about this run with a missing heartbeat...
        monitor.run();

        // Double check that the test run should not be interrupted.
        assertThat(runs.runNamesInterrupted).doesNotContain(testRunName); 

        // Now advance the clock somewhat so that the run with no heartbeat is out of date...
        // advanced clock by 10 minutes...
        Instant slightlyLaterTime  = initialTime.plus(10,ChronoUnit.MINUTES);
        timeService.setCurrentTime(slightlyLaterTime);

        // When...
        monitor.run();

        // Then...

        // The run should have been interrupted now.
        assertThat(runs.runNamesInterrupted).contains(testRunName); 

        // The fact that it was interrupted should have been logged.
        assertThat(log.contains("Interrupting")).isTrue();

        // The monitor should have passed overall.
        assertThat(resourceManagement.isSuccessFul).isEqualTo(true);
    }



    @Test
    public void testCanRunMonitorDebrisInNoHeartbeatCacheGetsCleanedUpOverTime() throws Exception {
        String testRunName = "myTestRunName";
        MockRun run = new MockRun(
            "myTestBundle", 
            "myTestClassName",
            testRunName,
            "myTestStreamName",
            "myTestStreamOBR",
            "myTestStreamReportUrl",
            "myTestRequestorName",
            REMOTE_TEST );
        run.setSharedEnvironment(false);
        Instant timeOfRunHeartbeat = null;
        run.setHeartbeat(timeOfRunHeartbeat);
        
        List<IRun> runsList = new ArrayList<IRun>();
        runsList.add(run);

        MockFrameworkRunsExtended runs = new MockFrameworkRunsExtended(runsList);

        MockFramework framework = new MockFramework() {
            @Override
            public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
                return runs ;
            };
        };

        MockResourceManagementExtended resourceManagement = new MockResourceManagementExtended();
        MockDSSStore dss = new MockDSSStore(new HashMap<String,String>());
        MockResourceManagementProvider resourceManagementProvider = new MockResourceManagementProvider();
        MockCPSStore cps = new MockCPSStore(new HashMap<String,String>());
        MockLog log = new MockLog();

        // The timer service thinks it's more recent than the test run. But as the run has no heartbeat
        // that's quite easy for it.
        // Clock is one second further on than when the test run started.
        Instant initialTime = Instant.MIN.plusSeconds(1); 
        MockTimeService timeService = new MockTimeService(initialTime);

        RunDeadHeartbeatMonitor monitor = new RunDeadHeartbeatMonitor(
            framework, resourceManagement, dss, resourceManagementProvider, 
            cps, log, timeService);

        
        // The monitor should cache information about this run with a missing heartbeat...
        monitor.run();

        // Double check that the test run should still be in the list.
        assertThat(runsList).contains(run); 

        // Right. Now lets say the test cleans itself up.
        runsList.remove(run);

        // Now advance the clock somewhat so that the run with no heartbeat cache entry out of date...
        // advanced clock by 10 minutes...
        Instant slightlyLaterTime  = initialTime.plus(10,ChronoUnit.MINUTES);
        timeService.setCurrentTime(slightlyLaterTime);

        // When...
        monitor.run();

        // Then...

        // The fact that it was deleted should have been logged.
        assertThat(log.contains("Forgetting that now")).isTrue();

        // The monitor should have passed overall.
        assertThat(resourceManagement.isSuccessFul).isEqualTo(true);
    }

}
