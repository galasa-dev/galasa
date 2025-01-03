# Running the Selenium Manager IVT locally

The Selenium Manager IVT runs various tests using different browsers to ensure that Selenium can be used for browser-based testing.

## Prerequisites

1. You will need the following browsers:
    - [Mozilla Firefox](https://www.mozilla.org/en-GB/firefox/new)
    - [Google Chrome](https://www.google.com/chrome)
    - [Microsoft Edge](https://www.microsoft.com/en-gb/edge)

2. Once the above browsers are installed, you will also need to download their corresponding WebDrivers:
   - Firefox: [Geckodriver](https://github.com/mozilla/geckodriver)
   - Chrome: [ChromeDriver](https://developer.chrome.com/docs/chromedriver)
   - Edge: [EdgeDriver](https://learn.microsoft.com/en-gb/microsoft-edge/webdriver-chromium/?tabs=c-sharp&form=MA13LH)

3. Extract the WebDrivers into a folder and add that folder to your `PATH` environment variable

## Steps

### 1. Setting CPS properties

Add the following properties to your `cps.properties` file:
```properties
selenium.local.driver.FIREFOX.path=/path/to/geckodriver
selenium.local.driver.EDGE.path=/path/to/msedgedriver
selenium.local.driver.CHROME.path=/path/to/chromedriver
selenium.default.driver=FIREFOX
selenium.available.drivers=CHROME,FIREFOX,EDGE
```

where:
- `/path/to/geckodriver` is an absolute path to the Geckodriver
- `/path/to/msedgedriver` is an absolute path to the EdgeDriver
- `/path/to/chromedriver` is an absolute path to the ChromeDriver

### 2. Running the Selenium Manager IVT

Having configured your `cps.properties` file, you should now be able to run the Selenium Manager IVT using `galasactl`:

1. Build the `managers` project using the `build-locally.sh` script:
    ```bash
    ./build-locally -c
    ```
2. Run the Selenium Manager IVT, replacing `0.39.0` with the relevant Galasa OBR version you wish to use:
    ```bash
    galasactl runs submit local --obr mvn:dev.galasa/dev.galasa.uber.obr/0.39.0/obr --class dev.galasa.selenium.manager.ivt/dev.galasa.selenium.manager.ivt.SeleniumManagerIVT --log -
    ```

## Using a Selenium Grid

If you would like to run the Selenium Manager IVT using a Selenium Grid running locally:

1. Set up and start a Selenium Grid by following the [Selenium Grid documentation](https://www.selenium.dev/documentation/grid/getting_started/)
2. Add the following properties to your `cps.properties` file in addition to the ones added in the [previous section](#1-setting-cps-properties):
    ```properties
    selenium.grid.endpoint=http://localhost:4444
    selenium.driver.type=grid
    ```
3. Run the Selenium Manager IVT as shown in the [previous section](#2-running-the-selenium-manager-ivt)
