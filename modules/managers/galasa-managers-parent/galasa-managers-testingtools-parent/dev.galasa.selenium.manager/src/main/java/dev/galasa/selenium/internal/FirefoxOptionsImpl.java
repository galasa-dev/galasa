/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.selenium.internal;

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

import dev.galasa.selenium.IFirefoxOptions;

/**
 * An Inplementation of FirefixOptions as to avoid test code having dependanicies 
 * on org.openqa.selenium*. 
 */
public class FirefoxOptionsImpl implements IFirefoxOptions {
    public FirefoxOptions options;

    public FirefoxOptionsImpl() {
        this.options = new FirefoxOptions();
    }
    
    protected FirefoxOptions get() {
    	return this.options;
    }
    
    @Override
    public FirefoxOptions getOptions() {
        return this.options;
    }
    
    @Override
    public void addPreference(String key, String value) {
        options.addPreference(key, value);
    }

    @Override
    public void addPreference(String key, Integer value) {
        options.addPreference(key, value);
    }

    @Override
    public void addPreference(String key, Boolean value) {
        options.addPreference(key, value);
    }

    @Override
    public void setProfile(FirefoxProfile profile) {
       options.setProfile(profile);
    }

    @Override
    public void setHeadless(boolean bool) {
        options.setHeadless(bool);
    }    

    @Override
    public void setAcceptInsecureCerts(boolean bool){
        options.setAcceptInsecureCerts(bool);
    }

    @Override
    public void setBinary(Path path) {
        options.setBinary(path);
    }

    @Override
    public void setBinary(String path) {
        options.setBinary(path);
    }

    @Override
    public void addArguments(String...arguments) {
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
    public void setLegacy(boolean bool) {
        options.setLegacy(bool);
    }

    @Override
    public void setLogLevel(Level level) {
        options.setLogLevel(level);
    }

	@Override
	public Map<String, Object> asMap() {
		return options.asMap();
	}

	@Override
	public Optional<FirefoxBinary> getBinaryOrNull() {
		return options.getBinaryOrNull();
	}

	@Override
	public String getBrowserName() {
		return options.getBrowserName();
	}

	@Override
	public Platform getPlatform() {
		return options.getPlatform();
	}

	@Override
	public FirefoxProfile getProfile() {
		return options.getProfile();
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
	public boolean isJavascriptEnabled() {
		return options.isJavascriptEnabled();
	}

	@Override
	public boolean isLegacy() {
		return options.isLegacy();
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
}
