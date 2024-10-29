/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.elasticlog.internal.properties;

import dev.galasa.elasticlog.internal.ElasticLogManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

public class ElasticLogCredentials extends CpsProperties {
   public static String get() throws ElasticLogManagerException{
      try {
         String credentialsKey = getStringNulled(ElasticLogPropertiesSingleton.cps(),"auth", "credentials");
         
         if(credentialsKey == null) {
            return "ELASTIC";
         }
         
         return credentialsKey;
      }catch(ConfigurationPropertyStoreException e) {
         throw new ElasticLogManagerException("Failed to access the CPS to find the credential");
      }
   }
}
