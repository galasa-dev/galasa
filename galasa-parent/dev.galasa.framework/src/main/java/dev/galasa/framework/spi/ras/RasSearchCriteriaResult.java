/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.ras;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.teststructure.TestStructure;

public class RasSearchCriteriaResult implements IRasSearchCriteria {
   
   private final String[] results;
   
   public RasSearchCriteriaResult(@NotNull String... results) {
      this.results = results;
   }
   
   @Override
   public boolean criteriaMatched(@NotNull TestStructure structure) {
      
      if(structure == null) {
         return Boolean.FALSE;
      }
      
      if(results != null) {
         for(String result : results) {
             if(result.equals(structure.getResult())){
                 return Boolean.TRUE;
             }
         }
     }
      
      return Boolean.FALSE;
      
      
   }
   

   public String[] getResults() {
       return this.results;
   }
   

}
