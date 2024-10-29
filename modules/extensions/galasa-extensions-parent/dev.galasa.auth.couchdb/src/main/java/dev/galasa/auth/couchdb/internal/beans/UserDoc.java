/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.auth.couchdb.internal.beans;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.SerializedName;

/**
 * A document serialised to exchange json with couchdb, representing a user in the system.
 */
public class UserDoc {

    @SerializedName("_id")
    private String userNumber;

    @SerializedName("_rev")
    private String version;

    @SerializedName("login-id")
    private String loginId;

    @SerializedName("activity")
    private List<FrontEndClient> clients;

    public UserDoc() {
        setClients(new ArrayList<FrontEndClient>());
    }

    public UserDoc(String loginId, List<FrontEndClient> clients) {
        this.loginId = loginId;
        setClients( clients);
    }

    public String getUserNumber(){
        return userNumber;
    }

    public void setUserNumber(String userNumber){
        this.userNumber = userNumber;
    }

    public String getVersion(){
        return version;
    }

    public void setVersion(String version){
        this.version = version;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }
    
    public List<FrontEndClient> getClients() {
        return clients;
    }

    public void setClients(List<FrontEndClient> clients) {
        this.clients = clients;
    }
    
}
