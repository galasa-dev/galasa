/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.selenium;

import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriver.Navigation;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * TPI for the WebPageImpl object provisioned by the Selenium Manager
 */
public interface IWebPage {
  
    /**
     * Close current Window and quit browser if was only Window open
     */
    public void close();

    /**
     * Clears the Element specified by a Class Name
     * @param className The Object used to specify the Element
     * @return The WebPage after the Element is cleared
     */
    public IWebPage clearElementByClassName(String className);

    /**
     * Clears the Element specified by a Class Name
     * @param className The Object used to specify the Element
     * @param secondsTimeout The wait timeout in seconds
     * @return The WebPage after the Element is cleared
     */
    public IWebPage clearElementByClassName(String className, int secondsTimeout);

    /**
     * Clears the Element specified by a Css Selector
     * @param selector The Object used to specify the Element
     * @return The WebPage after the Element is cleared
     */
    public IWebPage clearElementByCssSelector(String selector);

    /**
     * Clears the Element specified by a Css Selector
     * @param selector The Object used to specify the Element
     * @param secondsTimeout The wait timeout in seconds
     * @return The WebPage after the Element is cleared
     */
    public IWebPage clearElementByCssSelector(String selector, int secondsTimeout);

    /**
     * Clears the Element specified by an ID
     * @param id The Object used to specify the Element
     * @return The WebPage after the Element is cleared
     */
    public IWebPage clearElementById(String id);

    /**
     * Clears the Element specified by an ID
     * @param id The Object used to specify the Element
     * @param secondsTimeout The wait timeout in seconds
     * @return The WebPage after the Element is cleared
     */
    public IWebPage clearElementById(String id, int secondsTimeout);

    /**
     * Clears the Element specified by a Link Text
     * @param linkText The Object used to specify the Element
     * @return The WebPage after the Element is cleared
     */
    public IWebPage clearElementByLinkText(String linkText);

    /**
     * Clears the Element specified by a Link Text
     * @param linkText The Object used to specify the Element
     * @param secondsTimeout The wait timeout in seconds
     * @return The WebPage after the Element is cleared
     */
    public IWebPage clearElementByLinkText(String linkText, int secondsTimeout);

    /**
     * Clears the Element specified by a Name
     * @param name The Object used to specify the Element
     * @return The WebPage after the Element is cleared
     */
    public IWebPage clearElementByName(String name);

    /**
     * Clears the Element specified by a Name
     * @param name The Object used to specify the Element
     * @param secondsTimeout The wait timeout in seconds
     * @return The WebPage after the Element is cleared
     */
    public IWebPage clearElementByName(String name, int secondsTimeout);

    /**
     * Clears the Element specified by a Partial Link Text
     * @param linkText The Object used to specify the Element
     * @return The WebPage after the Element is cleared
     */
    public IWebPage clearElementByPartialLinkText(String linkText);

    /**
     * Clears the Element specified by a Partial Link Text
     * @param linkText The Object used to specify the Element
     * @param secondsTimeout The wait timeout in seconds
     * @return The WebPage after the Element is cleared
     */
    public IWebPage clearElementByPartialLinkText(String linkText, int secondsTimeout);

    /**
     * Clears the Element specified by a Tag Name
     * @param name The Object used to specify the Element
     * @return The WebPage after the Element is cleared
     */
    public IWebPage clearElementByTagName(String name);

    /**
     * Clears the Element specified by a Tag Name
     * @param name The Object used to specify the Element
     * @param secondsTimeout The wait timeout in seconds
     * @return The WebPage after the Element is cleared
     */
    public IWebPage clearElementByTagName(String name, int secondsTimeout);

    /**
     * Clears the Element specified by an XPath Expression
     * @param xpathExpression The Object used to specify the Element
     * @return The WebPage after the Element is cleared
     */
    public IWebPage clearElementByXpath(String xpathExpression);

    /**
     * Clears the Element specified by an XPath Expression
     * @param xpathExpression The Object used to specify the Element
     * @param secondsTimeout The wait timeout in seconds
     * @return The WebPage after the Element is cleared
     */
    public IWebPage clearElementByXpath(String xpathExpression, int secondsTimeout);

    /**
     * Clears the Element specified by a By Object
     * @param by The Object used to specify the Element
     * @return The WebPage after the Element is cleared
     */
    public IWebPage clearElement(By by);

    /**
     * Clears the Element specified by a By Object
     * @param by The Object used to specify the Element
     * @param secondsTimeout The wait timeout in seconds
     * @return The WebPage after the Element is cleared
     */
    public IWebPage clearElement(By by, int secondsTimeout);

