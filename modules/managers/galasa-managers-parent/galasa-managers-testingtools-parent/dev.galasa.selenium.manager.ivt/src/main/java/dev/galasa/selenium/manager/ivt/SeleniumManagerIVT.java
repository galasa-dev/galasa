/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.selenium.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;
// import static org.junit.Assert.assertThat;

import org.apache.commons.logging.Log;

import dev.galasa.Test;
import dev.galasa.core.manager.Logger;
import dev.galasa.selenium.Browser;
import dev.galasa.selenium.IChromeOptions;
import dev.galasa.selenium.IEdgeOptions;
import dev.galasa.selenium.IFirefoxOptions;
import dev.galasa.selenium.IOperaOptions;
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
    
    @WebDriver (browser = Browser.OPERA)
    public IWebDriver driverOpera;

    public static final String WEBSITE = "https://duckduckgo.com";
    public static final String WEBSITEGALASAGITHUB = "https://github.com/galasa-dev";
    public static final String TITLE = "DuckDuckGo";
    public static final String VALUE = "value";
    public static final String SEARCHID = "search_form_input_homepage";

    // Test Broswers support
    @Test
    public void testChromeOptionsCanBeUsed() throws SeleniumManagerException {
    	IChromeOptions options = driverChrome.getChromeOptions();
        IWebPage page = driverChrome.allocateWebPage(WEBSITE, options);
        page.maximize().takeScreenShot();
        assertThat(page.getTitle()).containsOnlyOnce(TITLE);
        page.quit();
    }
    
    @Test
    public void testFirefoxOptionsCanBeUsed() throws SeleniumManagerException {
    	IFirefoxOptions options = driverFirefox.getFirefoxOptions();
        IWebPage page = driverFirefox.allocateWebPage(WEBSITE, options);
        page.maximize().takeScreenShot();
        assertThat(page.getTitle()).containsOnlyOnce(TITLE);
        page.quit();
    }
    
    @Test
    public void testEdgeOptionsCanBeUsed() throws SeleniumManagerException {
    	IEdgeOptions options = driverEdge.getEdgeOptions();
        IWebPage page = driverEdge.allocateWebPage(WEBSITE, options);
        page.maximize().takeScreenShot();
        assertThat(page.getTitle()).containsOnlyOnce(TITLE);
        page.quit();
    }
    
    @Test
    public void testOperaOptionsCanBeUsed() throws SeleniumManagerException {
    	IOperaOptions options = driverOpera.getOperaOptions();
        IWebPage page = driverOpera.allocateWebPage(WEBSITE, options);
        page.maximize().takeScreenShot();
        assertThat(page.getTitle()).containsOnlyOnce(TITLE);
        page.quit();
    }
    
    @Test
    public void testChromeArguments() throws SeleniumManagerException {
    	IChromeOptions options = driverChrome.getChromeOptions();
    	options.addArguments("--ignore-ssl-errors=yes");
    	IWebPage page = driverChrome.allocateWebPage(WEBSITE, options);
    	page.takeScreenShot();
    	page.quit();
    }
    
    @Test
    public void testFirefoxArguments() throws SeleniumManagerException {
    	IFirefoxOptions options = driverFirefox.getFirefoxOptions();
    	options.addArguments("--ignore-ssl-errors=yes");
    	IWebPage page = driverFirefox.allocateWebPage(WEBSITE, options);
    	page.takeScreenShot();
    	page.quit();
    }
    
    @Test
    public void testOperaArguments() throws SeleniumManagerException {
    	IOperaOptions options = driverOpera.getOperaOptions();
    	options.addArguments("--ignore-ssl-errors=yes");
    	IWebPage page = driverOpera.allocateWebPage(WEBSITE, options);
    	page.takeScreenShot();
    	page.quit();
    }
    
    // Some basic Tests
    @Test
    public void sendingKeysAndClearingFields() throws SeleniumManagerException {
        IWebPage page = driverFirefox.allocateWebPage(WEBSITE);
        page.maximize().takeScreenShot();
        assertThat(page.getTitle()).containsOnlyOnce(TITLE);
        assertThat(page.findElementById(SEARCHID).getAttribute(VALUE)).isEmpty();
        page.sendKeysToElementById(SEARCHID, "galasa");
        assertThat(page.findElementById(SEARCHID).getAttribute(VALUE)).isEqualTo("galasa");
        page.clearElementById(SEARCHID);
        assertThat(page.findElementById(SEARCHID).getAttribute(VALUE)).isEmpty();
        page.quit();
    }
   

    @Test
    public void clickingFields() throws SeleniumManagerException {
        IWebPage page = driverFirefox.allocateWebPage(WEBSITE);
        page.maximize().takeScreenShot();
        assertThat(page.getTitle()).containsOnlyOnce(TITLE);
        page.clickElementByCssSelector("a.header__button--menu.js-side-menu-open")
            .clickElementByLinkText("Twitter").takeScreenShot()
            .waitForElementByLinkText("duckduckgo.com");
        assertThat(page.getTitle()).contains("DuckDuckGo (@DuckDuckGo)");
        page.quit();
    }   

    @Test
    public void navigateGalasaGithub() throws SeleniumManagerException {
        IWebPage page = driverFirefox.allocateWebPage(WEBSITE);
        page.maximize().takeScreenShot();
        assertThat(page.getTitle()).containsOnlyOnce(TITLE);
        page.takeScreenShot().sendKeysToElementById(SEARCHID, "galasa dev github").takeScreenShot()
            .clickElementById("search_button_homepage").takeScreenShot()
            .clickElementByLinkText("galasa - GitHub").takeScreenShot()
            .clickElementByPartialLinkText("Repositories").takeScreenShot();
        assertThat(page.findElementsByLinkText("framework")).isNotEmpty();
        page.quit();
    }
    
    @Test
    public void testNotCleaningUp() throws SeleniumManagerException {
        IWebPage page = driverChrome.allocateWebPage(WEBSITE);
        page.takeScreenShot();
        page.maximize();
    }

}