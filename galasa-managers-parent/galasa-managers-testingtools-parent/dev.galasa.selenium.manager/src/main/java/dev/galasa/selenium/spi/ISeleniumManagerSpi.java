/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.selenium.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.selenium.Browser;
import dev.galasa.selenium.IWebDriver;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.selenium.SeleniumManagerException;

public interface ISeleniumManagerSpi {
    
    @NotNull 
    IWebDriver provisionWebDriver(Browser browser) throws ResourceUnavailableException, SeleniumManagerException;
}