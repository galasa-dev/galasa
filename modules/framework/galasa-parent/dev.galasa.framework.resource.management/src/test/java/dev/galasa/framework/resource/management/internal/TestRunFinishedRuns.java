/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import dev.galasa.framework.mocks.MockCPSStore;
import dev.galasa.framework.mocks.MockDSSStore;
import dev.galasa.framework.mocks.MockFramework;
import dev.galasa.framework.mocks.MockFrameworkRuns;
import dev.galasa.framework.mocks.MockRun;
import dev.galasa.framework.resource.management.internal.mocks.MockResourceManagement;
import dev.galasa.framework.resource.management.internal.mocks.MockResourceManagementProvider;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IRun;

public class TestRunFinishedRuns {

    @Test
    public void testCanInstantiateRunFinishedRuns() throws Exception {
        // Given...
        MockFrameworkRuns runs = new MockFrameworkRuns();
        MockFramework framework = new MockFramework() {
            @Override
            public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
                return runs;
            }
        };
        MockResourceManagement resourceManagement = new MockResourceManagement();
        MockDSSStore dss = new MockDSSStore(new HashMap<String, String>());
        MockResourceManagementProvider runResourceManagement = new MockResourceManagementProvider();
        MockCPSStore cps = new MockCPSStore(new HashMap<String, String>());

        // When...
        RunFinishedRuns monitor = new RunFinishedRuns(framework, resourceManagement, dss, runResourceManagement, cps);

