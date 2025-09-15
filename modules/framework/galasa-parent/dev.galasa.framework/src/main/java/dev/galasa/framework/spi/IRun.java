/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import dev.galasa.api.run.Run;
import dev.galasa.framework.spi.teststructure.TestStructure;

public interface IRun {

    String getName();

    Instant getHeartbeat();

    String getType();

    String getTest();

    String getStatus();

    String getRequestor();

    String getStream();

    String getTestBundleName();

    String getTestClassName();

    boolean isLocal();

    String getGroup();

    String getSubmissionId();

    Instant getQueued();

    String getRepository();

    String getOBR();

    boolean isTrace();

    Instant getFinished();

    Instant getWaitUntil();

    Run getSerializedRun();

    String getResult();
    
    boolean isSharedEnvironment();

    public String getGherkin();

    String getInterruptReason();

    Instant getInterruptedAt();

    Instant getAllocatedTimeout();

    String getRasRunId();

    List<RunRasAction> getRasActions();
    public Set<String> getTags();

    TestStructure toTestStructure();
}
