/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.api.run;

import java.time.Instant;

public class Run {
    private String  name;
    private Instant heartbeat;
    private String  type;
    private String  group;
    private String  submissionId;
    private String  test;
    private String  bundleName;
    private String  testName;
    private String  status;
    private String  result;
    private Instant queued;
    private Instant finished;
    private Instant waitUntil;
    private String  requestor;
    private String  stream;
    private String  repo;
    private String  obr;
    private boolean isLocal;
    private boolean isTraceEnabled;
    private String  rasRunId;


    public Run(String name, Instant heartbeat, String type, String group, String test, String bundleName,
            String testName, String status, String result, Instant queued, Instant finished, Instant waitUntil,
            String requestor, String stream, String repo, String obr, boolean isLocal, boolean isTraceEnabled,
            String rasRunId, String submissionId
    ) {
        this.name = name;
        this.heartbeat = heartbeat;
        this.type = type;
        this.group = group;
        this.submissionId = submissionId;
        this.test = test;
        this.bundleName = bundleName;
        this.testName = testName;
        this.status = status;
        this.result = result;
        this.queued = queued;
        this.finished = finished;
        this.waitUntil = waitUntil;
        this.requestor = requestor;
        this.stream = stream;
        this.repo = repo;
        this.obr = obr;
        this.isLocal = isLocal;
        this.isTraceEnabled = isTraceEnabled;
        this.rasRunId = rasRunId;
    }

    public String getName() {
        return name;
    }

    public Instant getHeartbeat() {
        return heartbeat;
    }

    public String getType() {
        return type;
    }

    public String getGroup() {
        return group;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public String getTest() {
        return test;
    }

    public String getBundleName() {
        return bundleName;
    }

    public String getTestName() {
        return testName;
    }

    public String getStatus() {
        return status;
    }

    public Instant getQueued() {
        return queued;
    }

    public Instant getFinished() {
        return finished;
    }

    public Instant getWaitUntil() {
        return waitUntil;
    }

    public String getRequestor() {
        return requestor;
    }

    public String getStream() {
        return stream;
    }

    public String getRepo() {
        return repo;
    }

    public String getObr() {
        return obr;
    }

    /** 
     * @deprecated
     * This method will be removed in later versions of the code.
     * 
     * Use {@link #isLocal} instead.
     */
    @Deprecated(since = "0.30.0", forRemoval = true)
    public boolean getLocal() {
        return isLocal();
    }

    /**
     * @since 0.30.0
     */
    public boolean isLocal(){
        return isLocal;
    }

    /** 
     * @deprecated
     * This method will be removed in later versions of the code.
     * 
     * Use {@link #isTraceEnabled} instead.
     */
    @Deprecated (since = "0.30.0", forRemoval = true)
    public boolean getTrace() {
        return isTraceEnabled();
    }

    /**
     * @since 0.30.0
     */
    public boolean isTraceEnabled(){
        return isTraceEnabled;
    }

    public String getResult() {
        return result;
    }
    
    public String getRasRunId() {
        return rasRunId;
    }

}
