/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.cemt.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.cicsts.CemtManagerException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

@Component(service=CemtPropertiesSingleton.class, immediate=true)
public class CemtPropertiesSingleton {
  
   private static CemtPropertiesSingleton singletonInstance;
   private static void setInstance(CemtPropertiesSingleton instance) {
      singletonInstance = instance;
   }
   
   private IConfigurationPropertyStoreService cps;
   
   @Activate
   public void activate() {
      setInstance(this);
   }
   
   @Deactivate
   public void deactivate() {
      setInstance(null);
   }
   
   public static IConfigurationPropertyStoreService cps() throws CemtManagerException{
      if(singletonInstance != null) {
         return singletonInstance.cps;
      }
      
      throw new CemtManagerException("Attempt to access manager cps before it has been initialised");
   }
   
   public static void setCps(IConfigurationPropertyStoreService cps) throws CemtManagerException{
      if(singletonInstance != null) {
         singletonInstance.cps = cps;
         return;
      }
      
      throw new CemtManagerException("Attempt to set manager cps before instance created");
   }
}
