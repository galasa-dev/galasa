/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.selenium;

import java.util.List;

/**
 * This interface is being deprecated and replaced with a more appropriately named IWebDriver interface.
 * 
 *  
 *
 */
@Deprecated
public interface ISeleniumManager {
	 /**
     * Allocate a new WebPage
     * @throws SeleniumManagerException
     */
    public IWebPage allocateWebPage() throws SeleniumManagerException;

    /**
     * Allocate a new WebPage for a provided URL
     * @throws SeleniumManagerException
     */
    public IWebPage allocateWebPage(String url) throws SeleniumManagerException;

    /**
     * Allocate a new WebPage for a provided URL with Firefox Options
     * @throws SeleniumManagerException
     */
    public IWebPage allocateWebPage(String url, IFirefoxOptions options) throws SeleniumManagerException;

    /**
     * Allocate a new WebPage for a provided URL with Chrome Options
     * @throws SeleniumManagerException
     */
    public IWebPage allocateWebPage(String url, IChromeOptions options) throws SeleniumManagerException;

    /**
     * Allocate a new WebPage for a provided URL with Edge Options
     * @throws SeleniumManagerException
     */
    public IWebPage allocateWebPage(String url, IEdgeOptions options) throws SeleniumManagerException;

    /**
     * Allocate a new WebPage for a provided URL with Internet Explorer Options
     * @throws SeleniumManagerException
     */
    public IWebPage allocateWebPage(String url, IInternetExplorerOptions options) throws SeleniumManagerException;

    /**
     * Allocate a new WebPage for a provided URL with Opera Options
     * @throws SeleniumManagerException
     */
    public IWebPage allocateWebPage(String url, IOperaOptions options) throws SeleniumManagerException;

    /**
     * Creates a new interface to the Firefox properties that can be set.
     * @return IFirefoxOptions
     */
    public IFirefoxOptions getFirefoxOptions();

    /**
     * Creates a new interface to the Chrome properties that can be set.
     * @return IChromeOptions
     */
    public IChromeOptions getChromeOptions();

    /**
     * Creates a new interface to the Edge properties that can be set.
     * @return IEdgeOptions
     */
    public IEdgeOptions getEdgeOptions();

    /**
     * Creates a new interface to the InternetExplorer properties that can be set.
     * @return IInternetExplorerOptions
     */
    public IInternetExplorerOptions getInternetExplorerOptions();  
    
    /**
     * Return the active pages
     * @return List<IWebPage>
     */
    public List<IWebPage> getPages();
    
    /**
     * Cycle through any pages and quit
     */
    public void discard();
}