    /**
     * Clicks the Element specified by a Class Name
     * @param className The Object used to specify the Element
     * @return The WebPage after the Element is clicked
     */
    public IWebPage clickElementByClassName(String className);

    /**
     * Clicks the Element specified by a Class Name
     * @param className The Object used to specify the Element
     * @param secondsTimeout The wait timeout in seconds
     * @return The WebPage after the Element is clicked
     */
    public IWebPage clickElementByClassName(String className, int secondsTimeout);

    /**
     * Clicks the Element specified by a Css Selector
     * @param selector The Object used to specify the Element
     * @return The WebPage after the Element is clicked
     */
    public IWebPage clickElementByCssSelector(String selector);

    /**
     * Clicks the Element specified by a Css Selector
     * @param selector The Object used to specify the Element
     * @param secondsTimeout The wait timeout in seconds
     * @return The WebPage after the Element is clicked
     */
    public IWebPage clickElementByCssSelector(String selector, int secondsTimeout);

    /**
     * Clicks the Element specified by an ID
     * @param id The Object used to specify the Element
     * @return The WebPage after the Element is clicked
     */
    public IWebPage clickElementById(String id);

    /**
     * Clicks the Element specified by an ID
     * @param id The Object used to specify the Element
     * @param secondsTimeout The wait timeout in seconds
     * @return The WebPage after the Element is clicked
     */
    public IWebPage clickElementById(String id, int secondsTimeout);

    /**
     * Clicks the Element specified by a Link Text
     * @param linkText The Object used to specify the Element
     * @return The WebPage after the Element is clicked
     */
    public IWebPage clickElementByLinkText(String linkText);

    /**
     * Clicks the Element specified by a Link Text
     * @param linkText The Object used to specify the Element
     * @param secondsTimeout The wait timeout in seconds
     * @return The WebPage after the Element is clicked
     */
    public IWebPage clickElementByLinkText(String linkText, int secondsTimeout);

    /**
     * Clicks the Element specified by a Name
     * @param name The Object used to specify the Element
     * @return The WebPage after the Element is clicked
     */
    public IWebPage clickElementByName(String name);

    /**
     * Clicks the Element specified by a Name
     * @param name The Object used to specify the Element
     * @param secondsTimeout The wait timeout in seconds
     * @return The WebPage after the Element is clicked
     */
    public IWebPage clickElementByName(String name, int secondsTimeout);

    /**
     * Clicks the Element specified by a Partial Link Text
     * @param linkText The Object used to specify the Element
     * @return The WebPage after the Element is clicked
     */
    public IWebPage clickElementByPartialLinkText(String linkText);

    /**
     * Clicks the Element specified by a Partial Link Text
     * @param linkText The Object used to specify the Element
     * @param secondsTimeout The wait timeout in seconds
     * @return The WebPage after the Element is clicked
     */
    public IWebPage clickElementByPartialLinkText(String linkText, int secondsTimeout);

    /**
     * Clicks the Element specified by a Tag Name
     * @param name The Object used to specify the Element
     * @return The WebPage after the Element is clicked
     */
    public IWebPage clickElementByTagName(String name);

    /**
     * Clicks the Element specified by a Tag Name
     * @param name The Object used to specify the Element
     * @param secondsTimeout The wait timeout in seconds
     * @return The WebPage after the Element is clicked
     */
    public IWebPage clickElementByTagName(String name, int secondsTimeout);

    /**
     * Clicks the Element specified by an XPath Expression
     * @param xpathExpression The Object used to specify the Element
     * @return The WebPage after the Element is clicked
     */
    public IWebPage clickElementByXpath(String xpathExpression);

    /**
     * Clicks the Element specified by an XPath Expression
     * @param xpathExpression The Object used to specify the Element
     * @param secondsTimeout The wait timeout in seconds
     * @return The WebPage after the Element is clicked
     */
    public IWebPage clickElementByXpath(String xpathExpression, int secondsTimeout);

    /**
     * Clicks the Element specified by a By Object
     * @param by The Object used to specify the Element
     * @return The WebPage after the Element is clicked
     */
    public IWebPage clickElement(By by);

    /**
     * Clicks the Element specified by a By Object
     * @param by The Object used to specify the Element
     * @param secondsTimeout The wait timeout in seconds
     * @return The WebPage after the Element is clicked
     */
    public IWebPage clickElement(By by, int secondsTimeout);
    
