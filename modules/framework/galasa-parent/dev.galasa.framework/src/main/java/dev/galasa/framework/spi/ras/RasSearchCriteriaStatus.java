/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.ras;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class RasSearchCriteriaStatus implements IRasSearchCriteria {
   
   private static final String CRITERIA_NAME = "status";
   private final List<TestRunLifecycleStatus> statuses;
   
   public RasSearchCriteriaStatus(@NotNull List<TestRunLifecycleStatus> statuses) {
      this.statuses = statuses;
   }
   
   @Override
   public boolean criteriaMatched(@NotNull TestStructure structure) {
      
      for(TestRunLifecycleStatus status : statuses) {
            if(status.toString().equals(structure.getStatus())){
               return true;
            }
      }
      return false;
   }

   public List<TestRunLifecycleStatus> getStatuses() {
      return this.statuses;
   }

   public String[] getStatusesAsStrings() {
      List<String> statusesStrings = new ArrayList<String>();
      for (TestRunLifecycleStatus status : this.statuses){
         statusesStrings.add(status.toString());
      }
      return statusesStrings.toArray(new String[0]);
   }

   @Override
   public String getCriteriaName() {
      return CRITERIA_NAME;
   }

   @Override
   public String[] getCriteriaContent() {
      return getStatusesAsStrings();
   }
}