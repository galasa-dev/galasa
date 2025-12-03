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
   private String webUiUrl;
   private String restApiUrl;
   
   public RasRunResult(String runId, List<RasArtifact> artifacts, RasTestStructure testStructure, String baseWebUiUrl, String baseServletUrl) {
      this.runId = runId;
      this.artifacts = artifacts;
      this.testStructure = testStructure;
		this.webUiUrl = baseWebUiUrl + "/test-runs/" + runId;
		this.restApiUrl = baseServletUrl + "/ras/runs/" + runId;
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
   
   public String getWebUiUrl(){
        return webUiUrl;
    }

    public void setWebUiUrl(String webUiUrl){
        this.webUiUrl = webUiUrl;
    }

    public String getRestApiUrl(){
        return restApiUrl;
    }

    public void setRestApiUrl(String restApiUrl){
        this.restApiUrl = restApiUrl;
    }
   
}