    /**
     * Send CharSequence of Keys to an Element specified by a Class Name
     * @param className The Object used to specify the Element
     * @param keysToSend The Keys to be sent
     * @return The WebPage after the Keys have been sent
     */
    public IWebPage sendKeysToElementByClassName(String className, CharSequence keysToSend);

    /**
     * Send CharSequence of Keys to an Element specified by a Class Name
     * @param className The Object used to specify the Element
     * @param keysToSend The Keys to be sent
     * @param secondsTimeout The wait timeout in seconds
     * @return The WebPage after the Keys have been sent
     */
    public IWebPage sendKeysToElementByClassName(String className, CharSequence keysToSend,
      int secondsTimeout);

    /**
     * Send CharSequence of Keys to an Element specified by a Css Selector
     * @param selector The Object used to specify the Element
     * @param keysToSend The Keys to be sent
     * @return The WebPage after the Keys have been sent
     */
    public IWebPage sendKeysToElementByCssSelector(String selector, CharSequence keysToSend);

    /**
     * Send CharSequence of Keys to an Element specified by a Css Selector
     * @param selector The Object used to specify the Element
     * @param keysToSend The Keys to be sent
     * @param secondsTimeout The wait timeout in seconds
     * @return The WebPage after the Keys have been sent
     */
    public IWebPage sendKeysToElementByCssSelector(String selector, CharSequence keysToSend,
      int secondsTimeout);

    /**
     * Send CharSequence of Keys to an Element specified by an ID
     * @param id The Object used to specify the Element
     * @param keysToSend The Keys to be sent
     * @return The WebPage after the Keys have been sent
     */
    public IWebPage sendKeysToElementById(String id, CharSequence keysToSend);

    /**
     * Send CharSequence of Keys to an Element specified by an ID
     * @param id The Object used to specify the Element
     * @param keysToSend The Keys to be sent
     * @param secondsTimeout The wait timeout in seconds
     * @return The WebPage after the Keys have been sent
     */
    public IWebPage sendKeysToElementById(String id, CharSequence keysToSend, int secondsTimeout);

    /**
     * Send CharSequence of Keys to an Element specified by a Link Text
     * @param linkText The Object used to specify the Element
     * @param keysToSend The Keys to be sent
     * @return The WebPage after the Keys have been sent
     */
    public IWebPage sendKeysToElementByLinkText(String linkText, CharSequence keysToSend);

    /**
     * Send CharSequence of Keys to an Element specified by a Link Text
     * @param linkText The Object used to specify the Element
     * @param keysToSend The Keys to be sent
     * @param secondsTimeout The wait timeout in seconds
     * @return The WebPage after the Keys have been sent
     */
    public IWebPage sendKeysToElementByLinkText(String linkText, CharSequence keysToSend,
      int secondsTimeout);

    /**
     * Send CharSequence of Keys to an Element specified by a Name
     * @param name The Object used to specify the Element
     * @param keysToSend The Keys to be sent
     * @return The WebPage after the Keys have been sent
     */
    public IWebPage sendKeysToElementByName(String name, CharSequence keysToSend);

    /**
     * Send CharSequence of Keys to an Element specified by a Name
     * @param name The Object used to specify the Element
     * @param keysToSend The Keys to be sent
     * @param secondsTimeout The wait timeout in seconds
     * @return The WebPage after the Keys have been sent
     */
    public IWebPage sendKeysToElementByName(String name, CharSequence keysToSend, int secondsTimeout);

    /**
     * Send CharSequence of Keys to an Element specified by a Partial Link Text
     * @param linkText The Object used to specify the Element
     * @param keysToSend The Keys to be sent
     * @return The WebPage after the Keys have been sent
     */
    public IWebPage sendKeysToElementByPartialLinkText(String linkText, CharSequence keysToSend);

    /**
     * Send CharSequence of Keys to an Element specified by a Partial Link Text
     * @param linkText The Object used to specify the Element
     * @param keysToSend The Keys to be sent
     * @param secondsTimeout The wait timeout in seconds
     * @return The WebPage after the Keys have been sent
     */
    public IWebPage sendKeysToElementByPartialLinkText(String linkText, CharSequence keysToSend,
      int secondsTimeout);

    /**
     * Send CharSequence of Keys to an Element specified by a Tag Name
     * @param name The Object used to specify the Element
     * @param keysToSend The Keys to be sent
     * @return The WebPage after the Keys have been sent
     */
    public IWebPage sendKeysToElementByTagName(String name, CharSequence keysToSend);

