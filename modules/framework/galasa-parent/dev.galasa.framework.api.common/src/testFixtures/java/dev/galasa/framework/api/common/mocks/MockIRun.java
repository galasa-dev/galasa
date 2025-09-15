/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.mocks;

import java.time.Instant;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import dev.galasa.api.run.Run;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.RunRasAction;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class MockIRun implements IRun{

    private String runName;
    private String runType;
    private Instant heartbeat = Instant.parse("2023-10-12T12:16:49.832925Z");
    private Instant queued = Instant.parse("2023-10-12T12:16:49.832925Z");
    private Instant finished = Instant.parse("2023-10-12T12:16:49.832925Z");
    private Instant waitUntil = Instant.parse("2023-10-12T12:16:49.832925Z");
    private String requestor;
    private String test;
    private String runStatus;
    private String bundle; 
    private String testClass;
    private String groupName;
    private String submissionId;
    private String stream;
    private String repo;
    private String obr;
    private String result = "Passed";
    private Set<String> tags ;
    
    public MockIRun(
        String runName,
        String runType,
        String requestor,
        String test,
        String runStatus,
        String bundle,
        String testClass,
        String groupName,
        String submissionId,
        Set<String> tags
    ) {
        this.runName = runName;
        this.runType = runType;
        this.requestor = requestor;
        this.test = test;
        this.runStatus = runStatus;
        this.bundle = bundle;
        this.testClass = testClass;
        this.groupName = groupName;
        this.submissionId = submissionId;
        this.tags = new HashSet<String>();
        if( tags != null) {
            this.tags.addAll(tags);
        }
    }

    @Override
    public String getName() {
        return this.runName;
    }

    @Override
    public Instant getHeartbeat() {
        return this.heartbeat;
    }

    @Override
    public String getType() {
        return this.runType;
    }

    @Override
    public String getTest() {
        return this.test;
    }

    @Override
    public String getStatus() {
        return this.runStatus;
    }

    @Override
    public String getRequestor() {
        return this.requestor;
    }

    @Override
    public String getStream() {
        return this.stream;
    }

    @Override
    public String getTestBundleName() {
       return this.bundle;
    }

    @Override
    public String getTestClassName() {
        return this.testClass;
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public String getGroup() {
        return this.groupName;
    }

    @Override
    public Instant getQueued() {
        return this.queued;
    }

    @Override
    public String getRepository() {
        return this.repo;
    }

    @Override
    public String getOBR() {
        return this.obr;
    }

    @Override
    public boolean isTrace() {
        return false;
    }

    @Override
    public Instant getFinished() {
        return this.finished;
    }

    @Override
    public Instant getWaitUntil() {
        return this.waitUntil;
    }

    @Override
    public Run getSerializedRun() {
        return new Run(runName, heartbeat, runType, groupName, testClass, bundle, test, runStatus, result, queued,
                finished, waitUntil, requestor, stream, repo, obr, false, false, "cdb-"+runName, submissionId, tags);
    }

    @Override
    public String getResult() {
        return this.result;
    }

    @Override
    public boolean isSharedEnvironment() {
        return false;
    }

    @Override
    public String getSubmissionId() {
        return this.submissionId;
    }

    @Override
    public String getRasRunId() {
        return UUID.randomUUID().toString();
    }

    public Set<String> getTags() {
        Set<String> tagsToReturn = new HashSet<String>();
        tagsToReturn.addAll(this.tags);
        return tagsToReturn;
    }

    @Override
    public TestStructure toTestStructure() {
        TestStructure testStructure = new TestStructure();

        testStructure.setBundle(bundle);
        testStructure.setTestName(testClass);
        testStructure.setRunName(runName);
        testStructure.setRequestor(requestor);
        testStructure.setSubmissionId(submissionId);
        testStructure.setGroup(groupName);

        for (String tag : tags) {
            testStructure.addTag(tag);
        }

        return testStructure;
    }

    @Override
    public String getInterruptReason() {
        throw new UnsupportedOperationException("Unimplemented method 'getInterruptReason'");
    }

    @Override
    public String getGherkin() {
        throw new UnsupportedOperationException("Unimplemented method 'getGherkin'");
    }

    @Override
    public List<RunRasAction> getRasActions() {
        throw new UnsupportedOperationException("Unimplemented method 'getRasActions'");
    }

    @Override
    public Instant getInterruptedAt() {
        throw new UnsupportedOperationException("Unimplemented method 'getInterruptedAt'");
    }

    @Override
    public Instant getAllocatedTimeout() {
        throw new UnsupportedOperationException("Unimplemented method 'getAllocatedTimeout'");
    }
}
