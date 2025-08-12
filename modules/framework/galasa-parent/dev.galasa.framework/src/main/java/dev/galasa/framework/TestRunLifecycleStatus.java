/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import java.util.*;

/** 
 * Represents a possible state of a test run.
 * 
 * Over the course of a test run lifespan, the state will transition between these possible values.
 * 
 * @since 0.30.0
 */
public enum TestRunLifecycleStatus {
    // The starting state. From here the state can changed from QUEUED to CANCELLING
    QUEUED("queued"),

    /** When the engine controller sees a QUEUED run, it moves it to ALLOCATED */
    ALLOCATED("allocated"),

    // When the test pod starts, it moves the test run to started.
    STARTED("started"),
    BUILDING("building"),
    PROVSTART("provstart"),
    GENERATING("generating"),
    UP("up"),

    /** The test is running */
    RUNNING("running"),

    // Clean up states.
    RUNDONE("rundone"),
    ENDING("ending"), 
    FINISHED("finished"),

    /**
     * When the test run is cancelled, the engine controller moves the state to cancelling 
     * from QUEUED
     */
    CANCELLING("cancelling"),

    /** 
     * The resources were not available to run the test, so it backs off waiting for a while
     * before it can be attempted again.
     */
    WAITING("waiting"),
    ;

    private String value ;  

    private TestRunLifecycleStatus(String value) {
        this.value = value ;
    }
   
    /**
     * Looks up the enum from the string which describes the enum.
     * 
     * The string matches the enum value if the enum.toString() matches it,
     * ignoring case.
     */
    public static TestRunLifecycleStatus getFromString(String statusAsString) {
        TestRunLifecycleStatus match = null ;
        for( TestRunLifecycleStatus possibleMatch : TestRunLifecycleStatus.values() ) {
            if (possibleMatch.toString().equalsIgnoreCase(statusAsString) ) {
                match = possibleMatch ;
            }
        }
        return match;
    }

    /** 
     * Does the input string represent one of the enumerated values ?
     * 
     * An insensitive string comparison is performed against the enum.toString() 
     */
    public static boolean isStatusValid(String statusAsString){
        TestRunLifecycleStatus status = getFromString(statusAsString);
        return status != null;
    }


    @Override
    public String toString(){
        return value;
    }
      
    /** 
     * @return A list of possible status names, as strings
     */
    public static List<String> getAllAsStringList() {
        List<String> validStatuses = new ArrayList<String>();
        for (TestRunLifecycleStatus status : TestRunLifecycleStatus.values()){
            validStatuses.add(status.toString());
        }
        return validStatuses;

    }
}
