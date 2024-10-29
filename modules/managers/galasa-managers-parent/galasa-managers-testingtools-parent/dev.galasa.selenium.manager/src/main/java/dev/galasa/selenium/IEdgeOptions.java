/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.selenium;

import java.util.Map;
import java.util.Set;

import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;

/**
 * A Options pass through interface for the Selenium Options object
 * 
 *  
 *
 */
public interface IEdgeOptions {

    public void setCapability(String key, Object value);

    public void setCapability(String key, String value);

    public void setCapability(String key, Boolean value);

    public void setCapability(String key, Platform value);

    public void setPageLoadStrategy(String strategy);

    public void setProxy(Proxy proxy);

    public Map<String,Object> asMap();

    public String getBrowserName();

    public Object getCapability(String capabilityName);

    public Set<String> getCapabilityNames();

    public Platform getPlatform();

    public String getVersion();

    public boolean is(String capabilityName);

    public boolean isJavaScriptEnabled(String capabilityName);
    
}