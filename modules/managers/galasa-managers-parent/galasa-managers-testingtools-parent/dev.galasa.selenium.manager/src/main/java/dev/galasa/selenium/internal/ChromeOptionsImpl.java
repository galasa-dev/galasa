/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.selenium.internal;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.chrome.ChromeOptions;

import dev.galasa.selenium.IChromeOptions;

/**
 * An Inplementation of ChromeOptions as to avoid test code having dependanicies 
 * on org.openqa.selenium*. 
 */
public class ChromeOptionsImpl implements IChromeOptions {

    public ChromeOptions options;

    public ChromeOptionsImpl() {
        options = new ChromeOptions();
    }
    
    protected ChromeOptions get() {
    	return this.options;
    }

    @Override
    public void addEncodedExtensions(String... encoded) {
        options.addEncodedExtensions(encoded);
    }

    @Override
    public void addEncodedExtensions(List<String> encoded) {
        options.addEncodedExtensions(encoded);
    }

    @Override
    public void addExtensions(File... paths) {
        options.addExtensions(paths);
    }

    @Override
    public void addExtensions(List<File> paths) {
        options.addExtensions(paths);
    }

    @Override
    public void setAcceptInsecureCerts(boolean bool) {
        options.setAcceptInsecureCerts(bool);
    }

    @Override
    public void setHeadless(boolean bool) {
        options.setHeadless(bool);
    }

    @Override
    public void setBinary(File path) {
        options.setBinary(path);
    }

    @Override
    public void setBinary(String path) {
        options.setBinary(path);
    }

    @Override
    public void addArguments(String... arguments) {
        options.addArguments(arguments);
    }

    @Override
    public void addArguments(List<String> arguments) {
        options.addArguments(arguments);
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
	public void setExperimentalOption(String key, Platform value) {
		options.setExperimentalOption(key, value);
	}

	@Override
	public void setPageLoadStrategy(PageLoadStrategy strategy) {
		options.setPageLoadStrategy(strategy);
	}

	@Override
	public void setProxy(Proxy proxy) {
		options.setProxy(proxy);
	}

	@Override
	public void setUnhandledPromptBehaviour(UnexpectedAlertBehaviour behaviour) {
		options.setUnhandledPromptBehaviour(behaviour);
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
	public Object getExperimentalOption(String name) {
		return options.getExperimentalOption(name);
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