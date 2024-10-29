/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.selenium;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.UnexpectedAlertBehaviour;

/**
 * A Options pass through interface for the Selenium Options object
 * 
 *  
 *
 */
public interface IChromeOptions {

    public void addEncodedExtensions(String... encoded);

    public void addEncodedExtensions(List<String> encoded);

    public void addExtensions(File... paths);

    public void addExtensions(List<File> paths);

    public void setAcceptInsecureCerts(boolean bool);

    public void setHeadless(boolean bool);

    public void setBinary(File path);

    public void setBinary(String path);

    public void addArguments(String...arguments);

    public void addArguments(List<String> arguments);

    public void setCapability(String key, Object value);

    public void setCapability(String key, String value);

    public void setCapability(String key, Boolean value);

    public void setCapability(String key, Platform value);

    public void setExperimentalOption(String key, Platform value);

    public void setPageLoadStrategy(PageLoadStrategy strategy);

    public void setProxy(Proxy proxy);

    public void setUnhandledPromptBehaviour(UnexpectedAlertBehaviour behaviour);

    public Map<String,Object> asMap();

    public String getBrowserName();

    public Object getCapability(String capabilityName);

    public Set<String> getCapabilityNames();

    public Object getExperimentalOption(String name);

    public Platform getPlatform();

    public String getVersion();

    public boolean is(String capabilityName);

    public boolean isJavaScriptEnabled(String capabilityName);
}