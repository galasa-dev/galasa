/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.teststructure;

import java.time.Instant;
import java.util.*;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * This class represents the Test Class structure, ie it's test methods, order
 * of execution and their status
 *
 *  
 *
 */
public class TestStructure {

    private String           runName = "invalid";
    private String           bundle;
    private String           testName;
    private String           testShortName;

    private String           requestor;

    private String           status;
    private String           result;
    private String           group = "none";
    private String           submissionId;

    private Instant          queued;

    private Instant          startTime;
    private Instant          endTime;

    private List<TestMethod> methods;
    private List<TestGherkinMethod> gherkinMethods;

    private List<String>     logRecordIds;

    private List<String>     artifactRecordIds;

    public TestStructure() {
    }

    public TestStructure( TestStructure source ) {
        if (source!=null) {
            this.runName = source.runName;
            this.bundle = source.bundle;
            this.testName = source.testName;
            this.submissionId = source.submissionId;
            this.testShortName = source.testShortName;
            this.requestor = source.requestor;
            this.status = source.status;
            this.result = source.result;
            this.queued = source.queued;
            this.endTime = source.endTime;
            if (source.methods != null) {
                this.methods = new ArrayList<TestMethod>();
                this.methods.addAll(source.methods);
            }
            if (source.gherkinMethods!= null) {
                this.gherkinMethods = new ArrayList<TestGherkinMethod>();
                this.gherkinMethods.addAll(source.gherkinMethods);
            }
            if (source.logRecordIds != null) {
                this.logRecordIds = new ArrayList<String>();
                this.logRecordIds.addAll(source.logRecordIds);
            }
            if (source.artifactRecordIds != null ) {
                this.artifactRecordIds = new ArrayList<String>();
                this.artifactRecordIds.addAll(source.artifactRecordIds);
            }
        }
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }

    public String getBundle() {
        return bundle;
    }

    public void setBundle(String bundle) {
        this.bundle = bundle;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTestShortName() {
        return testShortName;
    }

    public void setTestShortName(String testShortName) {
        this.testShortName = testShortName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public List<TestMethod> getMethods() {
        return methods;
    }

    public void setMethods(List<TestMethod> methods) {
        this.methods = methods;
    }

    public void setGherkinMethods(List<TestGherkinMethod> methods) {
        this.gherkinMethods = methods;
    }

    public void setRequestor(String requestor) {
        this.requestor = requestor;
    }

    public @NotNull String getRequestor() {
        if (this.requestor == null) {
            return "unknown";
        }

        return this.requestor;
    }

    public void setQueued(Instant queued) {
        this.queued = queued;
    }

    public Instant getQueued() {
        if (queued == null) {
            return this.startTime;
        }

        return queued;
    }

    public String report(String prefix) {
        String actualStatus = this.status;
        if (actualStatus == null) {
            actualStatus = "Unknown";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append("Test Class ");
        sb.append(this.testName);
        sb.append(" status=");
        sb.append(actualStatus);

        String methodPrefix = prefix + "    ";
        for (TestMethod method : this.methods) {
            method.report(methodPrefix, sb);
        }

        return sb.toString();
    }

    public String gherkinReport(String prefix) {
        String actualStatus = this.status;
        if (actualStatus == null) {
            actualStatus = "Unknown";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append("Test Name ");
        sb.append(this.testName);
        sb.append(" status=");
        sb.append(actualStatus);

        String methodPrefix = prefix + "    ";
        for (TestGherkinMethod method : this.gherkinMethods) {
            method.report(methodPrefix, sb);
        }

        return sb.toString();
    }

    public void setRunName(String runName) {
        this.runName = runName;
    }

    public String getRunName() {
        return this.runName;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return this.group;
    }
    
    public boolean isValid() {
        return this.runName != null;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public List<String> getLogRecordIds() {
        return logRecordIds;
    }

    public void setLogRecordIds(List<String> logRecordIds) {
        this.logRecordIds = logRecordIds;
    }

    public List<String> getArtifactRecordIds() {
        return artifactRecordIds;
    }

    public void setArtifactRecordIds(List<String> artifactRecordIds) {
        this.artifactRecordIds = artifactRecordIds;
    }

    public void normalise() {
        if (this.status == null) {
            this.status = "unknown";
        }

        if (this.requestor == null) {
            this.requestor = "unknown";
        }

        if (this.queued == null) {
            this.queued = this.startTime;
        }
    }

}
