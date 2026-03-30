/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.time.Instant;

import dev.galasa.framework.spi.auth.IInternalAuthToken;
import dev.galasa.framework.spi.auth.IInternalUser;

public class MockInternalAuthToken implements IInternalAuthToken {

    private String tokenId;
    private String description;
    private String dexClientId;
    private Instant creationTime;
    private Instant expiryTime;
    private IInternalUser owner;

    public MockInternalAuthToken(String tokenId, String description, Instant creationTime, Instant expiryTime,
            IInternalUser owner, String dexClientId) {
        this.tokenId = tokenId;
        this.description = description;
        this.dexClientId = dexClientId;
        this.creationTime = creationTime;
        this.expiryTime = expiryTime;
        this.owner = owner;
    }

    @Override
    public String getTokenId() {
        return tokenId;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getDexClientId() {
        return dexClientId;
    }

    @Override
    public Instant getCreationTime() {
        return creationTime;
    }

    @Override
    public Instant getExpiryTime() {
        return expiryTime;
    }

    @Override
    public IInternalUser getOwner() {
        return owner;
    }
}
