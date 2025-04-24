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
    QUEUED("queued"),
    FINISHED("finished"),
    BUILDING("building"),
    GENERATING("generating"),
    RUNNING("running"),
    RUNDONE("rundone"),
    UP("up"),
    STARTED("started"),
    PROVSTART("provstart"),
    ENDING("ending"),
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
