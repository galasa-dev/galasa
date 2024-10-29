/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.selenium.internal;

import java.nio.file.Path;

import org.openqa.selenium.WebDriver;

import dev.galasa.selenium.Browser;
import dev.galasa.selenium.IChromeOptions;
import dev.galasa.selenium.IEdgeOptions;
import dev.galasa.selenium.IFirefoxOptions;
import dev.galasa.selenium.IInternetExplorerOptions;
import dev.galasa.selenium.IOperaOptions;
import dev.galasa.selenium.IWebDriver;
import dev.galasa.selenium.IWebPage;
import dev.galasa.selenium.SeleniumManagerException;

/**
 * A local driver can be used to run Selenium tests. This is not support inside an Galasa ecosystem, as the tests run inside 
 * the default engine. Please use the Docker, Kubernetes or Grid implementations for automation runs.
 * 
 *  
 *
 */
public class LocalDriverImpl extends DriverImpl implements IWebDriver{
    private Path screenshotRasDirectory;
    private Browser browser;
    private SeleniumManagerImpl seleniumManager;

    public LocalDriverImpl(SeleniumManagerImpl seleniumManager, Browser browser, Path screenshotRasDirectory) throws SeleniumManagerException {
        this.seleniumManager = seleniumManager;
    	this.screenshotRasDirectory = screenshotRasDirectory;
        this.browser = browser;
    }

    @Override
    public IWebPage allocateWebPage() throws SeleniumManagerException {
        return allocateWebPage(null);
    }

    @Override
    public IWebPage allocateWebPage(String url) throws SeleniumManagerException {

        WebDriver driver = null;

        try {
            driver = LocalBrowser.getWebDriver(this.browser);

            if (driver == null)
                throw new SeleniumManagerException("Unsupported driver type " + browser.getDriverName());
        } catch (SeleniumManagerException e) {
            throw new SeleniumManagerException("Issue provisioning web driver", e);
        }

        return allocatePage(seleniumManager, driver, url, screenshotRasDirectory);
    }

    @Override
    public IWebPage allocateWebPage(String url, IFirefoxOptions options) throws SeleniumManagerException {
        WebDriver driver = null;

        try {
            driver = LocalBrowser.getGeckoDriver(options);

            if (driver == null)
                throw new SeleniumManagerException("Unsupported driver type: " + browser.getDriverName());
        } catch (SeleniumManagerException e) {
            throw new SeleniumManagerException("Issue provisioning web driver", e);
        }

        return allocatePage(seleniumManager, driver, url, screenshotRasDirectory);
    }

    @Override
    public IWebPage allocateWebPage(String url, IChromeOptions options) throws SeleniumManagerException {
        WebDriver driver = null;

        try {
            driver = LocalBrowser.getChromeDriver(((ChromeOptionsImpl)options).get());

            if (driver == null)
                throw new SeleniumManagerException("Unsupported driver type: " + browser.getDriverName());
        } catch (SeleniumManagerException e) {
            throw new SeleniumManagerException("Issue provisioning web driver", e);
        }

        return allocatePage(seleniumManager, driver, url, screenshotRasDirectory);
    }

    @Override
    public IWebPage allocateWebPage(String url, IEdgeOptions options) throws SeleniumManagerException {
        WebDriver driver = null;

        try {
            driver = LocalBrowser.getEdgeDriver(((EdgeOptionsImpl)options).get());

            if (driver == null)
                throw new SeleniumManagerException("Unsupported driver type: " + browser.getDriverName());
        } catch (SeleniumManagerException e) {
            throw new SeleniumManagerException("Issue provisioning web driver", e);
        }

        return allocatePage(seleniumManager, driver, url, screenshotRasDirectory);
    }

    @Override
    public IWebPage allocateWebPage(String url, IInternetExplorerOptions options) throws SeleniumManagerException {
        WebDriver driver = null;

        try {
            driver = LocalBrowser.getIEDriver(((InternetExplorerOptionsImpl)options).get());

            if (driver == null)
                throw new SeleniumManagerException("Unsupported driver type:" + browser.getDriverName());
        } catch (SeleniumManagerException e) {
            throw new SeleniumManagerException("Issue provisioning web driver", e);
        }

        return allocatePage(seleniumManager, driver, url, screenshotRasDirectory);
    }

    @Override
    public IWebPage allocateWebPage(String url, IOperaOptions options) throws SeleniumManagerException {
        WebDriver driver = null;

        try {
            driver = LocalBrowser.getOperaDriver(((OperaOptionsImpl)options).get());

            if (driver == null)
                throw new SeleniumManagerException("Unsupported driver type:" + browser.getDriverName());
        } catch (SeleniumManagerException e) {
            throw new SeleniumManagerException("Issue provisioning web driver", e);
        }

        return allocatePage(seleniumManager, driver, url, screenshotRasDirectory);
    }

    @Override
    public IFirefoxOptions getFirefoxOptions() {
        return new FirefoxOptionsImpl();
    }

    @Override
    public IChromeOptions getChromeOptions() {
        return new ChromeOptionsImpl();
    }

    @Override
    public IEdgeOptions getEdgeOptions() {
        return new EdgeOptionsImpl();
    }
    
    @Override
    public IOperaOptions getOperaOptions() {
        return new OperaOptionsImpl();
    }

    @Override
    public IInternetExplorerOptions getInternetExplorerOptions() {
        return new InternetExplorerOptionsImpl();
    }

    public void discard() {
       discardPages();
    }
}