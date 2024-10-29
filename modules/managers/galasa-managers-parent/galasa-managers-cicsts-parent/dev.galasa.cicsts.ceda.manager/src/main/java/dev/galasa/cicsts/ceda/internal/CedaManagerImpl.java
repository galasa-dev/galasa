/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.ceda.internal;

import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.cicsts.CedaManagerException;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICeda;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ceda.internal.properties.CedaPropertiesSingleton;
import dev.galasa.cicsts.ceda.spi.ICedaManagerSpi;
import dev.galasa.cicsts.spi.ICedaProvider;
import dev.galasa.cicsts.spi.ICicstsManagerSpi;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.language.GalasaTest;

@Component(service = { IManager.class })
public class CedaManagerImpl extends AbstractManager implements ICedaManagerSpi,ICedaProvider {

	private ICicstsManagerSpi cicstsManager;
	HashMap<ICicsRegion, ICeda> regionCeda = new HashMap<>();

	protected static final String NAMESPACE = "ceda";

	/* (non-Javadoc)
	 * @see dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
	 */
	@Override
	public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
		super.initialise(framework, allManagers, activeManagers, galasaTest);
		try {
			CedaPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
		} catch (ConfigurationPropertyStoreException e) {
			throw new CedaManagerException("Unable to request framework services", e);
		}

		if(galasaTest.isJava()) {

			youAreRequired(allManagers, activeManagers, galasaTest);

		}
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

		cicstsManager = addDependentManager(allManagers,activeManagers,galasaTest, ICicstsManagerSpi.class);
		if(cicstsManager == null) {
			throw new CicstsManagerException("CICS Manager is not available");
		}

		cicstsManager.registerCedaProvider(this);
	}

	@Override
	public @NotNull ICeda getCeda(ICicsRegion cicsRegion) {

		ICeda ceda = regionCeda.get(cicsRegion);
		if(ceda==null) {

			ceda = new CedaImpl(cicsRegion);
			regionCeda.put(cicsRegion, ceda);

		}

		return ceda;
	}


}