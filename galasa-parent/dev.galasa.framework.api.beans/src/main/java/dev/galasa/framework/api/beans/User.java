/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.beans;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("login_id")
    private String loginId;

    public User(String loginId) {
        this.loginId = loginId;
    }

    public String getLoginId() {
        return loginId;
    }
}
