---
title: "Selenium Manager"
---

This Manager is at Beta level. You can view the [Javadoc documentation for the Manager](https://javadoc.galasa.dev/dev/galasa/selenium/package-summary.html){target="_blank"}.


## Overview

This Manager enables the test to run Selenium WebDrivers in order to drive Web Browsers during the test. Browsers can have actions performed against them  to navigate WebPages and extract information about the current page.

As an absolute minimum, the CPS property `selenium.instance.PRIMARY.gecko.path` must be provided as the Manager will default to using a GECKO WebDriver if no WebDriver is provided.

The CPS property `selenium.instance.PRIMARY.web.driver` can be used to set a different WebDriver. This will also require the corresponding driver path to be set.

eg. `selenium.instance.PRIMARY.web.driver=CHROME` requires `selenium.instance.PRIMARY.chrome.path=...`


## Limitations

The Selenium Manager only supports GECKO, CHROME, EDGE and IE WebDrivers.


## Code snippets

Use the following code snippets to help you get started with the Selenium Manager.
 

### Create the Selenium Manager

The following snippet shows the minimum code that is required to request the Selenium Manager in a test:

```java
@SeleniumManager
public ISeleniumManager seleniumManager;
```

The code creates an interface to the Selenium Manager which will allow the tester to provision web pages to test against.


### Open a Web Page

```java
IWebPage page = seleniumManager.allocateWebPage("https://galasa.dev/");
```

The code opens a Web Page with a Selenium WebDriver controlling the browser. This object provides an interface for the tester to perform actions on the page to navigate around, check the page content and switch between windows.

At the end of the test, the Selenium Manager automatically closes the WebDriver which removes the WebPage.

There is no limit in Galasa on how many Selenium WebPages can be used within a single test. The only limit is the ability of the Galasa Ecosystem they are running on to support the number of Selenium WebDrivers ensuring that they do not time out.


### Navigating around a web page browser

```java
page.clearElementByCssSelector("input.js-search-input.search__input--adv");
page.sendKeysToElementByClass("js-search-input.search__input--adv", "Galasa");
page.clickElementById("search_button_homepage");
```

The code showcases different actions which can be performed on a web page interface to interact with different WebElements on the Browser. These WebElements are selected using a range of different techniques which allows the tester flexibility in how they are selected.


### Extracting web page information

```java
WebElement element = page.findElementById("search_button_homepage");
String pageTitle = page.getTitle();
String pageSource = page.getPageSource();
```

The code shows different ways of gaining information about the web page to be tested against. Extracting the title is a very simple way of checking if the WebDriver is on the correct page and making sure that a WebElement is found.


## Configuration Properties

The following are properties used to configure the Selenium Manager.
 

### Selenium Available Drivers CPS Property

| Property: | Selenium Available Drivers CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | selenium.driver.type |
| Description: | Describes the selenium driver types that can be selected. |
| Required:  | No |
| Default value: | $default |
| Valid values: | A valid String the describes any of the supported drivers: FIREFOX,CHROME,OPERA,EDGE |
| Examples: | `selenium.available.drivers=CHROME,FIREFOX,OPERA,EDGE` |


### Selenium Default Driver CPS Property

| Property: | Selenium Default Driver CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | selenium.default.driver.type |
| Description: | If set, describes the default the selenium driver that will be used. |
| Required:  | No |
| Default value: | $default |
| Valid values: | A valid String representation of a type. Available choices: local, docker, kubernetes, grid |
| Examples: | `selenium.default.driver=FIREFOX` |


### Selenium Driver Version for Containerised Node

| Property: | Selenium Driver Version for Containerised Node |
| --------------------------------------- | :------------------------------------- |
| Name: | selenium.image.node.version |
| Description: | Provides the version number for the docker image that will be used for both the provisioning of docker and kubernetes selenium nodes. |
| Required:  | no |
| Default value: | $default |
| Valid values: | 4.0.0-beta-2-20210317 |
| Examples: | `selenium.image.node.version=4.0.0-beta-2-20210317` |


### Selenium Driver Max Slots CPS Property

| Property: | Selenium Driver Max Slots CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | selenium.driver.max.slots |
| Description: | Allows number of concurrent drivers to be limited. If docker selected, the docker slot limit will also be enforced |
| Required:  | No |
| Default value: | $default |
| Valid values: | Int value for number of congruent drivers |
| Examples: | `selenium.driver.max.slots=3` |


### Selenium Gecko Preferences CPS Property

| Property: | Selenium Gecko Preferences CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | selenium.local.gecko.preferences |
| Description: | Provides extra preferences to use when using the gecko driver for extensions |
| Required:  | No |
| Default value: | $default |
| Valid values: | A comma seperated list of key value pairs for the preferences |
| Examples: | `selenium.local.gecko.preferences=app.update.silent=false,dom.popup_maximum=0` |


### Selenium Gecko Profile CPS Property

| Property: | Selenium Gecko Profile CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | selenium.local.gecko.profile |
| Description: | Provides a profile to use when using the gecko driver for extensions |
| Required:  | No |
| Default value: | $default |
| Valid values: | A valid String name of a profile |
| Examples: | `selenium.local.gecko.profile=default` |


### Selenium Grid Endpoint CPS Property

| Property: | Selenium Grid Endpoint CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | selenium.grid.endpoint |
| Description: | States the grid endpoint |
| Required:  | No |
| Default value: | $default |
| Valid values: | ip's and hostnames for a selenium grid |
| Examples: | `selenium.grid.endpoint=127.0.0.1:4444` |


### Selenium Kubernetes Namespace

| Property: | Selenium Kubernetes Namespace |
| --------------------------------------- | :------------------------------------- |
| Name: | selenium.kubernetes.namespace |
| Description: | Provides the name of the namespace for the nodes to be provisioned on |
| Required:  | Yes |
| Default value: | $default |
| Valid values: | A valid String representation an available namespace on your k8's cluster |
| Examples: | `selenium.kubernetes.namespace=galasa` |


### Selenium Node Selector CPS Property

| Property: | Selenium Node Selector CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | selenium.kubernetes.node.selector |
| Description: | Node Selector tags to be added to the pod yaml that runs the Selenium Grid inside a k8's cluster. Multiple selectors can be passed comma seperated |
| Required:  | No |
| Default value: | $default |
| Valid values: | Comma seperated list of any node selectors: beta.kubernetes.io/arch: amd64, platform: myplatform |
| Examples: | `selenium.kubernetes.node.selector=beta.kubernetes.io/arch: amd64` |


### Selenium Driver Path CPS Property

| Property: | Selenium Driver Path CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | selenium.local.driver.BROWSER.path |
| Description: | Provides a path to a local webdriver on the system being tested |
| Required:  | Yes |
| Default value: | $default |
| Valid values: | A valid String representation of a path |
| Examples: | `selenium.local.driver.CHROME.path=/usr/bin/chromedriver` |


### Selenium Screenshot Failure CPS Property

| Property: | Selenium Screenshot Failure CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | selenium.screenshot.failure |
| Description: | Takes a screenshot on a test method failing |
| Required:  | No |
| Default value: | $default |
| Valid values: | true or false |
| Examples: | `selenium.screenshot.failure=true` |


### Selenium Driver Type CPS Property

| Property: | Selenium Driver Type CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | selenium.driver.type |
| Description: | Describes the selenium runtime that will be used. |
| Required:  | No |
| Default value: | $default |
| Valid values: | A valid String representation of a type. Available choices: local, docker, kubernetes, grid |
| Examples: | `selenium.driver.type=docker` |

