/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.api.ras;

public class RasArtifact {
   
   private String artifactPath;
   private String contentType;
   
   public RasArtifact(String artifactPath, String contentType) {
      this.artifactPath = artifactPath;
      this.contentType = contentType;
   }

   public String getArtifactPath() {
      return artifactPath;
   }

   public void setArtifactPath(String artifactPath) {
      this.artifactPath = artifactPath;
   }

   public String getContentType() {
      return contentType;
   }

   public void setContentType(String contentType) {
      this.contentType = contentType;
   }
   
   
}
