/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.ras;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.teststructure.TestStructure;

public class RasSearchCriteriaBundle implements IRasSearchCriteria {
   
   private static final String CRITERIA_NAME = "bundle";
   private final String[] bundles;
   
   public RasSearchCriteriaBundle(@NotNull String... bundles) {
      this.bundles = bundles;
   }
   
   @Override
   public boolean criteriaMatched(@NotNull TestStructure structure) {
      
      if(structure == null) {
         return Boolean.FALSE;   
     }
      
     if(bundles != null) {
        for(String bundle : bundles) {
            if(bundle.equals(structure.getBundle())){
                return Boolean.TRUE;
            }
        }
    }
     
     return Boolean.FALSE;
      
   }

   public String[] getBundles() {
       return bundles;
   }

   @Override
   public String getCriteriaName() {
       return CRITERIA_NAME;
   }

   @Override
   public String[] getCriteriaContent() {
        return this.bundles;
   }

}
