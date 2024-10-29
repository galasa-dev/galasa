/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.auth.couchdb.internal.beans;

import com.google.gson.annotations.SerializedName;

public class AuthStoreDBViews {
    @SerializedName("loginId-view")
    public AuthStoreDBLoginView loginIdView;
}
