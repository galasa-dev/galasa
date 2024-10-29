/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.api.ras;

import java.util.List;

public class RasRunResult {
   
   private String runId;
   private List<RasArtifact> artifacts;
   private RasTestStructure testStructure;
   
   public RasRunResult(String runId, List<RasArtifact> artifacts, RasTestStructure testStructure) {
      this.runId = runId;
      this.artifacts = artifacts;
      this.testStructure = testStructure;
   }

   public String getRunId() {
      return runId;
   }

   public void setRunId(String runId) {
      this.runId = runId;
   }

   public List<RasArtifact> getArtifacts() {
      return artifacts;
   }

   public void setArtifacts(List<RasArtifact> artifacts) {
      this.artifacts = artifacts;
   }

   public RasTestStructure getTestStructure() {
      return testStructure;
   }

   public void setTestStructure(RasTestStructure testStructure) {
      this.testStructure = testStructure;
   }
   
   
}
