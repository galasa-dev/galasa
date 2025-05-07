/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.teststructure;

import java.time.Instant;
import java.util.List;

public class TestMethod {

    private String           className;

    private String           methodName;
    private String           type;
    
    private String           status;
    private String           result;

    private Instant          startTime;
    private Instant          endTime;

    private String           exception;

    private int              runLogStart;
    private int              runLogEnd;

    private List<TestMethod> befores;
    private List<TestMethod> afters;

    @SuppressWarnings("unused")
    private TestMethod() {
        // NOP
    }

    public TestMethod(Class<?> testClass) {
        this.className = testClass.getName();
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<TestMethod> getBefores() {
        return befores;
    }

    public void setBefores(List<TestMethod> befores) {
        this.befores = befores;
    }

    public List<TestMethod> getAfters() {
        return afters;
    }

    public void setAfters(List<TestMethod> afters) {
        this.afters = afters;
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

    public void report(String prefix, StringBuilder sb) {
        String actualStatus = this.status;
        if (actualStatus == null) {
            actualStatus = "Unknown";
        }

        String subPrefix = prefix + "    ";
        if (this.befores != null) {
            for (TestMethod before : this.befores) {
                before.report(subPrefix, sb);
            }
        }

        sb.append(prefix);
        sb.append("Test Method ");
        sb.append(className);
        sb.append(".");
        sb.append(methodName);
        sb.append(", type=");
        sb.append(type);
        sb.append(", status=");
        sb.append(actualStatus);

        if (this.afters != null) {
            for (TestMethod after : this.afters) {
                after.report(subPrefix, sb);
            }
        }
    }

}