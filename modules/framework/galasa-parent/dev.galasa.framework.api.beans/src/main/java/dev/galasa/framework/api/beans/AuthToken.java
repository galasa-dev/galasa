/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.beans;

import java.time.Instant;

import com.google.gson.annotations.SerializedName;

public class AuthToken {

    @SerializedName("token_id")
    private String tokenId;

    @SerializedName("description")
    private String description;

    @SerializedName("creation_time")
    private Instant creationTime;

    @SerializedName("expiry_time")
    private Instant expiryTime;

    @SerializedName("owner")
    private User owner;

    public AuthToken(String tokenId, String description, Instant creationTime, Instant expiryTime, User owner) {
        this.tokenId = tokenId;
        this.description = description;
        this.creationTime = creationTime;
        this.expiryTime = expiryTime;
        this.owner = owner;
    }

    public String getTokenId() {
        return tokenId;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public Instant getExpiryTime() {
        return expiryTime;
    }

    public User getOwner() {
        return owner;
    }
}
