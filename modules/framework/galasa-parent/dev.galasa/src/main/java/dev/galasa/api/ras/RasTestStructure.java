/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.api.ras;

import java.time.Instant;
import java.util.List;


public class RasTestStructure {
   
   private String runName;
   private String bundle;
   private String testName;
   private String testShortName;
   private String requestor;
   private String status;
   private String result;
   private String group;
   private String submissionId;
   private Instant queued;
   private Instant startTime;
   private Instant endTime;
   private List<RasTestMethod> methods;
   
   public RasTestStructure(String runName, String bundle, String testName, String testShortName, String requestor,
         String status, String result, Instant queued, Instant startTime, Instant endTime, List<RasTestMethod> methods,
         String group, String submissionId
   ) {
      this.runName = runName;
      this.bundle = bundle;
      this.testName = testName;
      this.testShortName = testShortName;
      this.requestor = requestor;
      this.status = status;
      this.result = result;
      this.queued = queued;
      this.startTime = startTime;
      this.endTime = endTime;
      this.methods = methods;
      this.group = group;
      this.submissionId = submissionId;
   }

   public String getRunName() {
      return runName;
   }

   public void setRunName(String runName) {
      this.runName = runName;
   }

   public String getBundle() {
      return bundle;
   }

   public void setBundle(String bundle) {
      this.bundle = bundle;
   }

   public String getTestName() {
      return testName;
   }

   public void setTestName(String testName) {
      this.testName = testName;
   }

   public String getTestShortName() {
      return testShortName;
   }

   public void setTestShortName(String testShortName) {
      this.testShortName = testShortName;
   }

   public String getRequestor() {
      return requestor;
   }

   public void setRequestor(String requestor) {
      this.requestor = requestor;
   }

   public String getGroup() {
      return group;
   }

   public void setGroup(String group) {
      this.group = group;
   }

   public String getSubmissionId() {
      return this.submissionId;
   }

   public void setSubmissionId(String submissionId) {
      this.submissionId = submissionId;
   }

   public String getStatus() {
      return status;
   }

   public void setStatus(String status) {
      this.status = status;
   }

   public String getResult() {
      return result;
   }

   public void setResult(String result) {
      this.result = result;
   }

   public Instant getQueued() {
      return queued;
   }

   public void setQueued(Instant queued) {
      this.queued = queued;
   }

   public Instant getStartTime() {
      return startTime;
   }

   public void setStartTime(Instant startTime) {
      this.startTime = startTime;
   }

   public Instant getEndTime() {
      return endTime;
   }

   public void setEndTime(Instant endTime) {
      this.endTime = endTime;
   }

   public List<RasTestMethod> getMethods() {
      return methods;
   }

   public void setMethods(List<RasTestMethod> methods) {
      this.methods = methods;
   }
   
   
}
