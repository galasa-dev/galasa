/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.selenium.internal;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.ie.ElementScrollBehavior;
import org.openqa.selenium.ie.InternetExplorerOptions;

import dev.galasa.selenium.IInternetExplorerOptions;

/**
 * An Inplementation of InternetExplorerOptions as to avoid test code having dependanicies 
 * on org.openqa.selenium*. 
 */
public class InternetExplorerOptionsImpl implements IInternetExplorerOptions {

    public InternetExplorerOptions options;

    public InternetExplorerOptionsImpl() {
        options = new InternetExplorerOptions();
    }
    
    protected InternetExplorerOptions get() {
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
    public void destructivelyEnsureCleanSession() {
        options.destructivelyEnsureCleanSession();
    }

    @Override
    public void disableNativeEvents() {
        options.disableNativeEvents();
    }

    @Override
    public void enablePersistentHovering() {
        options.enablePersistentHovering();
    }

    @Override
    public void ignoreZoomSettings() {
        options.ignoreZoomSettings();
    }

    @Override
    public void introduceFlakinessByIgnoringSecurityDomains() {
        options.introduceFlakinessByIgnoringSecurityDomains();
    }

    @Override
    public void requireWindowFocus() {
        options.requireWindowFocus();
    }

    @Override
    public void useCreateProcessApiToLaunchIe() {
        options.useCreateProcessApiToLaunchIe();
    }

    @Override
    public void waitForUploadDialogUpTo(long duration, TimeUnit unit) {
        options.waitForUploadDialogUpTo(duration, unit)    ;
    }

    @Override
    public void withAttachTimeout(long duration, TimeUnit unit) {
        options.withAttachTimeout(duration, unit);
    }

    @Override
    public void withInitialBrowserUrl(String url) {
        options.withInitialBrowserUrl(url);
    }

    @Override
    public void addCommandSwitches(String... switches) {
        options.addCommandSwitches(switches);
    }

	@Override
	public Map<String, Object> asMap() {
		return options.asMap();
	}

	@Override
	public void elementScrollTo(ElementScrollBehavior behavior) {
		options.elementScrollTo(behavior);
		
	}

	@Override
	public void enableNativeEvents() {
		options.enableNativeEvents();
		
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
	public boolean isJavascriptEnabled() {
		return options.isJavascriptEnabled();
	}

	@Override
	public void setCapability(String capabilityName, Platform value) {
		options.setCapability(capabilityName, value);
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
	public void takeFullPageScreenshot() {
		options.takeFullPageScreenshot();
	}

	@Override
	public void usePerProcessProxy() {
		options.usePerProcessProxy();
	}

	@Override
	public void useShellWindowsApiToAttachToIe() {
		options.useShellWindowsApiToAttachToIe();
	}

	@Override
	public void waitForUploadDialogUpTo(Duration duration) {
		options.waitForUploadDialogUpTo(duration);
	}

	@Override
	public void waitForUploadDialogUpTo(Long duration, TimeUnit unit) {
		options.waitForUploadDialogUpTo(duration, unit);
	}

	@Override
	public void withAttachTimeout(Duration duration) {
		options.withAttachTimeout(duration);
	}
    
}