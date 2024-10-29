/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.selenium;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;

/**
 * A Options pass through interface for the Selenium Options object
 * 
 *  
 *
 */
public interface IFirefoxOptions {
    public void addPreference(String key, String value);

    public void addPreference(String key, Integer value);

    public void addPreference(String key, Boolean value);

    public FirefoxOptions getOptions();

    public void setProfile(FirefoxProfile profile);

    public void setAcceptInsecureCerts(boolean bool);

    public void setHeadless(boolean bool);

    public void setBinary(Path path);

    public void setBinary(String path);

    public void addArguments(String...arguments);

    public void addArguments(List<String> arguments);

    public void setCapability(String key, Object value);

    public void setLegacy(boolean bool);

    public void setLogLevel(Level level);

    public Map<String,Object> asMap();

    public Optional<FirefoxBinary> getBinaryOrNull();

    public String getBrowserName();

    public Platform getPlatform();

    public FirefoxProfile getProfile();
    
    public String getVersion();
    
    public boolean is(String capabilityName);
    
    public boolean isJavascriptEnabled();
    
    public boolean isLegacy();
    
    public void setPageLoadStrategy(PageLoadStrategy strategy);
    
    public void setProxy(Proxy proxy);
    
    public void setUnhandledPromptBehaviour(UnexpectedAlertBehaviour behaviour);
}