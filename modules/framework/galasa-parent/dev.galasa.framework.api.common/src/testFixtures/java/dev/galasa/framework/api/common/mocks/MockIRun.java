/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.mocks;

import java.time.Instant;

import dev.galasa.api.run.Run;
import dev.galasa.framework.spi.IRun;

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
    private String stream;
    private String repo;
    private String obr;
    private String result = "Passed";

    
    public MockIRun(String runName, String runType, String requestor, String test, String runStatus, String bundle, String testClass, String groupName){
        this.runName = runName;
        this.runType = runType;
        this.requestor = requestor;
        this.test = test;
        this.runStatus = runStatus;
        this.bundle = bundle;
        this.testClass = testClass;
        this.groupName = groupName;
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
                finished, waitUntil, requestor, stream, repo, obr, false, false, "cdb-"+runName);
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
    public String getGherkin() {
        throw new UnsupportedOperationException("Unimplemented method 'getGherkin'");
    }
    
}