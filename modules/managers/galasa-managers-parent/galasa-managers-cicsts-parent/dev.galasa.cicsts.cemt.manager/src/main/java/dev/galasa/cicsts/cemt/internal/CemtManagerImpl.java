/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.cicsts.cemt.internal;

import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.language.GalasaTest;

import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.cicsts.CemtManagerException;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICemt;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.cemt.internal.properties.CemtPropertiesSingleton;
import dev.galasa.cicsts.cemt.spi.ICemtManagerSpi;
import dev.galasa.cicsts.spi.ICemtProvider;
import dev.galasa.cicsts.spi.ICicstsManagerSpi;

@Component(service = { IManager.class })
public class CemtManagerImpl extends AbstractManager implements ICemtManagerSpi, ICemtProvider {
   
   protected static final String NAMESPACE = "cemt";
   private ICicstsManagerSpi cicstsManager;
   private HashMap<ICicsRegion, ICemt> regionCemt = new HashMap<>();
   
   @Override
   public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
       super.initialise(framework, allManagers, activeManagers, galasaTest);
       
       try {
          CemtPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
      } catch (ConfigurationPropertyStoreException e) {
          throw new CemtManagerException("Unable to request framework services", e);
      }
       
       if(galasaTest.isJava()) {
          youAreRequired(allManagers, activeManagers, galasaTest);
       }
   }
   
   
   @Override
   public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException{
      
      if(activeManagers.contains(this)) {
         return;
      }
      
      activeManagers.add(this);
      
      cicstsManager = addDependentManager(allManagers, activeManagers, galasaTest, ICicstsManagerSpi.class);
      
      if(cicstsManager == null) {
         throw new CicstsManagerException("CICS Manager is not available");
      }
      
      cicstsManager.registerCemtProvider(this);
      
   }


   @Override
   public @NotNull ICemt getCemt(ICicsRegion cicsRegion) {
      
      ICemt cemt = regionCemt.get(cicsRegion);
      
      if(cemt == null) {
         cemt = new CemtImpl(cicsRegion);
         regionCemt.put(cicsRegion, cemt);
      }
      
      return cemt;
      
   }
   
}
