/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.selenium;

import java.security.SecureRandom;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.selenium.internal.properties.SeleniumAvailableDrivers;
import dev.galasa.selenium.internal.properties.SeleniumDockerNodeVersion;

/**
 * Specifies the supported browser types for Local and remote versions
 * 
 *  
 *
 */
public enum Browser {
    FIREFOX,
    OPERA,
    IE, 
    CHROME, 
    EDGE,
    ANYAVAIALBLE,
    NOTSPECIFIED;
	
	private Browser selected;

	/**
	 * The image names for the supported driver types from the offical Selenium docker repo.
	 * 
	 * Supported: Firefox, Chrome, Opera, Edge
	 * @return Name of image for each driver
	 * @throws SeleniumManagerException
	 */
    public String getDockerImageName() throws SeleniumManagerException{
        String version = SeleniumDockerNodeVersion.get();
     
        switch (this) {
            case FIREFOX:
                return "selenium/standalone-firefox:"+version;
            case OPERA:
                return "selenium/standalone-opera:"+version;
            case CHROME:
                return "selenium/standalone-chrome:"+version;
            case EDGE:
                return "selenium/standalone-edge:"+version;
            case ANYAVAIALBLE:
            	if (selected == null) {
            		selectAvailableDriver();
            	}
            	return selected.getDockerImageName();
            case NOTSPECIFIED:
            	return null;
            	
            default:
                throw new SeleniumManagerException("Unsupported browser. Available docker nodes: Firefox, Chrome, Opera, Edge");
        }
    }
    
    /**
     * The capability name for each driver type is returned
     * 
     * @return
     * @throws SeleniumManagerException
     */
    public String getDriverName() throws SeleniumManagerException {
        switch (this) {
            case FIREFOX:
                return "firefox";
            case OPERA:
                return "opera";
            case CHROME:
                return "chrome";
            case EDGE:
                return "MicrosoftEdge";
            case IE:
                return "internet explorer";
            case ANYAVAIALBLE:
            	if (selected == null) {
            		selectAvailableDriver();
            	}
            	return selected.getDriverName();
            case NOTSPECIFIED:
            	return null;
            default:
                throw new SeleniumManagerException("Unsupported driver name.");
        }
    }
    
    /**
     * Choose a random driver from the available drivers, if not specified.
     * 
     * @throws SeleniumManagerException
     */
    private void selectAvailableDriver() throws SeleniumManagerException {
    	try {
    		String[] availabledrivers = SeleniumAvailableDrivers.get();
            if (availabledrivers.length < 1) {
              throw new SeleniumManagerException("No available drivers");
            }
            SecureRandom rand = new SecureRandom();
            this.selected = Browser.valueOf(availabledrivers[rand.nextInt(availabledrivers.length)]);
    	} catch (ConfigurationPropertyStoreException e) {
    		throw new SeleniumManagerException("Failed to find avilable drivers", e); 
    	}
    }
    
}