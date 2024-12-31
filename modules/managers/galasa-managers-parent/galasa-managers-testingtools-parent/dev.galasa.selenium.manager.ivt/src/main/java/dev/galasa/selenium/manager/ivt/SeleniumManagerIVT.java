/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.selenium.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.logging.Log;
import org.openqa.selenium.Keys;

import dev.galasa.Test;
import dev.galasa.core.manager.Logger;
import dev.galasa.selenium.Browser;
import dev.galasa.selenium.IChromeOptions;
import dev.galasa.selenium.IEdgeOptions;
import dev.galasa.selenium.IFirefoxOptions;
import dev.galasa.selenium.IWebDriver;
import dev.galasa.selenium.IWebPage;
import dev.galasa.selenium.SeleniumManagerException;
import dev.galasa.selenium.WebDriver;

@Test
public class SeleniumManagerIVT {

    @Logger
    public Log logger;
    
    @WebDriver (browser = Browser.CHROME)
    public IWebDriver driverChrome;

    @WebDriver (browser = Browser.FIREFOX)
    public IWebDriver driverFirefox;

    @WebDriver (browser = Browser.EDGE)
    public IWebDriver driverEdge;

    public static final String DUCKDUCKGO_WEBSITE = "https://duckduckgo.com";
    public static final String WEBSITE_GALASA_GITHUB = "https://github.com/galasa-dev";
    public static final String DUCKDUCKGO_TITLE = "DuckDuckGo";
    public static final String VALUE = "value";
    public static final String DUCKDUCKGO_SEARCH_ID = "searchbox_input";

    // Test Broswers support
    @Test
    public void testChromeOptionsCanBeUsed() throws SeleniumManagerException {
    	IChromeOptions options = driverChrome.getChromeOptions();
        IWebPage page = driverChrome.allocateWebPage(DUCKDUCKGO_WEBSITE, options);
        page.maximize().takeScreenShot();
        assertThat(page.getTitle()).containsOnlyOnce(DUCKDUCKGO_TITLE);
        page.quit();
    }
    
    @Test
    public void testFirefoxOptionsCanBeUsed() throws SeleniumManagerException {
    	IFirefoxOptions options = driverFirefox.getFirefoxOptions();
        IWebPage page = driverFirefox.allocateWebPage(DUCKDUCKGO_WEBSITE, options);
        page.maximize().takeScreenShot();
        assertThat(page.getTitle()).containsOnlyOnce(DUCKDUCKGO_TITLE);
        page.quit();
    }
    
    @Test
    public void testEdgeOptionsCanBeUsed() throws SeleniumManagerException {
    	IEdgeOptions options = driverEdge.getEdgeOptions();
        IWebPage page = driverEdge.allocateWebPage(DUCKDUCKGO_WEBSITE, options);
        page.maximize().takeScreenShot();
        assertThat(page.getTitle()).containsOnlyOnce(DUCKDUCKGO_TITLE);
        page.quit();
    }
    
    @Test
    public void testChromeArguments() throws SeleniumManagerException {
    	IChromeOptions options = driverChrome.getChromeOptions();
    	options.addArguments("--ignore-ssl-errors=yes");
    	IWebPage page = driverChrome.allocateWebPage(DUCKDUCKGO_WEBSITE, options);
    	page.takeScreenShot();
    	page.quit();
    }
    
    @Test
    public void testFirefoxArguments() throws SeleniumManagerException {
    	IFirefoxOptions options = driverFirefox.getFirefoxOptions();
    	options.addArguments("--ignore-ssl-errors=yes");
    	IWebPage page = driverFirefox.allocateWebPage(DUCKDUCKGO_WEBSITE, options);
    	page.takeScreenShot();
    	page.quit();
    }

    // Some basic Tests
    @Test
    public void sendingKeysAndClearingFields() throws SeleniumManagerException {
        IWebPage page = driverFirefox.allocateWebPage(DUCKDUCKGO_WEBSITE);
        page.maximize().takeScreenShot();
        assertThat(page.getTitle()).containsOnlyOnce(DUCKDUCKGO_TITLE);
        assertThat(page.findElementById(DUCKDUCKGO_SEARCH_ID).getDomAttribute(VALUE)).isEmpty();
        page.sendKeysToElementById(DUCKDUCKGO_SEARCH_ID, "galasa");
        assertThat(page.findElementById(DUCKDUCKGO_SEARCH_ID).getDomAttribute(VALUE)).isEqualTo("galasa");
        page.clearElementById(DUCKDUCKGO_SEARCH_ID);
        assertThat(page.findElementById(DUCKDUCKGO_SEARCH_ID).getDomAttribute(VALUE)).isEmpty();
        page.quit();
    }
   

    @Test
    public void clickingFields() throws SeleniumManagerException {
        IWebPage page = driverFirefox.allocateWebPage(WEBSITE_GALASA_GITHUB);
        page.maximize().takeScreenShot();
        assertThat(page.getTitle()).containsOnlyOnce("galasa");
        page.sendKeysToElementByCssSelector(".auto-search-input", "galasa")
            .clickElementByLinkText("galasa").takeScreenShot()
            .waitForElementByLinkText("modules");
        assertThat(page.getTitle()).contains("The Galasa source code");
        page.quit();
    }

    @Test
    public void navigateGalasaGithub() throws SeleniumManagerException {
        IWebPage page = driverFirefox.allocateWebPage(DUCKDUCKGO_WEBSITE);
        page.maximize().takeScreenShot();
        assertThat(page.getTitle()).containsOnlyOnce(DUCKDUCKGO_TITLE);
        page.takeScreenShot().sendKeysToElementById(DUCKDUCKGO_SEARCH_ID, "galasa dev github").takeScreenShot()
            .sendKeysToElementById(DUCKDUCKGO_SEARCH_ID, Keys.RETURN).takeScreenShot()
            .clickElementByPartialLinkText("The Galasa source code").takeScreenShot();
        assertThat(page.findElementsByLinkText("tools")).isNotEmpty();
        page.quit();
    }
    
    @Test
    public void testNotCleaningUp() throws SeleniumManagerException {
        IWebPage page = driverChrome.allocateWebPage(DUCKDUCKGO_WEBSITE);
        page.takeScreenShot();
        page.maximize();
    }

}