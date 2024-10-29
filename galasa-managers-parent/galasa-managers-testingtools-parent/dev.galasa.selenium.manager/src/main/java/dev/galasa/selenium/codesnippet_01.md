<details><summary>Create the Selenium Manager</summary>

The following snippet shows the minimum code that is required to request the Selenium Manager in a test:

```
@SeleniumManager
public ISeleniumManager seleniumManager;
```

The code creates an interface to the Selenium Manager which will allow the tester to provision web pages to test against.
</details>

<details><summary>Open a WebPage</summary>

```
IWebPage page = seleniumManager.allocateWebPage("https://galasa.dev/");
```

The code opens a WebPage with a Selenium WebDriver controlling the browser. This object provides an interface for the tester to perform actions on the page to navigate around, check the page content and switch between windows.

At the end of the test, the Selenium Manager automatically closes the WebDriver which removes the WebPage.

There is no limit in Galasa on how many Selenium WebPages can be used within a single test. The only limit is the ability of the Galasa Ecosystem they are running on to support the number of Selenium WebDrivers ensuring that they do not time out.
</details>

<details><summary>Navigating around a web page browser</summary>

```
page.clearElementByCssSelector("input.js-search-input.search__input--adv");
page.sendKeysToElementByClass("js-search-input.search__input--adv", "Galasa");
page.clickElementById("search_button_homepage");
```

The code showcases different actions which can be performed on a web page interface to interact with different WebElements on the Browser. These WebElements are selected using a range of different techniques which allows the tester flexibility in how they are selected.
</details>

<details><summary>Extracting web page information</summary>

```
WebElement element = page.findElementById("search_button_homepage");
String pageTitle = page.getTitle();
String pageSource = page.getPageSource();
```

The code shows different ways of gaining information about the web page to be tested against. Extracting the title is a very simple way of checking if the WebDriver is on the correct page and making sure that a WebElement is found.
</details>