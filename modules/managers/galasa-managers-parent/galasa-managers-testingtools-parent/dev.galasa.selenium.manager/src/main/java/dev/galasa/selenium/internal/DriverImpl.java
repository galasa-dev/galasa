/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.selenium.internal;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriver;

import dev.galasa.selenium.IWebPage;
import dev.galasa.selenium.SeleniumManagerException;

/**
 * Superclass for the local and remote driver implementations for tracking and discarding webpages
 * 
 *  
 *
 */
public class DriverImpl {
	private List<IWebPage> webPages = new ArrayList<>();
	 	 
	public IWebPage allocatePage(SeleniumManagerImpl seleniumManager, WebDriver driver, String url, Path screenshotRasDirectory)
			throws SeleniumManagerException {
		WebPageImpl webPage = new WebPageImpl(seleniumManager, driver, webPages, screenshotRasDirectory);

		if (url != null && !url.trim().isEmpty())
			webPage.get(url);

		this.webPages.add(webPage);
		return webPage;
	}
	 	 
	public void discardPages() {
		for (IWebPage page : webPages) {
            page.close();
        }
	}	
	
	public List<IWebPage> getPages() {
		return this.webPages;
	}
}