/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.selenium.internal;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Navigation;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import dev.galasa.ResultArchiveStoreContentType;
import dev.galasa.SetContentType;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.selenium.IWebPage;
import dev.galasa.selenium.SeleniumManagerException;

public class WebPageImpl implements IWebPage {

	private SeleniumManagerImpl selMan;
    private WebDriver driver;
    private List<IWebPage> webPages;
    private Path screenshotRasDirectory;

    public static final int DEFAULT_SECONDS_TIMEOUT = 30;

    public WebPageImpl(SeleniumManagerImpl selMan, WebDriver driver, List<IWebPage> webPages, Path screenshotRasDirectory) {
        this.selMan = selMan;
    	this.driver = driver;
        this.webPages = webPages;
        this.screenshotRasDirectory = screenshotRasDirectory;
    }

    @Override
    public void close() {
        driver.close();
    }

    @Override
    public IWebPage clearElementByClassName(String className) {
        return clearElement(By.className(className), DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public IWebPage clearElementByClassName(String className, int secondsTimeout) {
        return clearElement(By.className(className), secondsTimeout);
    }

    @Override
    public IWebPage clearElementByCssSelector(String selector) {
        return clearElement(By.cssSelector(selector), DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public IWebPage clearElementByCssSelector(String selector, int secondsTimeout) {
        return clearElement(By.cssSelector(selector), secondsTimeout);
    }

    @Override
    public IWebPage clearElementById(String id) {
        return clearElement(By.id(id), DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public IWebPage clearElementById(String id, int secondsTimeout) {
        return clearElement(By.id(id), secondsTimeout);
    }

    @Override
    public IWebPage clearElementByLinkText(String linkText) {
        return clearElement(By.linkText(linkText), DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public IWebPage clearElementByLinkText(String linkText, int secondsTimeout) {
        return clearElement(By.linkText(linkText), secondsTimeout);
    }

    @Override
    public IWebPage clearElementByName(String name) {
        return clearElement(By.name(name), DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public IWebPage clearElementByName(String name, int secondsTimeout) {
        return clearElement(By.name(name), secondsTimeout);
    }

    @Override
    public IWebPage clearElementByPartialLinkText(String linkText) {
        return clearElement(By.partialLinkText(linkText), DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public IWebPage clearElementByPartialLinkText(String linkText, int secondsTimeout) {
        return clearElement(By.partialLinkText(linkText), secondsTimeout);
    }

    @Override
    public IWebPage clearElementByTagName(String name) {
        return clearElement(By.tagName(name), DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public IWebPage clearElementByTagName(String name, int secondsTimeout) {
        return clearElement(By.tagName(name), secondsTimeout);
    }

    @Override
    public IWebPage clearElementByXpath(String xpathExpression) {
        return clearElement(By.xpath(xpathExpression), DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public IWebPage clearElementByXpath(String xpathExpression, int secondsTimeout) {
        return clearElement(By.xpath(xpathExpression), secondsTimeout);
    }

    @Override
    public IWebPage clearElement(By by) {
        return clearElement(by, DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public IWebPage clearElement(By by, int secondsTimeout) {
        waitForElement(by, secondsTimeout).clear();
        return this;
    }

    @Override
    public IWebPage clickElementByClassName(String className) {
        return clickElement(By.className(className), DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public IWebPage clickElementByClassName(String className, int secondsTimeout) {
        return clickElement(By.className(className), secondsTimeout);
    }

    @Override
    public IWebPage clickElementByCssSelector(String selector) {
        return clickElement(By.cssSelector(selector), DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public IWebPage clickElementByCssSelector(String selector, int secondsTimeout) {
        return clickElement(By.cssSelector(selector), secondsTimeout);
    }

    @Override
    public IWebPage clickElementById(String id) {
        return clickElement(By.id(id), DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public IWebPage clickElementById(String id, int secondsTimeout) {
        return clickElement(By.id(id), secondsTimeout);
    }

    @Override
    public IWebPage clickElementByLinkText(String linkText) {
        return clickElement(By.linkText(linkText), DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public IWebPage clickElementByLinkText(String linkText, int secondsTimeout) {
        return clickElement(By.linkText(linkText), secondsTimeout);
    }

    @Override
    public IWebPage clickElementByName(String name) {
        return clickElement(By.name(name), DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public IWebPage clickElementByName(String name, int secondsTimeout) {
        return clickElement(By.name(name), secondsTimeout);
    }

    @Override
    public IWebPage clickElementByPartialLinkText(String linkText) {
        return clickElement(By.partialLinkText(linkText), DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public IWebPage clickElementByPartialLinkText(String linkText, int secondsTimeout) {
        return clickElement(By.partialLinkText(linkText), secondsTimeout);
    }

    @Override
    public IWebPage clickElementByTagName(String name) {
        return clickElement(By.tagName(name), DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public IWebPage clickElementByTagName(String name, int secondsTimeout) {
        return clickElement(By.tagName(name), secondsTimeout);
    }

    @Override
    public IWebPage clickElementByXpath(String xpathExpression) {
        return clickElement(By.xpath(xpathExpression), DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public IWebPage clickElementByXpath(String xpathExpression, int secondsTimeout) {
        return clickElement(By.xpath(xpathExpression), secondsTimeout);

    }

    @Override
    public IWebPage clickElement(By by) {
        return clickElement(by, DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public IWebPage clickElement(By by, int secondsTimeout) {
        waitForElement(by, secondsTimeout).click();
        return this;
    }

    @Override
    public IWebPage sendKeysToElementByClassName(String className, CharSequence keysToSend) {
        return sendKeysToElement(By.className(className), keysToSend, DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public IWebPage sendKeysToElementByClassName(String className, CharSequence keysToSend, int secondsTimeout) {
        return sendKeysToElement(By.className(className), keysToSend, secondsTimeout);
    }

    @Override
    public IWebPage sendKeysToElementByCssSelector(String selector, CharSequence keysToSend) {
        return sendKeysToElement(By.cssSelector(selector), keysToSend, DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public IWebPage sendKeysToElementByCssSelector(String selector, CharSequence keysToSend, int secondsTimeout) {
        return sendKeysToElement(By.cssSelector(selector), keysToSend, secondsTimeout);
    }

    @Override
    public IWebPage sendKeysToElementById(String id, CharSequence keysToSend) {
        return sendKeysToElement(By.id(id), keysToSend, DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public IWebPage sendKeysToElementById(String id, CharSequence keysToSend, int secondsTimeout) {
        return sendKeysToElement(By.id(id), keysToSend, secondsTimeout);
    }

    @Override
    public IWebPage sendKeysToElementByLinkText(String linkText, CharSequence keysToSend) {
        return sendKeysToElement(By.linkText(linkText), keysToSend, DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public IWebPage sendKeysToElementByLinkText(String linkText, CharSequence keysToSend, int secondsTimeout) {
        return sendKeysToElement(By.linkText(linkText), keysToSend, secondsTimeout);
    }

    @Override
    public IWebPage sendKeysToElementByName(String name, CharSequence keysToSend) {
        return sendKeysToElement(By.name(name), keysToSend, DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public IWebPage sendKeysToElementByName(String name, CharSequence keysToSend, int secondsTimeout) {
        return sendKeysToElement(By.name(name), keysToSend, secondsTimeout);
    }

    @Override
    public IWebPage sendKeysToElementByPartialLinkText(String linkText, CharSequence keysToSend) {
        return sendKeysToElement(By.partialLinkText(linkText), keysToSend, DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public IWebPage sendKeysToElementByPartialLinkText(String linkText, CharSequence keysToSend, int secondsTimeout) {
        return sendKeysToElement(By.partialLinkText(linkText), keysToSend, secondsTimeout);
    }

    @Override
    public IWebPage sendKeysToElementByTagName(String name, CharSequence keysToSend) {
        return sendKeysToElement(By.tagName(name), keysToSend, DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public IWebPage sendKeysToElementByTagName(String name, CharSequence keysToSend, int secondsTimeout) {
        return sendKeysToElement(By.tagName(name), keysToSend, secondsTimeout);
    }

    @Override
    public IWebPage sendKeysToElementByXpath(String xpathExpression, CharSequence keysToSend) {
        return sendKeysToElement(By.xpath(xpathExpression), keysToSend, DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public IWebPage sendKeysToElementByXpath(String xpathExpression, CharSequence keysToSend, int secondsTimeout) {
        return sendKeysToElement(By.xpath(xpathExpression), keysToSend, secondsTimeout);
    }

    @Override
    public IWebPage sendKeysToElement(By by, CharSequence keysToSend) {
        return sendKeysToElement(by, keysToSend, DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public IWebPage sendKeysToElement(By by, CharSequence keysToSend, int secondsTimeout) {
        waitForElement(by, secondsTimeout).sendKeys(keysToSend);
        return this;
    }

    @Override
    public WebElement findElementByClassName(String className) {
        return findElement(By.className(className));
    }

    @Override
    public List<WebElement> findElementsByClassName(String className) {
        return findElements(By.className(className));
    }

    @Override
    public WebElement findElementByCssSelector(String selector) {
        return findElement(By.cssSelector(selector));
    }

    @Override
    public List<WebElement> findElementsByCssSelector(String selector) {
        return findElements(By.cssSelector(selector));
    }

    @Override
    public WebElement findElementById(String id) {
        return findElement(By.id(id));
    }

    @Override
    public List<WebElement> findElementsById(String id) {
        return findElements(By.id(id));
    }

    @Override
    public WebElement findElementByLinkText(String linkText) {
        return findElement(By.linkText(linkText));
    }

    @Override
    public List<WebElement> findElementsByLinkText(String linkText) {
        return findElements(By.linkText(linkText));
    }

    @Override
    public WebElement findElementByName(String name) {
        return findElement(By.name(name));
    }

    @Override
    public List<WebElement> findElementsByName(String name) {
        return findElements(By.name(name));
    }

    @Override
    public WebElement findElementByPartialLinkText(String linkText) {
        return findElement(By.partialLinkText(linkText));
    }

    @Override
    public List<WebElement> findElementsByPartialLinkText(String linkText) {
        return findElements(By.partialLinkText(linkText));
    }

    @Override
    public WebElement findElementByTagName(String name) {
        return findElement(By.tagName(name));
    }

    @Override
    public List<WebElement> findElementsByTagName(String name) {
        return findElements(By.tagName(name));
    }

    @Override
    public WebElement findElementByXpath(String xpathExpression) {
        return findElement(By.xpath(xpathExpression));
    }

    @Override
    public List<WebElement> findElementsByXpath(String xpathExpression) {
        return findElements(By.xpath(xpathExpression));
    }

    @Override
    public WebElement findElement(By by) {
        WebDriverWait wait = new WebDriverWait(this.driver, DEFAULT_SECONDS_TIMEOUT);
        wait.until(ExpectedConditions.presenceOfElementLocated(by));
        return this.driver.findElement(by);
    }

    @Override
    public List<WebElement> findElements(By by) {
        WebDriverWait wait = new WebDriverWait(this.driver, DEFAULT_SECONDS_TIMEOUT);
        wait.until(ExpectedConditions.presenceOfElementLocated(by));
        return this.driver.findElements(by);
    }

    @Override
    public IWebPage get(String url) {
        this.driver.get(url);
        return this;
    }

    @Override
    public String getCurrentUrl() {
        return this.driver.getCurrentUrl();
    }

    @Override
    public String getPageSource() {
        return this.driver.getPageSource();
    }

    @Override
    public String getTitle() {
        return this.driver.getTitle();
    }

    @Override
    public String getWindowHandle() {
        return this.driver.getWindowHandle();
    }

    @Override
    public Set<String> getWindowHandles() {
        return this.driver.getWindowHandles();
    }

    @Override
    public Options manage() {
        return this.driver.manage();
    }

    @Override
    public Navigation navigate() {
        return this.driver.navigate();
    }

    @Override
    public void quit() {
        this.webPages.remove(this);
        this.driver.quit();
    }

    public void managerQuit() {
        this.driver.quit();
    }

    @Override
    public TargetLocator switchTo() {
        return this.driver.switchTo();
    }

    @Override
    public IWebPage switchToFrameByClassName(String className) {
        return switchToFrame(By.className(className));
    }

    @Override
    public IWebPage switchToFrameByCssSelector(String selector) {
        return switchToFrame(By.cssSelector(selector));
    }

    @Override
    public IWebPage switchToFrameById(String id) {
        return switchToFrame(By.id(id));
    }

    @Override
    public IWebPage switchToFrameByLinkText(String linkText) {
        return switchToFrame(By.linkText(linkText));
    }

    @Override
    public IWebPage switchToFrameByName(String name) {
        return switchToFrame(By.name(name));
    }

    @Override
    public IWebPage switchToFrameByPartialLinkText(String linkText) {
        return switchToFrame(By.partialLinkText(linkText));
    }

    @Override
    public IWebPage switchToFrameByTagName(String name) {
        return switchToFrame(By.tagName(name));
    }

    @Override
    public IWebPage switchToFrameByXpath(String xpathExpression) {
        return switchToFrame(By.xpath(xpathExpression));
    }

    @Override
    public IWebPage switchToFrame(By by) {
        switchTo().frame(findElement(by));
        return this;
    }

    @Override
    public WebElement waitForElementByClassName(String className) {
        return waitForElement(By.className(className), DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public WebElement waitForElementByClassName(String className, int secondsTimeout) {
        return waitForElement(By.className(className), secondsTimeout);
    }

    @Override
    public WebElement waitForElementByCssSelector(String selector) {
        return waitForElement(By.cssSelector(selector), DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public WebElement waitForElementByCssSelector(String selector, int secondsTimeout) {
        return waitForElement(By.cssSelector(selector), secondsTimeout);
    }

    @Override
    public WebElement waitForElementById(String id) {
        return waitForElement(By.id(id), DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public WebElement waitForElementById(String id, int secondsTimeout) {
        return waitForElement(By.id(id), secondsTimeout);
    }

    @Override
    public WebElement waitForElementByLinkText(String linkText) {
        return waitForElement(By.linkText(linkText), DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public WebElement waitForElementByLinkText(String linkText, int secondsTimeout) {
        return waitForElement(By.linkText(linkText), secondsTimeout);
    }

    @Override
    public WebElement waitForElementByName(String name) {
        return waitForElement(By.name(name), DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public WebElement waitForElementByName(String name, int secondsTimeout) {
        return waitForElement(By.tagName(name), secondsTimeout);
    }

    @Override
    public WebElement waitForElementByPartialLinkText(String linkText) {
        return waitForElement(By.partialLinkText(linkText), DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public WebElement waitForElementByPartialLinkText(String linkText, int secondsTimeout) {
        return waitForElement(By.partialLinkText(linkText), secondsTimeout);
    }

    @Override
    public WebElement waitForElementByTagName(String tagName) {
        return waitForElement(By.tagName(tagName), DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public WebElement waitForElementByTagName(String tagName, int secondsTimeout) {
        return waitForElement(By.tagName(tagName), secondsTimeout);
    }

    @Override
    public WebElement waitForElementByXpath(String xpathExpression) {
        return waitForElement(By.xpath(xpathExpression), DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public WebElement waitForElementByXpath(String xpathExpression, int secondsTimeout) {
        return waitForElement(By.xpath(xpathExpression), secondsTimeout);
    }

    @Override
    public WebElement waitForElement(By by) {
        return waitForElement(by, DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public WebElement waitForElement(By by, int secondsTimeout) {
        WebDriverWait wait = new WebDriverWait(driver, secondsTimeout);
        wait.until(ExpectedConditions.presenceOfElementLocated(by));
        return findElement(by);
    }

    @Override
    public WebDriverWait driverWait() {
        return driverWait(DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public WebDriverWait driverWait(int secondsTimeout) {
        return new WebDriverWait(driver, secondsTimeout);
    }

    @Override
    public WebDriver getWebDriver() {
        return this.driver;
    }

    @Override
    public IWebPage maximize() {
        manage().window().maximize();
        return this;
    }

    @Override
    public IWebPage waitForPageLoad() {
        return waitForPageLoad(DEFAULT_SECONDS_TIMEOUT);
    }

    @Override
    public IWebPage waitForPageLoad(int secondsTimeout) {
        WebDriverWait wait = new WebDriverWait(driver, secondsTimeout);
        wait.until(webDriver -> 
        String.valueOf("complete".equals(((JavascriptExecutor) webDriver).executeScript("return document.readyState")))
                );
        return this;
    }

    @Override
    public IWebPage takeScreenShot() throws SeleniumManagerException {
        File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        String time = String.valueOf(Instant.now().toEpochMilli());
        try {
            Files.createFile(screenshotRasDirectory.
            		resolve(selMan.getCurrentMethod()).
            		resolve("screenshot_" + time + ".png"));
            try(OutputStream os = Files.newOutputStream(screenshotRasDirectory.resolve(selMan.getCurrentMethod()).resolve("screenshot_" + time + ".png"), new SetContentType(ResultArchiveStoreContentType.PNG))) {
                Files.copy(scrFile.toPath(), os); 
                os.flush();
                os.close();
            }
        } catch (IOException e) {
            throw new SeleniumManagerException("Unable to take screenshot", e);
        }
        return this;
    }

}