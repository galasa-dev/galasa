/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import java.util.List;

import dev.galasa.framework.spi.ras.Artifact;
import dev.galasa.framework.spi.teststructure.TestStructure;


public class RunResult {
	
    private String runId;
	private TestStructure testStructure;
	private List<Artifact> artifacts;

	public RunResult(String runId, TestStructure testStructure, List<Artifact> artifacts) {
		
	    this.runId = runId;
		this.testStructure = testStructure;
		this.artifacts = artifacts;

	}
	
	public void setRunId(String runId) {
	   this.runId = runId;
	}
	
	public void setTestStructure(TestStructure testStructure) {
	   this.testStructure = testStructure;
	}
	
	public void setArtifacts(List<Artifact> artifacts) {
	   this.artifacts = artifacts;
	}
	
	public String getRunId() {
	   return this.runId;
	}
	
	public TestStructure getTestStructure() {
	   return this.testStructure;
	}
	
	public List<Artifact> getArtifacts(){
	   return this.artifacts;
	}
	
	

}
