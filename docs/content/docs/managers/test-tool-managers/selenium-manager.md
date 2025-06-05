---
title: "Selenium Manager"
---

This Manager is at Beta level. You can view the <a href="https://javadoc.galasa.dev/dev/galasa/selenium/package-summary.html" target="_blank" rel="noopener noreferrer">Javadoc documentation for the Manager here</a>.<br>



[Overview](#overview)<br>
[Code snippets and examples](#codesnippets)<br>


# <a name="overview"></a>Overview
This Manager enables the test to run Selenium WebDrivers in order to drive Web Browsers during the test. Browsers can have actions performed against them  to navigate WebPages and extract information about the current page. <br><br> As an absolute minimum, the CPS property <br> <code>selenium.instance.PRIMARY.gecko.path</code><br> must be provided as the Manager will default to using a GECKO WebDriver if no WebDriver is provided. <br><br> The CPS property <br> <code>selenium.instance.PRIMARY.web.driver</code><br> can be used to set a different WebDriver. This will also require the corresponding driver path to be set. <br> eg. <code>selenium.instance.PRIMARY.web.driver=CHROME</code><br> requires <code>selenium.instance.PRIMARY.chrome.path=...</code><br>

## Limitations
The Selenium Manager only supports GECKO, CHROME, EDGE and IE WebDrivers.<br><br> 



## <a name="codesnippets"></a>Code snippets

Use the following code snippets to help you get started with the Selenium Manager.
 
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

## Configuration Properties

The following are properties used to configure the Selenium Manager.
 
<details>
<summary>Selenium Available Drivers CPS Property</summary>

| Property: | Selenium Available Drivers CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | selenium.driver.type |
| Description: | Describes the selenium driver types that can be selected. |
| Required:  | No |
| Default value: | $default |
| Valid values: | A valid String the describes any of the supported drivers: FIREFOX,CHROME,OPERA,EDGE |
| Examples: | <code>selenium.available.drivers=CHROME,FIREFOX,OPERA,EDGE</code> |

</details>
 
<details>
<summary>Selenium Default Driver CPS Property</summary>

| Property: | Selenium Default Driver CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | selenium.default.driver.type |
| Description: | If set, describes the default the selenium driver that will be used. |
| Required:  | No |
| Default value: | $default |
| Valid values: | A valid String representation of a type. Available choices: local, docker, kubernetes, grid |
| Examples: | <code>selenium.default.driver=FIREFOX</code> |

</details>
 
<details>
<summary>Selenium Driver Version for Containerised Node</summary>

| Property: | Selenium Driver Version for Containerised Node |
| --------------------------------------- | :------------------------------------- |
| Name: | selenium.image.node.version |
| Description: | Provides the version number for the docker image that will be used for both the provisioning of docker and kubernetes selenium nodes. |
| Required:  | no |
| Default value: | $default |
| Valid values: | 4.0.0-beta-2-20210317 |
| Examples: | <code>selenium.image.node.version=4.0.0-beta-2-20210317</code> |

</details>
 
<details>
<summary>Selenium Driver Max Slots CPS Property</summary>

| Property: | Selenium Driver Max Slots CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | selenium.driver.max.slots |
| Description: | Allows number of concurrent drivers to be limited. If docker selected, the docker slot limit will also be enforced |
| Required:  | No |
| Default value: | $default |
| Valid values: | Int value for number of congruent drivers |
| Examples: | <code>selenium.driver.max.slots=3</code> |

</details>
 
<details>
<summary>Selenium Gecko Preferences CPS Property</summary>

| Property: | Selenium Gecko Preferences CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | selenium.local.gecko.preferences |
| Description: | Provides extra preferences to use when using the gecko driver for extensions |
| Required:  | No |
| Default value: | $default |
| Valid values: | A comma seperated list of key value pairs for the preferences |
| Examples: | <code>selenium.local.gecko.preferences=app.update.silent=false,dom.popup_maximum=0</code> |

</details>
 
<details>
<summary>Selenium Gecko Profile CPS Property</summary>

| Property: | Selenium Gecko Profile CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | selenium.local.gecko.profile |
| Description: | Provides a profile to use when using the gecko driver for extensions |
| Required:  | No |
| Default value: | $default |
| Valid values: | A valid String name of a profile |
| Examples: | <code>selenium.local.gecko.profile=default</code> |

</details>
 
<details>
<summary>Selenium Grid Endpoint CPS Property</summary>

| Property: | Selenium Grid Endpoint CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | selenium.grid.endpoint |
| Description: | States the grid endpoint |
| Required:  | No |
| Default value: | $default |
| Valid values: | ip's and hostnames for a selenium grid |
| Examples: | <code>selenium.grid.endpoint=127.0.0.1:4444</code> |

</details>
 
<details>
<summary>Selenium Kubernetes Namespace</summary>

| Property: | Selenium Kubernetes Namespace |
| --------------------------------------- | :------------------------------------- |
| Name: | selenium.kubernetes.namespace |
| Description: | Provides the name of the namespace for the nodes to be provisioned on |
| Required:  | Yes |
| Default value: | $default |
| Valid values: | A valid String representation an available namespace on your k8's cluster |
| Examples: | <code>selenium.kubernetes.namespace=galasa</code> |

</details>
 
<details>
<summary>Selenium Node Selector CPS Property</summary>

| Property: | Selenium Node Selector CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | selenium.kubernetes.node.selector |
| Description: | Node Selector tags to be added to the pod yaml that runs the Selenium Grid inside a k8's cluster. Multiple selectors can be passed comma seperated |
| Required:  | No |
| Default value: | $default |
| Valid values: | Comma seperated list of any node selectors: beta.kubernetes.io/arch: amd64, platform: myplatform |
| Examples: | <code>selenium.kubernetes.node.selector=beta.kubernetes.io/arch: amd64</code> |

</details>
 
<details>
<summary>Selenium Driver Path CPS Property</summary>

| Property: | Selenium Driver Path CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | selenium.local.driver.BROWSER.path |
| Description: | Provides a path to a local webdriver on the system being tested |
| Required:  | Yes |
| Default value: | $default |
| Valid values: | A valid String representation of a path |
| Examples: | <code>selenium.local.driver.CHROME.path=/usr/bin/chromedriver</code> |

</details>
 
<details>
<summary>Selenium Screenshot Failure CPS Property</summary>

| Property: | Selenium Screenshot Failure CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | selenium.screenshot.failure |
| Description: | Takes a screenshot on a test method failing |
| Required:  | No |
| Default value: | $default |
| Valid values: | true or false |
| Examples: | <code>selenium.screenshot.failure=true</code> |

</details>
 
<details>
<summary>Selenium Driver Type CPS Property</summary>

| Property: | Selenium Driver Type CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | selenium.driver.type |
| Description: | Describes the selenium runtime that will be used. |
| Required:  | No |
| Default value: | $default |
| Valid values: | A valid String representation of a type. Available choices: local, docker, kubernetes, grid |
| Examples: | <code>selenium.driver.type=docker</code> |

</details>