        // Then...
        assertThat(monitor).isNotNull();
    }

    @Test
    public void testRunWithNoFinishedRunsCompletesSuccessfully() throws Exception {
        // Given...
        List<IRun> runs = new ArrayList<>();
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(runs);

        MockFramework framework = new MockFramework() {
            @Override
            public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
                return mockFrameworkRuns;
            }
        };

        MockResourceManagement resourceManagement = new MockResourceManagement();
        MockDSSStore dss = new MockDSSStore(new HashMap<String, String>());
        MockResourceManagementProvider runResourceManagement = new MockResourceManagementProvider();
        MockCPSStore cps = new MockCPSStore(new HashMap<String, String>());

        RunFinishedRuns monitor = new RunFinishedRuns(framework, resourceManagement, dss, runResourceManagement, cps);

        // When...
        monitor.run();

        // Then...
        assertThat(resourceManagement.isSuccessful).isTrue();
        assertThat(mockFrameworkRuns.getDeletedRunNames()).isEmpty();
    }

    @Test
    public void testRunWithNonFinishedRunsDoesNotDeleteThem() throws Exception {
        // Given...
        List<IRun> runs = new ArrayList<>();
        MockRun run1 = new MockRun("bundle", "class", "run1", "stream", "obr", "repo",
                "requestor", false);
        run1.setStatus("queued");
        runs.add(run1);

        MockRun run2 = new MockRun("bundle", "class", "run2", "stream", "obr", "repo",
                "requestor", false);
        run2.setStatus("running");
        runs.add(run2);

        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(runs);

        MockFramework framework = new MockFramework() {
            @Override
            public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
                return mockFrameworkRuns;
            }
        };

        MockResourceManagement resourceManagement = new MockResourceManagement();
        MockDSSStore dss = new MockDSSStore(new HashMap<String, String>());
        MockResourceManagementProvider runResourceManagement = new MockResourceManagementProvider();
        MockCPSStore cps = new MockCPSStore(new HashMap<String, String>());

        RunFinishedRuns monitor = new RunFinishedRuns(framework, resourceManagement, dss, runResourceManagement, cps);

        // When...
        monitor.run();

        // Then...
        assertThat(resourceManagement.isSuccessful).isTrue();
        assertThat(mockFrameworkRuns.getDeletedRunNames()).isEmpty();
    }

    @Test
    public void testRunDeletesFinishedRunThatHasExpired() throws Exception {
        // Given...
        List<IRun> runs = new ArrayList<>();
        MockRun finishedRun = new MockRun("bundle", "class", "run1", "stream", "obr", "repo",
                "requestor", false);
        finishedRun.setStatus("finished");
        // Set finished time to 10 minutes ago (default timeout is 5 minutes)
        finishedRun.setFinished(Instant.now().minus(10, ChronoUnit.MINUTES));
        runs.add(finishedRun);

        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(runs);

        MockFramework framework = new MockFramework() {
            @Override
            public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
                return mockFrameworkRuns;
            }
        };

        MockResourceManagement resourceManagement = new MockResourceManagement();
        MockDSSStore dss = new MockDSSStore(new HashMap<String, String>());
        MockResourceManagementProvider runResourceManagement = new MockResourceManagementProvider();
        MockCPSStore cps = new MockCPSStore(new HashMap<String, String>());

        RunFinishedRuns monitor = new RunFinishedRuns(framework, resourceManagement, dss, runResourceManagement, cps);

        // When...
        monitor.run();

        // Then...
        assertThat(resourceManagement.isSuccessful).isTrue();
        assertThat(mockFrameworkRuns.getDeletedRunNames()).contains("run1");
    }

    @Test
    public void testRunDoesNotDeleteFinishedRunThatHasNotExpired() throws Exception {
        // Given...
        List<IRun> runs = new ArrayList<>();
        MockRun finishedRun = new MockRun("bundle", "class", "run1", "stream", "obr", "repo",
                "requestor", false);
        finishedRun.setStatus("finished");
        // Set finished time to 1 minute ago (default timeout is 5 minutes)
        finishedRun.setFinished(Instant.now().minus(1, ChronoUnit.MINUTES));
        runs.add(finishedRun);

        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(runs);

        MockFramework framework = new MockFramework() {
            @Override
            public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
                return mockFrameworkRuns;
            }
        };

        MockResourceManagement resourceManagement = new MockResourceManagement();
        MockDSSStore dss = new MockDSSStore(new HashMap<String, String>());
        MockResourceManagementProvider runResourceManagement = new MockResourceManagementProvider();
        MockCPSStore cps = new MockCPSStore(new HashMap<String, String>());

        RunFinishedRuns monitor = new RunFinishedRuns(framework, resourceManagement, dss, runResourceManagement, cps);

        // When...
        monitor.run();

        // Then...
        assertThat(resourceManagement.isSuccessful).isTrue();
        assertThat(mockFrameworkRuns.getDeletedRunNames()).isEmpty();
    }

    @Test
    public void testRunDeletesFinishedRunWithNullFinishedTime() throws Exception {
        // Given...
        List<IRun> runs = new ArrayList<>();
        MockRun finishedRun = new MockRun("bundle", "class", "run1", "stream", "obr", "repo",
                "requestor", false);
        finishedRun.setStatus("finished");
        finishedRun.setFinished(null); // No finished time
        runs.add(finishedRun);

        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(runs);

        MockFramework framework = new MockFramework() {
            @Override
            public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
                return mockFrameworkRuns;
            }
        };

        MockResourceManagement resourceManagement = new MockResourceManagement();
        MockDSSStore dss = new MockDSSStore(new HashMap<String, String>());
        MockResourceManagementProvider runResourceManagement = new MockResourceManagementProvider();
        MockCPSStore cps = new MockCPSStore(new HashMap<String, String>());

        RunFinishedRuns monitor = new RunFinishedRuns(framework, resourceManagement, dss, runResourceManagement, cps);

        // When...
        monitor.run();

        // Then...
        assertThat(resourceManagement.isSuccessful).isTrue();
        assertThat(mockFrameworkRuns.getDeletedRunNames()).contains("run1");
    }

    @Test
    public void testRunUsesCustomTimeoutFromCPS() throws Exception {
        // Given...
        List<IRun> runs = new ArrayList<>();
        MockRun finishedRun = new MockRun("bundle", "class", "run1", "stream", "obr", "repo",
                "requestor", false);
        finishedRun.setStatus("finished");
        // Set finished time to 2 minutes ago
        finishedRun.setFinished(Instant.now().minus(2, ChronoUnit.MINUTES));
        runs.add(finishedRun);

        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(runs);

        MockFramework framework = new MockFramework() {
            @Override
            public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
                return mockFrameworkRuns;
            }
        };

        MockResourceManagement resourceManagement = new MockResourceManagement();
        MockDSSStore dss = new MockDSSStore(new HashMap<String, String>());
        MockResourceManagementProvider runResourceManagement = new MockResourceManagementProvider();
        
        // Set custom timeout to 60 seconds (1 minute)
        HashMap<String, String> cpsProperties = new HashMap<>();
        cpsProperties.put("resource.management.finished.timeout", "60");
        MockCPSStore cps = new MockCPSStore(cpsProperties);

        RunFinishedRuns monitor = new RunFinishedRuns(framework, resourceManagement, dss, runResourceManagement, cps);

        // When...
        monitor.run();

        // Then...
        // Run should be deleted because it finished 2 minutes ago and timeout is 1 minute
        assertThat(resourceManagement.isSuccessful).isTrue();
        assertThat(mockFrameworkRuns.getDeletedRunNames()).contains("run1");
    }

    @Test
    public void testRunDoesNotDeleteWhenCustomTimeoutNotReached() throws Exception {
        // Given...
        List<IRun> runs = new ArrayList<>();
        MockRun finishedRun = new MockRun("bundle", "class", "run1", "stream", "obr", "repo",
                "requestor", false);
        finishedRun.setStatus("finished");
        // Set finished time to 30 seconds ago
        finishedRun.setFinished(Instant.now().minus(30, ChronoUnit.SECONDS));
        runs.add(finishedRun);

        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(runs);

        MockFramework framework = new MockFramework() {
            @Override
            public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
                return mockFrameworkRuns;
            }
        };

        MockResourceManagement resourceManagement = new MockResourceManagement();
        MockDSSStore dss = new MockDSSStore(new HashMap<String, String>());
        MockResourceManagementProvider runResourceManagement = new MockResourceManagementProvider();
        
        // Set custom timeout to 600 seconds (10 minutes)
        HashMap<String, String> cpsProperties = new HashMap<>();
        cpsProperties.put("resource.management.finished.timeout", "600");
        MockCPSStore cps = new MockCPSStore(cpsProperties);

        RunFinishedRuns monitor = new RunFinishedRuns(framework, resourceManagement, dss, runResourceManagement, cps);

        // When...
        monitor.run();

        // Then...
        // Run should NOT be deleted because it finished 30 seconds ago and timeout is 10 minutes
        assertThat(resourceManagement.isSuccessful).isTrue();
        assertThat(mockFrameworkRuns.getDeletedRunNames()).isEmpty();
    }

    @Test
    public void testRunDeletesMultipleExpiredFinishedRuns() throws Exception {
        // Given...
        List<IRun> runs = new ArrayList<>();
        
        MockRun finishedRun1 = new MockRun("bundle", "class", "run1", "stream", "obr", "repo",
                "requestor", false);
        finishedRun1.setStatus("finished");
        finishedRun1.setFinished(Instant.now().minus(10, ChronoUnit.MINUTES));
        runs.add(finishedRun1);

        MockRun finishedRun2 = new MockRun("bundle", "class", "run2", "stream", "obr", "repo",
                "requestor", false);
        finishedRun2.setStatus("finished");
        finishedRun2.setFinished(Instant.now().minus(15, ChronoUnit.MINUTES));
        runs.add(finishedRun2);

        MockRun finishedRun3 = new MockRun("bundle", "class", "run3", "stream", "obr", "repo",
                "requestor", false);
        finishedRun3.setStatus("finished");
        finishedRun3.setFinished(Instant.now().minus(20, ChronoUnit.MINUTES));
        runs.add(finishedRun3);

        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(runs);

        MockFramework framework = new MockFramework() {
            @Override
            public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
                return mockFrameworkRuns;
            }
        };

        MockResourceManagement resourceManagement = new MockResourceManagement();
        MockDSSStore dss = new MockDSSStore(new HashMap<String, String>());
        MockResourceManagementProvider runResourceManagement = new MockResourceManagementProvider();
        MockCPSStore cps = new MockCPSStore(new HashMap<String, String>());

        RunFinishedRuns monitor = new RunFinishedRuns(framework, resourceManagement, dss, runResourceManagement, cps);

        // When...
        monitor.run();

        // Then...
        assertThat(resourceManagement.isSuccessful).isTrue();
        assertThat(mockFrameworkRuns.getDeletedRunNames()).containsExactlyInAnyOrder("run1", "run2", "run3");
    }

    @Test
    public void testRunDeletesSomeExpiredRunsButNotOthers() throws Exception {
        // Given...
        List<IRun> runs = new ArrayList<>();
        
        // Expired run
        MockRun expiredRun = new MockRun("bundle", "class", "expiredRun", "stream", "obr", "repo",
                "requestor", false);
        expiredRun.setStatus("finished");
        expiredRun.setFinished(Instant.now().minus(10, ChronoUnit.MINUTES));
        runs.add(expiredRun);

        // Not expired run
        MockRun notExpiredRun = new MockRun("bundle", "class", "notExpiredRun", "stream", "obr", "repo",
                "requestor", false);
        notExpiredRun.setStatus("finished");
        notExpiredRun.setFinished(Instant.now().minus(1, ChronoUnit.MINUTES));
        runs.add(notExpiredRun);

        // Non-finished run
        MockRun runningRun = new MockRun("bundle", "class", "runningRun", "stream", "obr", "repo",
                "requestor", false);
        runningRun.setStatus("running");
        runs.add(runningRun);

        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(runs);

        MockFramework framework = new MockFramework() {
            @Override
            public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
                return mockFrameworkRuns;
            }
        };

        MockResourceManagement resourceManagement = new MockResourceManagement();
        MockDSSStore dss = new MockDSSStore(new HashMap<String, String>());
        MockResourceManagementProvider runResourceManagement = new MockResourceManagementProvider();
        MockCPSStore cps = new MockCPSStore(new HashMap<String, String>());

        RunFinishedRuns monitor = new RunFinishedRuns(framework, resourceManagement, dss, runResourceManagement, cps);

        // When...
        monitor.run();

        // Then...
        assertThat(resourceManagement.isSuccessful).isTrue();
        assertThat(mockFrameworkRuns.getDeletedRunNames()).containsExactly("expiredRun");
    }

    @Test
    public void testRunHandlesInvalidTimeoutPropertyGracefully() throws Exception {
        // Given...
        List<IRun> runs = new ArrayList<>();
        MockRun finishedRun = new MockRun("bundle", "class", "run1", "stream", "obr", "repo",
                "requestor", false);
        finishedRun.setStatus("finished");
        finishedRun.setFinished(Instant.now().minus(10, ChronoUnit.MINUTES));
        runs.add(finishedRun);

        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(runs);

        MockFramework framework = new MockFramework() {
            @Override
            public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
                return mockFrameworkRuns;
            }
        };

        MockResourceManagement resourceManagement = new MockResourceManagement();
        MockDSSStore dss = new MockDSSStore(new HashMap<String, String>());
        MockResourceManagementProvider runResourceManagement = new MockResourceManagementProvider();
        
        // Set invalid timeout property
        HashMap<String, String> cpsProperties = new HashMap<>();
        cpsProperties.put("resource.management.finished.timeout", "invalid");
        MockCPSStore cps = new MockCPSStore(cpsProperties);

        RunFinishedRuns monitor = new RunFinishedRuns(framework, resourceManagement, dss, runResourceManagement, cps);

        // When...
        monitor.run();

        // Then...
        // Should use default timeout (300 seconds = 5 minutes) and delete the run
        assertThat(resourceManagement.isSuccessful).isTrue();
        assertThat(mockFrameworkRuns.getDeletedRunNames()).contains("run1");
    }

    @Test
    public void testRunHandlesExceptionDuringGetAllRunsGracefully() throws Exception {
        // Given...
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns() {
            @Override
            public List<IRun> getAllRuns() throws FrameworkException {
                throw new FrameworkException("Simulated error getting runs");
            }
        };

        MockFramework framework = new MockFramework() {
            @Override
            public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
                return mockFrameworkRuns;
            }
        };

        MockResourceManagement resourceManagement = new MockResourceManagement();
        MockDSSStore dss = new MockDSSStore(new HashMap<String, String>());
        MockResourceManagementProvider runResourceManagement = new MockResourceManagementProvider();
        MockCPSStore cps = new MockCPSStore(new HashMap<String, String>());

        RunFinishedRuns monitor = new RunFinishedRuns(framework, resourceManagement, dss, runResourceManagement, cps);

        // When...
        monitor.run();

        // Then...
        // Should handle exception gracefully and still mark as successful
        assertThat(resourceManagement.isSuccessful).isTrue();
    }

    @Test
    public void testRunHandlesExceptionDuringDeleteGracefully() throws Exception {
        // Given...
        List<IRun> runs = new ArrayList<>();
        MockRun finishedRun = new MockRun("bundle", "class", "run1", "stream", "obr", "repo",
                "requestor", false);
        finishedRun.setStatus("finished");
        finishedRun.setFinished(Instant.now().minus(10, ChronoUnit.MINUTES));
        runs.add(finishedRun);

        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(runs) {
            @Override
            public List<IRun> getAllRuns() throws FrameworkException {
                return runs;
            }

            @Override
            public boolean delete(String runname) throws DynamicStatusStoreException {
                throw new DynamicStatusStoreException("Simulated delete error");
            }
        };

        MockFramework framework = new MockFramework() {
            @Override
            public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
                return mockFrameworkRuns;
            }
        };

        MockResourceManagement resourceManagement = new MockResourceManagement();
        MockDSSStore dss = new MockDSSStore(new HashMap<String, String>());
        MockResourceManagementProvider runResourceManagement = new MockResourceManagementProvider();
        MockCPSStore cps = new MockCPSStore(new HashMap<String, String>());

        RunFinishedRuns monitor = new RunFinishedRuns(framework, resourceManagement, dss, runResourceManagement, cps);

        // When...
        monitor.run();

        // Then...
        // Should handle exception gracefully and still mark as successful
        assertThat(resourceManagement.isSuccessful).isTrue();
    }
}
