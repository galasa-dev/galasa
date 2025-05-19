 /*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.runs.common;

public enum GroupRunActionStatus {

    CANCELLED("cancelled"),
    ;
    private String value;

    private GroupRunActionStatus(String type){
        this.value = type;
    }

    public static GroupRunActionStatus getfromString(String typeAsString){
        GroupRunActionStatus match = null;
        for (GroupRunActionStatus type : GroupRunActionStatus.values()){
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
