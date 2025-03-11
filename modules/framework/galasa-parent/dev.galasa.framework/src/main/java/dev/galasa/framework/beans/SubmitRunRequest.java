/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.beans;

import java.util.Properties;

import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFrameworkRuns.SharedEnvironmentPhase;

/**
 * An internal bean class containing the details required to submit a test run to the Galasa framework.
 */
public class SubmitRunRequest {

    private String runType;
    private String requestor;
    private String bundleName;
    private String testName;
    private String groupName;
    private String submissionId;
    private String mavenRepository;
    private String obr;
    private String stream;
    private boolean isLocalRun;
    private boolean isTraceEnabled = false;
    private Properties overrides = new Properties();
    private SharedEnvironmentPhase sharedEnvironmentPhase;
    private String sharedEnvironmentRunName;
    private String language = "java";
    private String bundleTest;
    private String gherkinTest;

    public SubmitRunRequest(
        String runType,
        String requestor,
        String bundleName,
        String testName,
        String groupName,
        String submissionId,
        String mavenRepository,
        String obr,
        String stream,
        boolean isLocalRun,
        boolean isTraceEnabled,
        Properties overrides,
        SharedEnvironmentPhase sharedEnvironmentPhase,
        String sharedEnvironmentRunName,
        String language
    ) throws FrameworkException {
        setTestName(testName);

        this.runType = runType;
        this.requestor = requestor;
        this.bundleName = bundleName;
        this.groupName = groupName;
        this.submissionId = submissionId;
        this.mavenRepository = mavenRepository;
        this.obr = obr;
        this.stream = stream;
        this.isLocalRun = isLocalRun;
        this.isTraceEnabled = isTraceEnabled;
        this.overrides = overrides;
        this.sharedEnvironmentPhase = sharedEnvironmentPhase;
        this.sharedEnvironmentRunName = sharedEnvironmentRunName;
        this.language = language;
    }

    public String getRunType() {
        return runType;
    }

    public void setRunType(String runType) {
        this.runType = runType;
    }

    public String getRequestor() {
        return requestor;
    }

    public void setRequestor(String requestor) {
        this.requestor = requestor;
    }

    public String getBundleName() {
        return bundleName;
    }

    public void setBundleName(String bundleName) {
        this.bundleName = bundleName;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) throws FrameworkException {
        if (testName == null) {
            throw new FrameworkException("Missing test name");
        }
        this.testName = testName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getMavenRepository() {
        return mavenRepository;
    }

    public void setMavenRepository(String mavenRepository) {
        this.mavenRepository = mavenRepository;
    }

    public String getObr() {
        return obr;
    }

    public void setObr(String obr) {
        this.obr = obr;
    }

    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    public boolean isLocalRun() {
        return isLocalRun;
    }

    public void setLocalRun(boolean isLocalRun) {
        this.isLocalRun = isLocalRun;
    }

    public boolean isTraceEnabled() {
        return isTraceEnabled;
    }

    public void setTraceEnabled(boolean isTraceEnabled) {
        this.isTraceEnabled = isTraceEnabled;
    }

    public Properties getOverrides() {
        return overrides;
    }

    public void setOverrides(Properties overrides) {
        this.overrides = overrides;
    }

    public SharedEnvironmentPhase getSharedEnvironmentPhase() {
        return sharedEnvironmentPhase;
    }

    public void setSharedEnvironmentPhase(SharedEnvironmentPhase sharedEnvironmentPhase) {
        this.sharedEnvironmentPhase = sharedEnvironmentPhase;
    }

    public String getSharedEnvironmentRunName() {
        return sharedEnvironmentRunName;
    }

    public void setSharedEnvironmentRunName(String sharedEnvironmentRunName) {
        this.sharedEnvironmentRunName = sharedEnvironmentRunName;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getBundleTest() {
        return bundleTest;
    }

    public void setBundleTest(String bundleTest) {
        this.bundleTest = bundleTest;
    }

    public String getGherkinTest() {
        return gherkinTest;
    }

    public void setGherkinTest(String gherkinTest) {
        this.gherkinTest = gherkinTest;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }
}
