/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.auth.couchdb.internal.beans;

import com.google.gson.annotations.SerializedName;

import dev.galasa.framework.spi.auth.IFrontEndClient;
import java.time.Instant;

public class FrontEndClient implements IFrontEndClient{
    
    @SerializedName("client-name")
    private String clientName;

    @SerializedName("last-login")
    private Instant lastLogin;

    // No-arg constructor
    public FrontEndClient() {}

    // Parameterized constructor
    public FrontEndClient(String clientName, Instant lastLoggedIn) {
        this.clientName = clientName;
        this.lastLogin = lastLoggedIn;
    }

    public FrontEndClient(IFrontEndClient fClient){
        this.clientName = fClient.getClientName();
        this.lastLogin = fClient.getLastLogin();
    }

    // Getter and Setter for lastLoggedIn
    public Instant getLastLogin() {
        return lastLogin;
    }

    @Override
    public void setLastLogin(Instant lastLoginTime) {
        this.lastLogin = lastLoginTime;
    }

    // Getter and Setter for clientName
    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    // toString method to display client details
    @Override
    public String toString() {
        return "Client [clientName=" + clientName + ", lastLoggedIn=" + lastLogin + "]";
    }

}
