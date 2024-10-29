 /*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.common;

public enum RunActionStatus {
    FINISHED("finished"),
    QUEUED("queued")
    ;
    private String value;

    private RunActionStatus(String type){
        this.value = type;
    }

    public static RunActionStatus getfromString(String typeAsString){
        RunActionStatus match = null;
        for (RunActionStatus type : RunActionStatus.values()){
            if (type.toString().equalsIgnoreCase(typeAsString)){
                match = type;
            }
        }
        return match;
    }

    @Override
    public String toString(){
        return value;
    }

}