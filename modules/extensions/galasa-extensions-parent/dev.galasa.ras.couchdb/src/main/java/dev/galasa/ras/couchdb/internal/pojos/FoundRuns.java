/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ras.couchdb.internal.pojos;

import java.util.List;

import dev.galasa.extensions.common.couchdb.pojos.Row;

public class FoundRuns {

    public List<TestStructureCouchdb> docs;    // NOSONAR

    public List<Row>                  rows;    // NOSONAR

    public String                     bookmark;// NOSONAR

    public String                     warning; // NOSONAR

}
