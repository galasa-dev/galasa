/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal;

import com.google.gson.annotations.SerializedName;

/**
 * A pojo to represent the details of a Dex client
 */
public class DexClient {

    @SerializedName("client_id")
    private String clientId;

    public DexClient(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }
}
