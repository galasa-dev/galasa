/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.auth.couchdb.internal.beans;

   
public class AuthDBNameViewDesign {
    public String _rev;
    public String _id;
    public AuthStoreDBViews views;
    public String language;

    public AuthDBNameViewDesign(String _rev, String _id, AuthStoreDBViews views, String language) {
        this._rev = _rev;
        this._id = _id;
        this.views = views;
        this.language = language;
    }

    public AuthDBNameViewDesign(){
        
    }
}
    