    /**
     * Send CharSequence of Keys to an Element specified by a Tag Name
     * @param name The Object used to specify the Element
     * @param keysToSend The Keys to be sent
     * @param secondsTimeout The wait timeout in seconds
     * @return The WebPage after the Keys have been sent
     */
    public IWebPage sendKeysToElementByTagName(String name, CharSequence keysToSend,
      int secondsTimeout);

    /**
     * Send CharSequence of Keys to an Element specified by an XPath Expression
     * @param xpathExpression The Object used to specify the Element
     * @param keysToSend The Keys to be sent
     * @return The WebPage after the Keys have been sent
     */
    public IWebPage sendKeysToElementByXpath(String xpathExpression, CharSequence keysToSend);

    /**
     * Send CharSequence of Keys to an Element specified by an XPath Expression
     * @param xpathExpression The Object used to specify the Element
     * @param keysToSend The Keys to be sent
     * @param secondsTimeout The wait timeout in seconds
     * @return The WebPage after the Keys have been sent
     */
    public IWebPage sendKeysToElementByXpath(String xpathExpression, CharSequence keysToSend,
      int secondsTimeout);

    /**
     * Send CharSequence of Keys to an Element specified by a By Object
     * @param by The Object used to specify the Element
     * @param keysToSend The Keys to be sent
     * @return The WebPage after the Keys have been sent
     */
    public IWebPage sendKeysToElement(By by, CharSequence keysToSend);

    /**
     * Send CharSequence of Keys to an Element specified by a By Object
     * @param by The Object used to specify the Element
     * @param keysToSend The Keys to be sent 
     * @param secondsTimeout The wait timeout in seconds
     * @return The WebPage after the Keys have been sent
     */
    public IWebPage sendKeysToElement(By by, CharSequence keysToSend, int secondsTimeout);

    /**
     * Returns the first WebElement specified by a Class Name
     * @param className The Object used to find the element
     * @return The first found WebElement
     */
    public WebElement findElementByClassName(String className);

    /**
     * Returns all WebElements specified by a Class Name
     * @param className The Object used to find the elements
     * @return All found WebElements
     */
    public List<WebElement> findElementsByClassName(String className);

    /**
     * Returns the first WebElement specified by a Css Selector
     * @param selector The Object used to find the element
     * @return The first found WebElement
     */
    public WebElement findElementByCssSelector(String selector);

    /**
     * Returns all WebElements specified by a Css Selector
     * @param selector The Object used to find the elements
     * @return All found WebElements
     */
    public List<WebElement> findElementsByCssSelector(String selector);

    /**
     * Returns the first WebElement specified by an ID
     * @param id The Object used to find the element
     * @return The first found WebElement
     */
    public WebElement findElementById(String id);

    /**
     * Returns all WebElements specified by an ID
     * @param id The Object used to find the elements
     * @return All found WebElements
     */
    public List<WebElement> findElementsById(String id);

    /**
     * Returns the first WebElement specified by a Link Text
     * @param linkText The Object used to find the element
     * @return The first found WebElement
     */
    public WebElement findElementByLinkText(String linkText);

    /**
     * Returns all WebElements specified by a Link Text
     * @param linkText The Object used to find the elements
     * @return All found WebElements
     */
    public List<WebElement> findElementsByLinkText(String linkText);

    /**
     * Returns the first WebElement specified by a Name
     * @param name The Object used to find the element
     * @return The first found WebElement
     */
    public WebElement findElementByName(String name);

    /**
     * Returns all WebElements specified by a Name
     * @param name The Object used to find the elements
     * @return All found WebElements
     */
    public List<WebElement> findElementsByName(String name);

    /**
     * Returns the first WebElement specified by a Partial Link Text
     * @param linkText The Object used to find the element
     * @return The first found WebElement
     */
    public WebElement findElementByPartialLinkText(String linkText);

    /**
     * Returns all WebElements specified by a Partial Link Text
     * @param linkText The Object used to find the elements
     * @return All found WebElements
     */
    public List<WebElement> findElementsByPartialLinkText(String linkText);

    /**
     * Returns the first WebElement specified by a Tag Name
     * @param name The Object used to find the element
     * @return The first found WebElement
     */
    public WebElement findElementByTagName(String name);

    /**
     * Returns all WebElements specified by a Tag Name
     * @param name The Object used to find the elements
     * @return All found WebElements
     */
    public List<WebElement> findElementsByTagName(String name);

