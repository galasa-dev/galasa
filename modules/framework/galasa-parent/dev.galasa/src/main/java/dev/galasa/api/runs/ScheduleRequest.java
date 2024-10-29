/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.api.runs;

import java.io.Serializable;
import java.util.List;
import java.util.Properties;

public class ScheduleRequest implements Serializable {
    private static final long       serialVersionUID = 1L;

    private List<String>            classNames;
    private String                  requestorType;
    private String                  requestor;
    private String                  testStream;
    private String                  obr;
    private String                  mavenRepository;
    private boolean                 trace;
    private String                  sharedEnvironmentPhase;
    private String                  sharedEnvironmentRunName;
    private Properties              overrides;

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public List<String> getClassNames() {
        return classNames;
    }


    public String getRequestorType() {
        return requestorType;
    }

    public String getRequestor() {
        return requestor;
    }

    public String getTestStream() {
        return testStream;
    }

    public String getObr() {
        return obr;
    }

    public String getMavenRepository() {
        return mavenRepository;
    }

    public void setClassNames(List<String> classNames) {
        this.classNames = classNames;
    }

    public void setRequestorType(String requestorType) {
        this.requestorType = requestorType;
    }

    public void setRequestor(String requestor) {
        this.requestor = requestor;
    }

    public void setTestStream(String testStream) {
        this.testStream = testStream;
    }

    public void setObr(String obr) {
        this.obr = obr;
    }

    public void setMavenRepository(String mavenRepository) {
        this.mavenRepository = mavenRepository;
    }

    public void setOverrides(Properties runProperties) {
        overrides = runProperties; 
    }
    
    public Properties getOverrides() {
        return overrides;
    }


    public boolean isTrace() {
        return trace;
    }

    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    public String getSharedEnvironmentPhase() {
        return sharedEnvironmentPhase;
    }

    public void setSharedEnvironmentPhase(String sharedEnvironmentPhase) {
        this.sharedEnvironmentPhase = sharedEnvironmentPhase;
    }

    public String getSharedEnvironmentRunName() {
        return sharedEnvironmentRunName;
    }

    public void setSharedEnvironmentRunName(String sharedEnvironmentRunName) {
        this.sharedEnvironmentRunName = sharedEnvironmentRunName;
    }
    
    

}
