/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
/**
 * Selenium Manager
 * 
 * @galasa.manager Selenium
 * 
 * @galasa.release.state BETA - This Manager is almost ready.  It has been tested and the TPI is stable, but there may be minor changes to come.
 * 
 * @galasa.description
 * 
 * This Manager enables the test to run Selenium WebDrivers in order to drive Web Browsers during the test. Browsers can have actions performed against them 
 * to navigate WebPages and extract information about the current page.
 * <br><br>
 * As an absolute minimum, the CPS property <br>
 * <code>selenium.instance.PRIMARY.gecko.path</code><br>
 * must be provided as the Manager will default to using a GECKO WebDriver if no WebDriver is provided.
 * <br><br>
 * The CPS property <br>
 * <code>selenium.instance.PRIMARY.web.driver</code><br>
 * can be used to set a different WebDriver. This will also require the corresponding driver path to be set. <br>
 * eg. <code>selenium.instance.PRIMARY.web.driver=CHROME</code><br>
 * requires <code>selenium.instance.PRIMARY.chrome.path=...</code><br>
 * 
 * @galasa.limitations
 * 
 * The Selenium Manager only supports GECKO, CHROME, EDGE and IE WebDrivers.<br><br>
 *
 * You can view the <a href="https://javadoc.galasa.dev/dev/galasa/selenium/package-summary.html">Javadoc documentation for the Manager here</a>.
 * <br><br>
 * 
 */
package dev.galasa.selenium;
