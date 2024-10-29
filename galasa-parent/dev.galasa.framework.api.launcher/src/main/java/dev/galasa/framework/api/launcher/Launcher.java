/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.launcher;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import dev.galasa.framework.FrameworkInitialisation;
import dev.galasa.framework.spi.FrameworkException;

@Component(configurationPid = { "dev.galasa" }, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class Launcher {
    private final String            bootstapLocationKey = "framework.bootstrap.url";
    private FrameworkInitialisation frameInit;

    private String                  bootstrapURL;

    @Activate
    public void activate(Map<String, Object> properties) throws FrameworkException {
        Properties bootstrapProperties = new Properties();
        Properties emptyOverrideProperties = new Properties();

        this.bootstrapURL = (String) properties.get(this.bootstapLocationKey);

        try {
            URL bootstrap = new URL(this.bootstrapURL);
            bootstrapProperties.load(bootstrap.openStream());
        } catch (IOException e) {
            throw new FrameworkException("Could not read from bootstrap");
        }

        try {
            frameInit = init(bootstrapProperties, emptyOverrideProperties);
        } catch (FrameworkException | InvalidSyntaxException | URISyntaxException e) {
            throw new FrameworkException("Failed to start framework", e);
        }

        if ((frameInit.getFramework()).isInitialised()) {
            return;
        }
        throw new FrameworkException("Framework not correctly Initialised");
    }

    public FrameworkInitialisation init(Properties bootstrap, Properties overrides)
            throws FrameworkException, InvalidSyntaxException, URISyntaxException {
        return new FrameworkInitialisation(bootstrap, overrides);
    }
}
