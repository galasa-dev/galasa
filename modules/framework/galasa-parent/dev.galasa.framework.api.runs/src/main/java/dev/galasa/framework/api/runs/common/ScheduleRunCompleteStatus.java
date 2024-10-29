/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.framework.api.runs.common;

public enum ScheduleRunCompleteStatus {
    FINISHED("finished"),
    UP("up"),
    DISCARDED("discarded")
    ;

    private String value;  

    private ScheduleRunCompleteStatus(String value) {
        this.value = value;
    }
    /**
     * Get a vlaue matching the value of the enum above 
     * @param statusAsString
     * @return
     */
    public static ScheduleRunCompleteStatus getFromString(String statusAsString) {
        ScheduleRunCompleteStatus match = null ;
        for( ScheduleRunCompleteStatus possibleMatch : ScheduleRunCompleteStatus.values() ) {
            if (possibleMatch.toString().equalsIgnoreCase(statusAsString) ) {
                match = possibleMatch ;
            }
        }
        return match;
    }

    @Override
    public String toString(){
        return value;
    }

}
