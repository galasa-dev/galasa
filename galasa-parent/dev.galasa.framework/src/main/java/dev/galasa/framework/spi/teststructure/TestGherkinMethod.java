/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.teststructure;

import java.time.Instant;

public class TestGherkinMethod {
    
    private String           methodName;

    private String           status;
    private String           result;

    private String           exception;

    private int              runLogStart;
    private int              runLogEnd;

    private Instant          startTime;
    private Instant          endTime;

    public void report(String prefix, StringBuilder sb) {
        String actualStatus = this.status;
        if (actualStatus == null) {
            actualStatus = "Unknown";
        }

        sb.append(prefix);
        sb.append("Test Method ");
        sb.append(methodName);
        sb.append(", status=");
        sb.append(actualStatus);

    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
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

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public int getRunLogStart() {
        return runLogStart;
    }

    public void setRunLogStart(int runLogStart) {
        this.runLogStart = runLogStart;
    }

    public int getRunLogEnd() {
        return runLogEnd;
    }

    public void setRunLogEnd(int runLogEnd) {
        this.runLogEnd = runLogEnd;
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
    
    

}
