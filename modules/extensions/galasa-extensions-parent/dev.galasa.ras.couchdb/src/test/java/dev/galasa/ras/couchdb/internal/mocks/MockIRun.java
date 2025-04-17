/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ras.couchdb.internal.mocks;

import java.time.Instant;
import java.util.List;

import dev.galasa.api.run.Run;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.RunRasAction;

public class MockIRun implements IRun {

    private String name ;

    public MockIRun(String name) {
        this.name = name ;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Instant getHeartbeat() {
        throw new UnsupportedOperationException("Unimplemented method 'getHeartbeat'");
    }

    @Override
    public String getType() {
        throw new UnsupportedOperationException("Unimplemented method 'getType'");
    }

    @Override
    public String getTest() {
        throw new UnsupportedOperationException("Unimplemented method 'getTest'");
    }

    @Override
    public String getStatus() {
        throw new UnsupportedOperationException("Unimplemented method 'getStatus'");
    }

    @Override
    public String getRequestor() {
        throw new UnsupportedOperationException("Unimplemented method 'getRequestor'");
    }

    @Override
    public String getStream() {
        throw new UnsupportedOperationException("Unimplemented method 'getStream'");
    }

    @Override
    public String getTestBundleName() {
        throw new UnsupportedOperationException("Unimplemented method 'getTestBundleName'");
    }

    @Override
    public String getTestClassName() {
        throw new UnsupportedOperationException("Unimplemented method 'getTestClassName'");
    }

    @Override
    public boolean isLocal() {
        throw new UnsupportedOperationException("Unimplemented method 'isLocal'");
    }

    @Override
    public String getGroup() {
        throw new UnsupportedOperationException("Unimplemented method 'getGroup'");
    }

    @Override
    public Instant getQueued() {
        throw new UnsupportedOperationException("Unimplemented method 'getQueued'");
    }

    @Override
    public String getRepository() {
        throw new UnsupportedOperationException("Unimplemented method 'getRepository'");
    }

    @Override
    public String getOBR() {
        throw new UnsupportedOperationException("Unimplemented method 'getOBR'");
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

    @Override
    public boolean isSharedEnvironment() {
        throw new UnsupportedOperationException("Unimplemented method 'isSharedEnvironment'");
    }

    @Override
    public String getGherkin() {
        throw new UnsupportedOperationException("Unimplemented method 'getGherkin'");
    }

    @Override
    public String getSubmissionId() {
        throw new UnsupportedOperationException("Unimplemented method 'getSubmissionId'");
    }

    @Override
    public String getInterruptReason() {
        throw new UnsupportedOperationException("Unimplemented method 'getInterruptReason'");
    }

    @Override
    public String getRasRunId() {
        throw new UnsupportedOperationException("Unimplemented method 'getRasRunId'");
    }

    @Override
    public List<RunRasAction> getRasActions() {
        throw new UnsupportedOperationException("Unimplemented method 'getRasActions'");
    }
}
