/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.selenium.internal;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DssAdd;
import dev.galasa.framework.spi.DssDeletePrefix;
import dev.galasa.framework.spi.DssSwap;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDssAction;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.selenium.Browser;
import dev.galasa.selenium.ISeleniumManager;
import dev.galasa.selenium.IWebDriver;
import dev.galasa.selenium.IWebPage;
import dev.galasa.selenium.SeleniumManagerException;
import dev.galasa.selenium.internal.properties.SeleniumDefaultDriver;
import dev.galasa.selenium.internal.properties.SeleniumDriverMaxSlots;
import dev.galasa.selenium.internal.properties.SeleniumWebDriverType;

/**
 * Selenium Environment is a generic environment for both local and remote drivers
 * 
 * 
 *  
 *
 */
public class SeleniumEnvironment {
	private static final Log logger = LogFactory.getLog(SeleniumEnvironment.class);
	
	private SeleniumManagerImpl seleniumManager;
	private Path screenshotRasDirectory;
		
	private List<String> slots = new ArrayList<>();
	private List<ISeleniumManager> drivers = new ArrayList<>();
	
	private IDynamicStatusStoreService dss;
	private String runName;
	
	private int sessions = 0;
	
	public SeleniumEnvironment(SeleniumManagerImpl seleleniumManager, Path screenshotRasDirectory) throws SeleniumManagerException {
		this.seleniumManager = seleleniumManager;
		this.screenshotRasDirectory = screenshotRasDirectory;
		this.dss = seleleniumManager.getDss();
		this.runName = seleniumManager.getFramework().getTestRunName();
	}
	
	/**
	 * Allocate the driver based on CPS configurations.
	 * 
	 * @param browser
	 * @return
	 * @throws SeleniumManagerException
	 */
	public ISeleniumManager allocateDriver(Browser browser) throws ResourceUnavailableException, SeleniumManagerException {
		ISeleniumManager driver;
		Path driverRasDir = screenshotRasDirectory;
		
		try {
			if (browser.equals(Browser.NOTSPECIFIED)) {
				browser = Browser.valueOf(SeleniumDefaultDriver.get());
			}
			
			switch(SeleniumWebDriverType.get()) {
		    case ("local"):
		    	driver = new LocalDriverImpl(seleniumManager, browser, driverRasDir);
		    	break;
		    default:
		    	// Get a slot or fail
				String slotName = allocateSlot();
		    	driver =  new RemoteDriverImpl(this, seleniumManager, browser, slotName, driverRasDir);
		    	break;
			}
		} catch (ConfigurationPropertyStoreException e) {
			throw new SeleniumManagerException("Failed to fetch Driver type", e);
		}
		drivers.add(driver);
		return driver;

	}
	
	/**
	 * Allocate the driver based on CPS configurations.
	 * 
	 * @param browser
	 * @return
	 * @throws SeleniumManagerException
	 */
	public IWebDriver allocateWebDriver(Browser browser) throws ResourceUnavailableException, SeleniumManagerException {
		IWebDriver driver;
		Path driverRasDir = screenshotRasDirectory;
		
		try {
			if (browser.equals(Browser.NOTSPECIFIED)) {
				browser = Browser.valueOf(SeleniumDefaultDriver.get());
			}
			
			switch(SeleniumWebDriverType.get()) {
		    case ("local"):
		    	driver = new LocalDriverImpl(seleniumManager, browser, driverRasDir);
		    	break;
		    default:
		    	// Get a slot or fail
				String slotName = allocateSlot();
		    	driver =  new RemoteDriverImpl(this, seleniumManager, browser, slotName, driverRasDir);
		    	break;
			}
		} catch (ConfigurationPropertyStoreException e) {
			throw new SeleniumManagerException("Failed to fetch Driver type", e);
		}
		drivers.add(driver);
		return driver;

	}
	
	/**
	 * A selenium specific slot to limit the number of selenium parallel runs.
	 * 
	 * @return
	 * @throws SeleniumManagerException
	 */
	private String allocateSlot() throws ResourceUnavailableException, SeleniumManagerException {
		String slotKey = "driver.current.slots";
		String slots = "";
		String slotName = "";
		int currentSlots = 0;
		
		Exception lastPerformActionsException = null;
		try {
			for (int i=0; i<50;i++) {
				slots = dss.get(slotKey);
				if (slots != null) {
					currentSlots = Integer.valueOf(slots);
				} else {
					slots = "0";
					dss.performActions(
						new DssAdd(slotKey, slots)
					);
					currentSlots = 0;
				}
				
				if (currentSlots >= SeleniumDriverMaxSlots.get()) {
					throw new ResourceUnavailableException("Failed to provsion. No slots avilable");
				}			
				
				String slotNamePrefix = "SeleniumSlot_" + this.runName + "_";
			
				String slotNameAttempt = slotNamePrefix + sessions;
				try {
					currentSlots++;
					dss.performActions(
					new DssAdd("driver.slot." + slotNameAttempt,runName),
					new DssSwap(slotKey, slots, String.valueOf(currentSlots))
					);
					slotName = slotNameAttempt;
					break;
				} catch (DynamicStatusStoreException e) {
					logger.trace("Failed to get slot: " + slotNameAttempt + ". Retrying... ");
					lastPerformActionsException = e;
				}
			}

			if ("".equals(slotName)) {
				throw new SeleniumManagerException("Unable to resolve a slot name", lastPerformActionsException);
			}
			
		} catch (DynamicStatusStoreException | ConfigurationPropertyStoreException e) {
			throw new SeleniumManagerException("Failed to allocate slot", e);
		}
		this.slots.add(slotName);
		sessions++;
		
		return slotName;
	}

	/**
	 * Screenshot all active pages on all active drivers 
	 * 
	 * @throws SeleniumManagerException
	 */
	public void screenShotPages() throws SeleniumManagerException {
		for (ISeleniumManager driver: drivers) {
			for (IWebPage page : driver.getPages()) {
				page.takeScreenShot();
			}
		}
	}

	/**
	 * Discard drivers 
	 * 
	 * @throws SeleniumManagerException
	 */
	public void discard() throws SeleniumManagerException {
		freeSlots();
	}
	
	public void closePages() throws SeleniumManagerException {
		for (ISeleniumManager driver: drivers) {
			driver.discard();
		}
	}
	
	private void freeSlots() throws SeleniumManagerException {
		List<IDssAction> actions = new ArrayList<>();
		try {
			for (String slot: slots) {
				actions.add(new DssDeletePrefix("driver.slot."+slot));
			}
			int newSlots = 0;
			String currentSlots = dss.get("driver.current.slots");
			if (currentSlots != null){
				newSlots = Integer.valueOf(currentSlots)-actions.size();
			} 
			actions.add(new DssSwap("driver.current.slots", currentSlots, String.valueOf(newSlots)));
			
			dss.performActions(actions.toArray(new IDssAction[actions.size()]));
		} catch (DynamicStatusStoreException e) {
			throw new SeleniumManagerException("Failed to clean slots", e);
		}
	}
}
