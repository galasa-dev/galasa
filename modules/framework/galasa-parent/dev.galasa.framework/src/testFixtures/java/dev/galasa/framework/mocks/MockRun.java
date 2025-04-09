/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.time.Instant;
import java.util.UUID;

import dev.galasa.api.run.Run;
import dev.galasa.framework.spi.IRun;

public class MockRun implements IRun {
    private String testBundleName;
    private String testClassName; 
    private String testRunName;
    private String testStream;
    private String testStreamOBR;
    private String testStreamRepoUrl;
    private String requestorName ;
    private boolean isRunLocal;
    private String gherkinUrl;
    private Instant heartbeat;
    private String group;
    private String submissionId;
    private String status;

    public MockRun(
        String testBundleName, 
        String testClassName, String testRunName , 
        String testStream, String testStreamOBR, 
        String testStreamRepoUrl, String requestorName, 
        boolean isRunLocal
    ) {
        this( testBundleName, 
            testClassName, testRunName , 
            testStream, testStreamOBR, 
            testStreamRepoUrl, requestorName, 
            isRunLocal ,null, null, UUID.randomUUID().toString());
    }
    public MockRun(
        String testBundleName, 
        String testClassName, String testRunName , 
        String testStream, String testStreamOBR, 
        String testStreamRepoUrl, String requestorName, 
        boolean isRunLocal , String gherkinUrl, String group, String submissionId
    ) {
        this.testBundleName = testBundleName;
        this.testClassName = testClassName ;
        this.testRunName = testRunName;
        this.testStream = testStream;
        this.testStreamOBR = testStreamOBR;
        this.testStreamRepoUrl = testStreamRepoUrl;
        this.requestorName = requestorName;
        this.isRunLocal = isRunLocal;
        this.gherkinUrl = gherkinUrl;
        this.submissionId = submissionId;
    }

    // Shared environment not used very often so not adding
    // to the constructor.
    private boolean isSharedEnvironment = false;
    public void setSharedEnvironment(boolean newValue) {
        this.isSharedEnvironment = newValue ;
    }
    
    @Override
    public boolean isSharedEnvironment() {
        return isSharedEnvironment;
    }

    @Override
    public String getTestBundleName() {
        return this.testBundleName;
    }

    @Override
    public String getTestClassName() {
        return this.testClassName;
    }

    @Override
    public String getName() {
        return this.testRunName;
    }

    @Override
    public String getStream() {
        return this.testStream;
    }

    @Override
    public Instant getQueued() {
        return Instant.now();
    }

    @Override
    public String getRequestor() {
        return this.requestorName;
    }

    @Override
    public String getRepository() {
        return this.testStreamRepoUrl;
    }

    @Override
    public String getOBR() {
        return this.testStreamOBR;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public String getSubmissionId() {
        return this.submissionId;
    }

    @Override
    public boolean isLocal() {
        return this.isRunLocal ;
    }

    @Override
    public String getGherkin() {
        return this.gherkinUrl;
    }

    // Heartbeat is something we want to mess around with
    // on the fly so not adding it to the constructor.
    public void setHeartbeat(Instant newValue ) {
        this.heartbeat = newValue;
    }

    @Override
    public Instant getHeartbeat() {
        return this.heartbeat ;
    }

    @Override
    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // ------------- un-implemented methods follow ----------------

    @Override
    public String getType() {
        throw new UnsupportedOperationException("Unimplemented method 'getType'");
    }

    @Override
    public String getTest() {
        throw new UnsupportedOperationException("Unimplemented method 'getTest'");
    }

    @Override
    public boolean isTrace() {
        throw new UnsupportedOperationException("Unimplemented method 'isTrace'");
    }

    @Override
    public Instant getFinished() {
        throw new UnsupportedOperationException("Unimplemented method 'getFinished'");
    }

    @Override
    public Instant getWaitUntil() {
        throw new UnsupportedOperationException("Unimplemented method 'getWaitUntil'");
    }

    @Override
    public Run getSerializedRun() {
        throw new UnsupportedOperationException("Unimplemented method 'getSerializedRun'");
    }

    @Override
    public String getResult() {
        throw new UnsupportedOperationException("Unimplemented method 'getResult'");
    }
}
