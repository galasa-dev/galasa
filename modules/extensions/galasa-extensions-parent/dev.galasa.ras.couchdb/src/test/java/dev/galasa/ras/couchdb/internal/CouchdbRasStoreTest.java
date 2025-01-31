/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ras.couchdb.internal;

import org.junit.Test;

import dev.galasa.ras.couchdb.internal.mocks.CouchdbTestFixtures;

public class CouchdbRasStoreTest {
    

    CouchdbTestFixtures fixtures = new CouchdbTestFixtures();    

        
    // Creating the Ras store causes the test structure in the couchdb 
    @Test
    public void testCanCreateCouchdbRasStoreOK() throws Exception {

        // See if we can create a store...
        fixtures.createCouchdbRasStore(null);
    }

}