/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ras.couchdb.internal;

import java.nio.file.Path;

import org.apache.commons.logging.Log;

import dev.galasa.extensions.common.api.LogFactory;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.ras.ResultArchiveStoreFileStore;
import dev.galasa.framework.spi.teststructure.TestStructure;
import dev.galasa.ras.couchdb.internal.operations.CouchdbDeleteRunService;
import dev.galasa.ras.couchdb.internal.pojos.TestStructureCouchdb;

public class CouchdbRunResult implements IRunResult {

    private final TestStructureCouchdb   testStructure;
    private final CouchdbRasStore store;
    private final CouchdbDirectoryService storeService;
    private final CouchdbDeleteRunService deleteRunService;
    private Path path;

    private final Log logger;
    private final LogFactory logFactory;

    public CouchdbRunResult(CouchdbRasStore store, TestStructureCouchdb testStructure, LogFactory logFactory) {
        this.logFactory = logFactory;
        this.logger = logFactory.getLog(getClass());
        logger.info("CouchdbRunResult being constructed.");

        this.store = store;
        this.deleteRunService = new CouchdbDeleteRunService(this.store);
        this.storeService = (CouchdbDirectoryService) store.getDirectoryServices().get(0);
        if (testStructure == null) {
            this.testStructure = new TestStructureCouchdb();
        } else {
            this.testStructure = testStructure;
        }

        // Create an empty artifact filesystem and set the artifacts path to the root of this filesystem
        ResultArchiveStoreFileStore fileStore = new ResultArchiveStoreFileStore();
        this.path = new CouchdbRasFileSystemProvider(fileStore, store, logFactory).getRoot();
    }

    @Override
    public TestStructure getTestStructure() throws ResultArchiveStoreException {
        return this.testStructure;
    }

    @Override
    public Path getArtifactsRoot() throws ResultArchiveStoreException {
        return this.path;
    }

    @Override
    public String getLog() throws ResultArchiveStoreException {
        return this.store.getLog(this.testStructure);
    }

	@Override
	public void discard() throws ResultArchiveStoreException {
        deleteRunService.discardRun(this.testStructure);
	}

    @Override
    public String getRunId() {
        return "cdb-" + this.testStructure._id;
    }

    @Override
    public void loadArtifacts() throws ResultArchiveStoreException {
        logger.info("CouchdbRunResult loadArtifacts called...");
        this.path = storeService.getRunArtifactPath(this.testStructure);
        logger.info("CouchdbRunResult loadArtifacts exiting...");
    }

}
