/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.runs.common;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.mocks.MockIResultArchiveStore;
import dev.galasa.framework.mocks.MockRun;
import dev.galasa.framework.mocks.MockTimeService;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class TestGroupRuns {


    @Test
    public void testCanCreateRunRasRecordOK() throws Exception {
        // Given...
        ResponseBuilder responseBuidler = new ResponseBuilder();
        MockIResultArchiveStore ras = new MockIResultArchiveStore();
        MockFramework mockFramework = new MockFramework(ras);
        MockTimeService mockTimeService = new MockTimeService(Instant.MIN);
        GroupRuns groupRuns = new GroupRuns(responseBuidler, "/my/path", mockFramework, mockTimeService);
        IRun run = new MockRun("myBundle", "myClassName", "U12345" , "myStream" , "myOBR", "http://my/stream/repo/url", "myRequestorName", false);

        // When...
        groupRuns.createRunRasRecord(run, ras, mockTimeService);

        // Then...
        List<TestStructure> history = ras.getTestStructureHistory();

        assertThat(history).hasSize(1);
        TestStructure testStructure = history.get(0);

        assertThat(testStructure.getStatus()).isEqualTo("queued");
        assertThat(testStructure.getQueued()).isNotNull();
    }

}