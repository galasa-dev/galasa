/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.runs.common;

public class GroupRunActionJson {

    private String result;

    public GroupRunActionJson(String result){
        this.result = result;
    }

    public String getResult(){
        return this.result;
    }
    
}
