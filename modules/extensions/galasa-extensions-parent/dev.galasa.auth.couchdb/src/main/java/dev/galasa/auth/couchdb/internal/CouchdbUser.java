/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.auth.couchdb.internal;

import com.google.gson.annotations.SerializedName;

import dev.galasa.framework.spi.auth.IInternalUser;

public class CouchdbUser implements IInternalUser {
    
    @SerializedName(value = "loginId", alternate = { "login_id" })
    private String loginId;
    private String dexUserId;

    public CouchdbUser(String loginId, String dexUserId) {
        this.loginId = loginId;
        this.dexUserId = dexUserId;
    }

    public CouchdbUser(IInternalUser user) {
        this.loginId = user.getLoginId();
        this.dexUserId = user.getDexUserId();
    }

    @Override
    public String getDexUserId() {
        return dexUserId;
    }

    @Override
    public String getLoginId() {
        return loginId;
    }
}
