/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.selenium.internal;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.ProfilesIni;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.CapabilityType;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.selenium.Browser;
import dev.galasa.selenium.IFirefoxOptions;
import dev.galasa.selenium.SeleniumManagerException;
import dev.galasa.selenium.internal.properties.SeleniumDefaultDriver;
import dev.galasa.selenium.internal.properties.SeleniumGeckoPreferences;
import dev.galasa.selenium.internal.properties.SeleniumGeckoProfile;
import dev.galasa.selenium.internal.properties.SeleniumLocalDriverPath;

/**
 * Interactions with a different browser types for local Selenium drivers.
 * 
 *  
 *
 */
public enum LocalBrowser {
  GECKO, IE, CHROME, EDGE, OPERA;

  static final Log logger = LogFactory.getLog(LocalBrowser.class);

  public static WebDriver getWebDriver(Browser browser) throws SeleniumManagerException {
    try {
      switch (browser) {
        case FIREFOX:
          return getGeckoDriver();
        case CHROME:
          return getChromeDriver();
        case OPERA:
          return getOperaDriver();
        case EDGE:
          return getEdgeDriver();
        case IE:
          return getIEDriver();    
        default:
          throw new SeleniumManagerException("Unknown/Unsupported driver instance: " + browser.getDriverName());
      }
    } catch (Exception e) {
      throw new SeleniumManagerException("Unable to get driver instance", e);
    }
  }

  private static WebDriver getGeckoDriver() throws SeleniumManagerException {
    IFirefoxOptions ffOptions = new FirefoxOptionsImpl();
    ffOptions.setAcceptInsecureCerts(true);
    return getGeckoDriver(ffOptions);
  }

  public static WebDriver getGeckoDriver(IFirefoxOptions capabilities) throws SeleniumManagerException {
    try {
      System.setProperty("webdriver.gecko.driver", SeleniumLocalDriverPath.get("FIREFOX"));
    } catch (Exception e) {
      throw new SeleniumManagerException("Unable to get Gecko path from CPS for instance: " + e);
    }

    ProfilesIni profile = new ProfilesIni();
    String geckoProfile = null;
    try {
      geckoProfile = SeleniumGeckoProfile.get();
    } catch (ConfigurationPropertyStoreException e) {
      throw new SeleniumManagerException("Unable to get Gecko profile from CPS for instance: " + e);
    }

    FirefoxProfile ffProfile = null;
    if(geckoProfile != null && !geckoProfile.trim().isEmpty()) {
      ffProfile = profile.getProfile(geckoProfile);
      if(ffProfile == null) {
        logger.info("Gecko profile " + geckoProfile + " unavaiable, creating new profile");
        ffProfile = new FirefoxProfile();
      } else {
        logger.info("Gecko profile " + geckoProfile + " found and available");
      }
    } else {
      logger.info("Gecko profile not found in CPS, creating new profile");
      ffProfile = new FirefoxProfile();
    }

    capabilities.setProfile(ffProfile);

    try {
      String cpsPreferences = SeleniumGeckoPreferences.get();
      if(cpsPreferences != null) {
        String[] preferences = cpsPreferences.split(",");
        for(String preference : preferences) {
          String[] keyValue = preference.split("=");
          if(keyValue.length != 2) {
            logger.debug("Ignoring preference " + preference);
          } else {
            if(isInt(keyValue[1])) {
              capabilities.addPreference(keyValue[0], Integer.parseInt(keyValue[1]));
            } else if(isBool(keyValue[1])) {
              capabilities.addPreference(keyValue[0], Boolean.parseBoolean(keyValue[1]));
            } else {
              capabilities.addPreference(keyValue[0], keyValue[1]);
            }
            logger.debug("Adding extra preference " + preference);
          }
        }
      }
    } catch (ConfigurationPropertyStoreException e) {
      throw new SeleniumManagerException("Unable to get Gecko preferences from CPS", e);
    }

    return new FirefoxDriver(capabilities.getOptions());
  }

  private static WebDriver getChromeDriver() throws SeleniumManagerException {
    ChromeOptions cOptions = new ChromeOptions();
    cOptions.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
    return getChromeDriver(cOptions);
  }

  public static WebDriver getChromeDriver(ChromeOptions capabilities) throws SeleniumManagerException {
    try {
      System.setProperty("webdriver.chrome.driver", SeleniumLocalDriverPath.get("CHROME"));
    } catch (Exception e) {
      throw new SeleniumManagerException("Unable to get Chrome path from CPS", e);
    }
    return new ChromeDriver(capabilities);
  }

  private static WebDriver getEdgeDriver() throws SeleniumManagerException {
    EdgeOptions eOptions = new EdgeOptions();
    eOptions.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
    return getEdgeDriver(eOptions);
  }

  public static WebDriver getEdgeDriver(EdgeOptions capabilities) throws SeleniumManagerException {
    try {
      System.setProperty("webdriver.edge.driver", SeleniumLocalDriverPath.get("EDGE"));
    } catch (Exception e) {
      throw new SeleniumManagerException("Unable to get Edge path from CPS",e);
    }
    return new EdgeDriver(capabilities);
  }

  private static WebDriver getIEDriver() throws SeleniumManagerException {
    InternetExplorerOptions ieOptions = new InternetExplorerOptions();
    ieOptions.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
    return getIEDriver(ieOptions);
  }

  public static WebDriver getIEDriver(InternetExplorerOptions capabilities) throws SeleniumManagerException {
    try {
      System.setProperty("webdriver.ie.driver", SeleniumLocalDriverPath.get("IE"));
    } catch (Exception e) {
      throw new SeleniumManagerException("Unable to get IE path from CPS", e);
    }

    return new InternetExplorerDriver(capabilities);
  }

  private static WebDriver getOperaDriver() throws SeleniumManagerException {
    OperaOptions operaOptions = new OperaOptions();
    operaOptions.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
    return getOperaDriver(operaOptions);
  }

  public static WebDriver getOperaDriver(OperaOptions capabilities) throws SeleniumManagerException {
    try {
      System.setProperty("webdriver.opera.driver", SeleniumLocalDriverPath.get("OPERA"));
    } catch (Exception e) {
      throw new SeleniumManagerException("Unable to get Opera path from CPS", e);
    }

    return new OperaDriver(capabilities);
  }

  public static @NotNull LocalBrowser getBrowser(@NotNull String browser) throws SeleniumManagerException {
    browser = browser.trim();

    for (LocalBrowser d : values()) {
      if (browser.equalsIgnoreCase(d.name())) {
        return d;
      }
    }
    throw new SeleniumManagerException("Unsupported browser type: " + browser);
  }

  private static Boolean isInt(String value) {
    try{
      Integer.parseInt(value);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private static Boolean isBool(String value) {
    return "TRUE".equalsIgnoreCase(value) || "FALSE".equalsIgnoreCase(value);
  }
}
