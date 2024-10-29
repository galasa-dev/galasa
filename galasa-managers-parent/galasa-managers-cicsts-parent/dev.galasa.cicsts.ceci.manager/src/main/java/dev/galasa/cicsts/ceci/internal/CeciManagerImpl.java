/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.ceci.internal;

import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.cicsts.CeciManagerException;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICeci;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ceci.internal.properties.CeciPropertiesSingleton;
import dev.galasa.cicsts.ceci.spi.ICeciManagerSpi;
import dev.galasa.cicsts.spi.ICeciProvider;
import dev.galasa.cicsts.spi.ICicstsManagerSpi;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;

@Component(service = { IManager.class })
public class CeciManagerImpl extends AbstractManager implements ICeciManagerSpi, ICeciProvider {
    
    protected static final String NAMESPACE = "ceci";
    private ICicstsManagerSpi cicstsManager;
    
    protected HashMap<ICicsRegion, ICeci> regionCecis = new HashMap<>();
    
    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        try {
            CeciPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new CeciManagerException("Unable to request framework services", e);
        }

        if(galasaTest.isJava()) {
            youAreRequired(allManagers, activeManagers, galasaTest);
        }
    }

    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#provisionGenerate()
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        generateAnnotatedFields(CeciManagerField.class);
    }


    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#youAreRequired()
     */
    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);
        
        cicstsManager = addDependentManager(allManagers, activeManagers, galasaTest, ICicstsManagerSpi.class);
        if(cicstsManager == null) {
           throw new CicstsManagerException("CICS Manager is not available");
        }
        
        cicstsManager.registerCeciProvider(this);

    }
    
    @Override
    public @NotNull ICeci getCeci(ICicsRegion cicsRegion) {
        ICeci ceci = this.regionCecis.get(cicsRegion);
        if (ceci == null) {
            ceci = new CeciImpl(this, cicsRegion);
            this.regionCecis.put(cicsRegion, ceci);
        }
        return ceci;
    }
}
