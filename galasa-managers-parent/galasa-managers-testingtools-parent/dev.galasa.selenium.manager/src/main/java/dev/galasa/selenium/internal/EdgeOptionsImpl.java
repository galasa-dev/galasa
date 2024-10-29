/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.selenium.internal;

import java.util.Map;
import java.util.Set;

import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.edge.EdgeOptions;

import dev.galasa.selenium.IEdgeOptions;

/**
 * An Inplementation of EdgeOptions as to avoid test code having dependanicies 
 * on org.openqa.selenium*. 
 */

public class EdgeOptionsImpl implements IEdgeOptions {

    public EdgeOptions options;

    public EdgeOptionsImpl() {
        options = new EdgeOptions();
    }
    
    protected EdgeOptions get() {
    	return this.options;
    }

    @Override
    public void setCapability(String key, Object value) {
        options.setCapability(key, value);
    }

    @Override
    public void setCapability(String key, String value) {
        options.setCapability(key, value);
    }

    @Override
    public void setCapability(String key, Boolean value) {
        options.setCapability(key, value);
    }

	@Override
	public void setCapability(String key, Platform value) {
		options.setCapability(key, value);	
	}

	@Override
	public void setPageLoadStrategy(String strategy) {
		options.setPageLoadStrategy(strategy);
	}

	@Override
	public void setProxy(Proxy proxy) {
		options.setProxy(proxy);
	}

	@Override
	public Map<String, Object> asMap() {
		return options.asMap();
	}

	@Override
	public String getBrowserName() {
		return options.getBrowserName();
	}

	@Override
	public Object getCapability(String capabilityName) {
		return options.getCapability(capabilityName);
	}

	@Override
	public Set<String> getCapabilityNames() {
		return options.getCapabilityNames();
	}

	@Override
	public Platform getPlatform() {
		return options.getPlatform();
	}

	@Override
	public String getVersion() {
		return options.getVersion();
	}

	@Override
	public boolean is(String capabilityName) {
		return options.is(capabilityName);
	}

	@Override
	public boolean isJavaScriptEnabled(String capabilityName) {
		return options.isJavascriptEnabled();
	}
    
}