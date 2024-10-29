/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.auth.couchdb.internal;

import java.time.Instant;

import dev.galasa.framework.spi.auth.IInternalAuthToken;
import dev.galasa.framework.spi.auth.IInternalUser;

public class CouchdbAuthToken implements IInternalAuthToken {

    private String _id;
    private String dexClientId;
    private String description;
    private Instant creationTime;
    private CouchdbUser owner;

    public CouchdbAuthToken(String clientId, String description, Instant creationTime, CouchdbUser owner) {
        this.dexClientId = clientId;
        this.description = description;
        this.creationTime = creationTime;
        this.owner = owner;
    }

    public CouchdbAuthToken(String documentId, String clientId, String description, Instant creationTime, CouchdbUser owner) {
        this(clientId, description, creationTime, owner);
        this._id = documentId;
    }

    public String getTokenId() {
        return _id;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public IInternalUser getOwner() {
        return owner;
    }

    public String getDexClientId() {
        return dexClientId;
    }
}
