/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.rbac;

import dev.galasa.framework.spi.rbac.Action;

public class ActionImpl implements Action {

    private String name ;
    private String id ;
    private String description ;

    public ActionImpl( String id , String name , String description ) {
        this.id = id ;
        this.name = name ;
        this.description = description ;
    }

    @Override
    public String getName() {
       return this.name;
    }

    @Override
    public String getId() {
        return this.id ;
    }

    @Override
    public String getDescription() {
        return this.description ;
    }
    
}
