/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ras.couchdb.internal.operations;

import static dev.galasa.ras.couchdb.internal.CouchdbRasStore.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.validation.constraints.NotNull;

import dev.galasa.extensions.common.couchdb.CouchdbException;
import dev.galasa.extensions.common.couchdb.pojos.IdRev;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.ras.couchdb.internal.CouchdbRasException;
import dev.galasa.ras.couchdb.internal.CouchdbRasStore;
import dev.galasa.ras.couchdb.internal.pojos.TestStructureCouchdb;

public class CouchdbDeleteRunService {

    private final CouchdbRasStore store;

    public CouchdbDeleteRunService(CouchdbRasStore store) {
        this.store = store;
    }

    public void discardRun(@NotNull TestStructureCouchdb runTestStructure) throws ResultArchiveStoreException {
        try {
            // Build a list of discard operation futures
            List<CompletableFuture<Void>> futures = discardRecords(LOG_DB, runTestStructure.getLogRecordIds());
            futures.addAll(discardRecords(ARTIFACTS_DB, runTestStructure.getArtifactRecordIds()));
            futures.add(discardRecord(RUNS_DB, runTestStructure._id, runTestStructure._rev));

            // Wait for all the discard operations to finish
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (CompletionException e) {
            throw new ResultArchiveStoreException("Failed to discard run: " + runTestStructure._id, e);
        }
    }

    private List<CompletableFuture<Void>> discardRecords(String databaseName, List<String> ids) throws ResultArchiveStoreException {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (String id : ids) {
            futures.add(discardRecord(databaseName, id));
        }
        return futures;
    }

    private CompletableFuture<Void> discardRecord(String databaseName, String id) throws ResultArchiveStoreException {
        return getRevision(databaseName, id)
            .thenCompose(revision -> discardRecord(databaseName, id, revision));
    }

    private CompletableFuture<Void> discardRecord(String databaseName, String id, String revision) {
        return CompletableFuture.runAsync(() -> {
            try {
                store.deleteDocumentFromDatabase(databaseName, id, revision);
            } catch (CouchdbException e) {
                throw new CompletionException(e);
            }
        });
    }

    private CompletableFuture<String> getRevision(String databaseName, String id) {
        return CompletableFuture.supplyAsync(() -> {
            String foundRevision = null;
            try {
                IdRev found = store.getDocumentFromDatabase(databaseName, id, IdRev.class);
                if (found._id == null) {
                    throw new CouchdbRasException("Unable to find runs - Invalid JSON response");
                }
                if (found._rev == null) {
                    throw new CouchdbRasException("Unable to find rev - Invalid JSON response");
                }
                foundRevision = found._rev;
            } catch (CouchdbException | ResultArchiveStoreException e) {
                throw new CompletionException(e);
            }
            return foundRevision;
        });
    }
}
