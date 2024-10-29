/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.phoenix2.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.phoenix2.internal.Phoenix2ManagerException;

public class Phoenix2DefaultBuildLevel extends CpsProperties {
   public static String get() throws Phoenix2ManagerException{
      try {
         return getStringNulled(Phoenix2PropertiesSingleton.cps(),"default", "build.level");
      }catch(ConfigurationPropertyStoreException e) {
         throw new Phoenix2ManagerException("Failed to access the CPS to find the default build.level");
      }
   }
}
