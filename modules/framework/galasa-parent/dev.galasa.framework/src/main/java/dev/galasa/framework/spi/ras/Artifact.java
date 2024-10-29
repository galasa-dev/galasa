/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.ras;

import java.nio.file.Path;

public class Artifact {
   
   private Path artifactPath;
   private String contentType;
   
   public Artifact(Path artifactPath, String contentType) {
      this.artifactPath = artifactPath;
      this.contentType = contentType;
   }
   
   public void setContentType(String contentType) {
      this.contentType = contentType;
   }
   
   public void setArtifactPath(Path artifactPath) {
      this.artifactPath = artifactPath;
   }
   
   public Path getArtifactPath() {
      return this.artifactPath;
   }
   
   public String getContentType() {
      return this.contentType;
   }
   
   
}
