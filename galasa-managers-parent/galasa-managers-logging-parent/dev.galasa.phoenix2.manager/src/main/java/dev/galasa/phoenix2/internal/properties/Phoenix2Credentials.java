/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.phoenix2.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.phoenix2.internal.Phoenix2ManagerException;

public class Phoenix2Credentials extends CpsProperties {
   public static String get() throws Phoenix2ManagerException{
      try {
         String credentialsKey = getStringNulled(Phoenix2PropertiesSingleton.cps(),"endpoint", "credentials");
         
         if(credentialsKey == null) {
            return "PHOENIX2";
         }
         
         return credentialsKey;
      }catch(ConfigurationPropertyStoreException e) {
         throw new Phoenix2ManagerException("Failed to access the CPS to find the credentials id");
      }
   }
}