    /**
     * Returns the first WebElement specified by an XPath Expression
     * @param xpathExpression The Object used to find the element
     * @return The first found WebElement
     */
    public WebElement findElementByXpath(String xpathExpression);

    /**
     * Returns all WebElements specified by an XPath Expression
     * @param xpathExpression The Object used to find the elements
     * @return All found WebElements
     */
    public List<WebElement> findElementsByXpath(String xpathExpression);

    /**
     * Returns the first WebElement specified by a By Object
     * @param by The Object used to find the element
     * @return The first found WebElement
     */
    public WebElement findElement(By by);

    /**
     * Returns all WebElements specified by a By Object
     * @param by The Object used to find the elements
     * @return All found WebElements
     */
    public List<WebElement> findElements(By by);

    /**
     * Load WebPage with the given URL in current Browser Window
     * @param url The URL of the specified WebPage
     */
    public IWebPage get(String url);

    /**
     * Returns the String representation of the URL f the current WebPage
     * @return The String representation of the URL f the current WebPage
     */
    public String getCurrentUrl();

    /**
     * Returns the source of the current WebPage
     * @return The source of the current WebPage
     */
    public String getPageSource();

    /**
     * Returns the Title of the current WebPage
     * @return The Title of the current WebPage
     */
    public String getTitle();

    /**
     * Return an opaque handle to this window that uniquely identifies it within this driver instance
     * @return An opaque handle to this window that uniquely identifies it within this driver instance
     */
    public String getWindowHandle();

    /**
     * Return a set of window handles which can be used to iterate over all open windows of this
     * WebDriver instance by passing them to switchTo().WebDriver.Options.window()
     * @return A set of window handles which can be used to iterate over all open windows of this
     *         WebDriver instance by passing them to switchTo().WebDriver.Options.window()
     */
    public Set<String> getWindowHandles();

    /**
     * Returns the Options interface for the WebPage
     * @return The Options interface for the WebPage
     */
    public Options manage();

    /**
     * Returns an abstraction allowing the Diver to access the Browser's history to navigate to a URL
     * @return An abstraction allowing the Diver to access the Browser's history to navigate to a URL
     */
    public Navigation navigate();

    /**
     * Quit the Driver, closes all associated Windows
     */
    public void quit();

    /**
     * Send future commands to a different frame
     * @return The TargetLocator for this WebPage
     */
    public TargetLocator switchTo();

    /**
     * Send future commands to the frame specified by Class Name
     * @param className The element used to identify the frame
     * @return The switched WebPage
     */
    public IWebPage switchToFrameByClassName(String className);

    /**
     * Send future commands to the frame specified by Css Selector
     * @param selector The element used to identify the frame
     * @return The switched WebPage
     */
    public IWebPage switchToFrameByCssSelector(String selector);

    /**
     * Send future commands to the frame specified by ID
     * @param id The element used to identify the frame
     * @return The switched WebPage
     */
    public IWebPage switchToFrameById(String id);

    /**
     * Send future commands to the frame specified by Link Text
     * @param linkText The element used to identify the frame
     * @return The switched WebPage
     */
    public IWebPage switchToFrameByLinkText(String linkText);

    /**
     * Send future commands to the frame specified by Name
     * @param name The element used to identify the frame
     * @return The switched WebPage
     */
    public IWebPage switchToFrameByName(String name);

    /**
     * Send future commands to the frame specified by Partial Link Text
     * @param linkText The element used to identify the frame
     * @return The switched WebPage
     */
    public IWebPage switchToFrameByPartialLinkText(String linkText);

    /**
     * Send future commands to the frame specified by Tag Name
     * @param name The element used to identify the frame
     * @return The switched WebPage
     */
    public IWebPage switchToFrameByTagName(String name);

    /**
     * Send future commands to the frame specified by XPath Expression
     * @param xpathExpression The element used to identify the frame
     * @return The switched WebPage
     */
    public IWebPage switchToFrameByXpath(String xpathExpression);

    /**
     * Send future commands to the frame specified by By Object
     * @param by The element used to identify the frame
     * @return The switched WebPage
     */
    public IWebPage switchToFrame(By by);

    /**
     * Waits for an Object with specified Class Name
     * @param className The specified Object
     * @return The WebElement specified after the wait
     */
    public WebElement waitForElementByClassName(String className);

    /**
     * Waits for an Object with specified Class Name with timeout
     * @param className The specified Object
     * @param secondsTimeout The Timeout
     * @return The WebElement specified after the wait
     */
    public WebElement waitForElementByClassName(String className, int secondsTimeout);

