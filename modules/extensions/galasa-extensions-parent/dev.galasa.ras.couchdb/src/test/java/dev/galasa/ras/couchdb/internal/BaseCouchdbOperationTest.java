/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ras.couchdb.internal;

import java.util.ArrayList;

import dev.galasa.ras.couchdb.internal.pojos.TestStructureCouchdb;

public abstract class BaseCouchdbOperationTest {

    protected TestStructureCouchdb createRunTestStructure(String runId, String runName) {
        TestStructureCouchdb mockTestStructure = new TestStructureCouchdb();
        mockTestStructure._id = runId;
        mockTestStructure._rev = "this-is-a-revision";
        mockTestStructure.setRunName(runName);
        mockTestStructure.setArtifactRecordIds(new ArrayList<>());
        mockTestStructure.setLogRecordIds(new ArrayList<>());
        return mockTestStructure;
    }
}
