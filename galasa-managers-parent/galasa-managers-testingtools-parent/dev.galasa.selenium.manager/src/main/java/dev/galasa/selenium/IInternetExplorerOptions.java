/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.selenium;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.ie.ElementScrollBehavior;

/**
 * A Options pass through interface for the Selenium Options object
 * 
 *  
 *
 */
public interface IInternetExplorerOptions {

    public void destructivelyEnsureCleanSession();

    public void disableNativeEvents();

    public void enablePersistentHovering();

    public void ignoreZoomSettings();

    public void introduceFlakinessByIgnoringSecurityDomains();

    public void requireWindowFocus();

    public void useCreateProcessApiToLaunchIe();

    public void waitForUploadDialogUpTo(long duration, TimeUnit unit);

    public void withAttachTimeout(long duration, TimeUnit unit);

    public void withInitialBrowserUrl(String url);

    public void addCommandSwitches(String... switches);

    public void setCapability(String key, Object value);

    public void setCapability(String key, String value);

    public void setCapability(String key, Boolean value);
    
    public Map<String,Object> asMap();

    public void elementScrollTo(ElementScrollBehavior behavior);
    
    public void enableNativeEvents();
    
    public String getBrowserName();
    
    public Object getCapability(String capabilityName);
    
    public Set<String> getCapabilityNames();
    
    public Platform getPlatform();
    
    public String getVersion();
    
    public boolean is(String capabilityName);
    
    public boolean isJavascriptEnabled();
    
    public void setCapability(String capabilityName, Platform value);
    
    public void setPageLoadStrategy(PageLoadStrategy strategy);
    
    public void setProxy(Proxy proxy);
    
    public void setUnhandledPromptBehaviour(UnexpectedAlertBehaviour  behaviour);
    
    public void takeFullPageScreenshot();
    
    public void usePerProcessProxy();
    
    public void useShellWindowsApiToAttachToIe();
    
    public void waitForUploadDialogUpTo(Duration duration);
    
    public void waitForUploadDialogUpTo(Long duration, TimeUnit unit);
    
    public void withAttachTimeout(Duration duration);
    
}