    /**
     * Waits for an Object with specified Css Selector
     * @param selector The specified Object
     * @return The WebElement specified after the wait
     */
    public WebElement waitForElementByCssSelector(String selector);

    /**
     * Waits for an Object with specified Css Selector with timeout
     * @param selector The specified Object
     * @param secondsTimeout The Timeout
     * @return The WebElement specified after the wait
     */
    public WebElement waitForElementByCssSelector(String selector, int secondsTimeout);

    /**
     * Waits for an Object with specified ID
     * @param id The specified Object
     * @return The WebElement specified after the wait
     */
    public WebElement waitForElementById(String id);

    /**
     * Waits for an Object with specified ID with timeout
     * @param id The specified Object
     * @param secondsTimeout The Timeout
     * @return The WebElement specified after the wait
     */
    public WebElement waitForElementById(String id, int secondsTimeout);

    /**
     * Waits for an Object with specified Link Text
     * @param linkText The specified Object
     * @return The WebElement specified after the wait
     */
    public WebElement waitForElementByLinkText(String linkText);

    /**
     * Waits for an Object with specified Link Text with timeout
     * @param linkText The specified Object
     * @param secondsTimeout The Timeout
     * @return The WebElement specified after the wait
     */
    public WebElement waitForElementByLinkText(String linkText, int secondsTimeout);

    /**
     * Waits for an Object with specified Name
     * @param name The specified Object
     * @return The WebElement specified after the wait
     */
    public WebElement waitForElementByName(String name);

    /**
     * Waits for an Object with specified Name with timeout
     * @param name The specified Object
     * @param secondsTimeout The Timeout
     * @return The WebElement specified after the wait
     */
    public WebElement waitForElementByName(String name, int secondsTimeout);

    /**
     * Waits for an Object with specified Partial Link Text
     * @param linkText The specified Object
     * @return The WebElement specified after the wait
     */
    public WebElement waitForElementByPartialLinkText(String linkText);

    /**
     * Waits for an Object with specified Partial Link Text with timeout
     * @param linkText The specified Object
     * @param secondsTimeout The Timeout
     * @return The WebElement specified after the wait
     */
    public WebElement waitForElementByPartialLinkText(String linkText, int secondsTimeout);

    /**
     * Waits for an Object with specified Tag Name
     * @param tagName The specified Object
     * @return The WebElement specified after the wait
     */
    public WebElement waitForElementByTagName(String tagName);

    /**
     * Waits for an Object with specified Tag Name with timeout
     * @param tagName The specified Object
     * @param secondsTimeout The Timeout
     * @return The WebElement specified after the wait
     */
    public WebElement waitForElementByTagName(String tagName, int secondsTimeout);

    /**
     * Waits for an Object with specified XPath expression
     * @param xpathExpression The specified Object
     * @return The WebElement specified after the wait
     */
    public WebElement waitForElementByXpath(String xpathExpression);

    /**
     * Waits for an Object with specified XPath expression with timeout
     * @param xpathExpression The specified Object
     * @param secondsTimeout The Timeout
     * @return The WebElement specified after the wait
     */
    public WebElement waitForElementByXpath(String xpathExpression, int secondsTimeout);

    /**
     * Waits for a specified By object
     * @param by The specified Object
     * @return The WebElement specified after the wait
     */
    public WebElement waitForElement(By by);

    /**
     * Waits for a specified By object with timeout
     * @param by The specified Object
     * @param secondsTimeout The Timeout
     * @return The WebElement specified after the wait
     */
    public WebElement waitForElement(By by, int secondsTimeout);

    /**
     * Return a WebDriverWait with a default timeout
     * @return A WebDriverWait with a default timeout
     */
    public WebDriverWait driverWait();

    /**
     * Return a WebDriverWait with a given timeout
     * @param secondsTimeout
     * @return A WebDriverWait with a given timeout
     */
    public WebDriverWait driverWait(int secondsTimeout);

    /**
     * Returns the WebDriver associated with this WebPage
     * @return The WebDriver associated with this WebPage
     */
    public WebDriver getWebDriver();

    /**
     * Maximizes the WebPage
     */
    public IWebPage maximize();

    /**
     * Waits for the page to fully load with the default timeout
     */
    public IWebPage waitForPageLoad();

    /**
     * Waits for the page to fully load with the given timeout
     */
    public IWebPage waitForPageLoad(int secondsTimeout);

    /**
     * Takes a screenshot of the current screen and stores it in the RAS
     */
    public IWebPage takeScreenShot() throws SeleniumManagerException;

}