/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ras.couchdb.internal.pojos;

public class Artifacts {

    public String runId;   // NOSONAR
    public String runName; // NOSONAR

    // This is here for an experiment, where we add small artifacts inline to the artifacts document to see if it's better than 
    // attaching the binary value of the document.
    public String inlineArtifactData; // NOSONAR

}
